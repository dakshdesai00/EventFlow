# EventFlow: Scalable Event-Driven Serverless Function Execution Engine

This document provides a detailed technical report of the EventFlow system architecture, its components, data models, design decisions, security profile, and plans for future scalability.

EventFlow is a distributed, event-driven serverless platform that allows developers to register webhook events, define subscriptions, and execute custom JavaScript functions asynchronously in response to HTTP requests. The system is designed to handle high-throughput event ingestion, decouple ingestion from execution, validate request origins, and manage function scaling.

---

## 1. System Architecture

EventFlow is designed as a microservices architecture composed of two core services: the **Control Plane** and the **Worker Pool**, coordinated via **Apache Kafka** and backed by **PostgreSQL** and **MinIO**.

```
                           +----------------------+
                           |  External Webhooks   |
                           +-----------+----------+
                                       | HTTP POST
                                       v
                           +----------------------+
                           |    Control Plane     |
                           |    (Spring Boot)     |
                           +-----+-----+------+---+
                                 |     |      |
        JPA/SQL Queries (Read)   |     |      | Put Object (Upload ZIP/JS)
   +-----------------------------+     |      +----------------------------+
   |                                   |                                   |
   v                                   | Write Event                       v
+--+------------------+                |                               +---+------------------+
|      PostgreSQL     |                v                               |        MinIO         |
|  (Relational DB)    |     +----------+----------+                    |   (Object Store)     |
+--+------------------+     |     Apache Kafka    |                    +---+------------------+
   ^                        |    (Message Bus)    |                        ^
   |                        +----------+----------+                        |
   | JPA/SQL Queries (Write)           |                                   |
   |                                   | Consume Execution Job             | fGetObject (Download JS)
   |                                   v                                   |
   |                       +-----------+----------+                        |
   +-----------------------+     Worker Pool      +------------------------+
                           |  (Node.js instances) |
                           +----------------------+
```

### 1.1 Ingestion & Ingress Control (Control Plane)
The Control Plane is built using **Java** and **Spring Boot 4.x/3.x**. It is responsible for all metadata management, security configuration, user authentication, and synchronous API endpoints.
* **REST API Interfaces**: Manages entity creation (Users, Projects, Functions, Subscriptions) and handles incoming webhook ingestion.
* **JPA & ORM Layer**: Interacts with PostgreSQL using Hibernate to enforce schema constraints and database-level consistency.
* **Security & Authentication**: Implements JWT-based request filtering to authenticate and authorize administrative operations.
* **Kafka Template Integration**: Acts as a producer, publishing execution tasks to Kafka when webhook events are ingested.

### 1.2 Storage Engine (MinIO)
To ensure separation of database state from large binary data, user-uploaded JavaScript function files are packaged and stored as objects in **MinIO**, an S3-compatible object storage server.
* **Control Plane Upload**: The Control Plane uploads functions upon registration or version updates.
* **Worker Retrieval**: Workers download code bundles asynchronously on-demand prior to execution.

### 1.3 Asynchronous Event Bus (Apache Kafka)
**Apache Kafka** acts as the decoupled transport medium between the ingress layer and the execution layer.
* **`function-executions` Topic**: Receives execution requests containing metadata (`executionId`, `attemptCount`).
* **`retry-executions` Topic**: Receives execution retries, isolated from primary executions to prevent head-of-line blocking.
* **`function-executions-dlq` (Dead Letter Queue) Topic**: Captures poison pills and permanently failed executions (after exceeding maximum retry limit) for manual auditing.

### 1.4 Execution Layer (Worker Pool)
The Worker Pool consists of containerized **Node.js** processes that run asynchronously.
* **Function Runner (`consumer.js`)**: Consumes from `function-executions`. Pulls execution metadata from PostgreSQL, downloads the target code from MinIO, executes it within a sandboxed wrapper, updates PostgreSQL execution status, and logs output.
* **Retry Runner (`retryConsumer.js`)**: Consumes from `retry-executions`. Implements a backoff delay (e.g., 5 seconds) before invoking the execution runner again.

---

## 2. Relational Schema & Data Modeling

EventFlow uses PostgreSQL as its primary metadata and state store. The schema is optimized for relational consistency, mapping hierarchical relationships from users down to execution logs.

```
 +------------------+
 |       User       |
 +--------+---------+
          | 1
          |
          | *
 +--------v---------+
 |     Project      |
 +---+----+----+----+
     | 1  | 1  | 1
     |    |    |
     | *  | *  | *
     |    |    +-----------------------------+
     |    +--------------------+             |
     |                         |             |
 +---v----+               +----v---+    +----v----+
 |Function|               | Event  |    | Env/Sec |
 +---+----+               +----+---+    +---------+
     | 1                       | 1
     |                         |
     | *                       | *
 +---v----+               +----v----+
 |Version | <-------------+ Subscr  |
 +--------+  1         *  +---------+
```

### 2.1 Core Relational Tables
* **`users`**: Contains authenticated user records with password hashing (via Spring Security).
* **`projects`**: Organizes services into logical isolated groups. Every project is owned by a single user.
* **`functions`**: Configures execution parameters (such as `timeoutMs`, `memoryLimitMb`, and reference to the `activeVersion`). Unique constraints are enforced on `(project_id, name)`.
* **`function_versions`**: Stores historical function deployments. Includes a `storageKey` referencing MinIO, a `fileHash` for integrity verification, and a `versionNumber` unique per function.
* **`events`**: Defines webhook endpoints and triggers. Generates a unique `webhookToken` used in the endpoint URL to route incoming payloads.
* **`event_allowed_domains`**: Protects webhooks from unauthorized origins. Stores permitted domains validated against the incoming HTTP `Origin` or `Referer` headers.
* **`event_subscriptions`**: Maps events to target functions. Creates a many-to-many join table between `events` and `functions`.
* **`executions`**: Tracks individual runs. Records start/end timestamps, duration in milliseconds, worker hostname, execution status (`PENDING`, `RUNNING`, `SUCCESS`, `FAILED`, `FAILED_PERMANENTLY`), payload data, and retry attempts.
* **`execution_logs`**: Capture stdout/stderr streams generated by functions. Logs are linked back to their corresponding `execution_id` and cataloged with a log level (`INFO`, `WARN`, `ERROR`, `DEBUG`) and timestamp.
* **`environment_variables` / `secret_variables`**: Key-value stores for execution context. Can be mapped at the project level (shared across all functions in the project) or function level. Secret variables are segregated to support separate access auditing and encryption.

---

## 3. Core Workflows & Mechanics

### 3.1 Webhook Ingestion & Origin Check
1. An external service posts a JSON payload to `/api/events/trigger/{webhookToken}`.
2. The Control Plane queries the event via `webhookToken`.
3. Origin validation is performed using the `Origin` header. If the event configuration restricts domains, and the request origin is not matched, the transaction is immediately rejected with a HTTP 403 Forbidden.
4. If validation succeeds, the Control Plane queries all functions subscribed to this event.

### 3.2 Job Dispatching via Kafka
1. For each subscribed function, the Control Plane inserts a pending record into `executions` (returning a unique `executionId`).
2. The Control Plane publishes a message to `function-executions` containing `executionId` and `attempt = 0`.
3. The HTTP response is returned immediately to the webhook client with a status of `202 Accepted` along with the execution identifiers, minimizing ingress latency.

### 3.3 Worker Consumption and Sandboxed Execution
1. A Node.js worker pulls a message from the `function-executions` partition.
2. The worker updates the execution status to `RUNNING`, notes the start timestamp, and records its `worker_id`.
3. The worker queries the database to load the merged environment variables and secrets, and reads the active version's MinIO storage key.
4. The worker downloads the file from MinIO to the local directory.
5. The wrapper runs the code:
```javascript
const mod = await import(`file://${tempFile}`);
const result = await Promise.race([
  mod.default(payload, env, logger),
  timeoutPromise
]);
```
6. The custom logger injected into the runner pipes function output in real-time straight to PostgreSQL `execution_logs`.

### 3.4 Fault Tolerance, Retries, and DLQ
* **Execution Failure**: If execution fails (due to code crash or exceeding `timeoutMs`), the worker Catches the exception, updates the status to `FAILED`, and increments `attempt_count`.
* **Retry Route**: If `attempt_count < 5`, the worker pushes a retry job to the `retry-executions` topic.
* **Retry Backoff**: The `retry-runner` consumes this topic, executes a 5-second asynchronous delay, and dispatches the execution logic again to prevent thundering herds on upstream resources.
* **DLQ Route**: If attempts reach 5, the worker updates the status to `FAILED_PERMANENTLY` and publishes the message to the DLQ topic for monitoring and alerting.

---

## 4. Technical Stack Decisions & Justifications

### 4.1 PostgreSQL
PostgreSQL was selected over NoSQL alternatives (e.g., MongoDB, DynamoDB) due to the relational nature of EventFlow's core data model:
* **Relational Integrity**: The relationship between Users, Projects, Functions, Active Versions, Subscriptions, and Executions requires strict foreign key constraints and cascade rules to prevent orphaned records.
* **Complex Joins**: Webhook routing requires crossing subscriptions and configurations. PostgreSQL handles relational joins efficiently, backed by indexes on columns like `webhookToken`, `eventId`, and `functionId`.
* **ACID Transactions**: Modifying function versions, switching active versions, and updating execution states require strict write consistency and serialization.

### 4.2 Spring Boot and Java
The Control Plane is built on Java/Spring Boot for enterprise durability:
* **Static Typing and Safety**: Compile-time safety reduces configuration bugs in complex ingress pipelines.
* **Thread Management**: High performance is achieved via HikariCP connection pooling and Spring Web MVC's thread-per-request architecture, which handles ingestion load without blocking.
* **Ecosystem Integration**: Spring Kafka provides clean abstractions for partition assignment, serialization, and high-performance publishing. Spring Security offers standardized JWT filtration.

### 4.3 Apache Kafka vs. RabbitMQ
Apache Kafka was selected over RabbitMQ because EventFlow is designed to function as an event stream processing system rather than a traditional task distribution system:

| Feature | Apache Kafka (EventFlow Choice) | RabbitMQ (Alternative Considered) |
| :--- | :--- | :--- |
| **Model** | Pull-based log replication. Consumers pull messages from partitions. | Push-based routing. Broker manages message delivery states. |
| **Throughput** | High throughput via sequential disk I/O, zero-copy socket transfers, and batched records. | Lower throughput due to routing logic overhead and queue state management in memory. |
| **Durability & Replay** | Append-only logs are persisted on disk. Messages can be replayed from any offset. | Messages are deleted immediately upon consumption and acknowledgment. |
| **Horizontal Scaling** | Highly scalable. Partitioning allows linear scale-out of consumers within a group. | Harder to scale horizontally; relies on queue mirroring, which degrades performance. |
| **Decoupling** | Multiple independent consumer groups can read the same stream without message duplication. | Requires configuring exchanges, fanouts, and dedicated queues for each receiver group. |

In EventFlow, Kafka's pull model enables workers to control their consumption rates, shielding them from ingestion spikes. Replayability allows developers to inspect historical failures and re-run executions on old payloads, a critical requirement for serverless computing.

### 4.4 Node.js Workers
While the Control Plane uses Java, the execution workers use Node.js:
* **Single-Language Runtime**: Enables developers to write native JavaScript functions (the default for serverless deployments).
* **Dynamic Loading**: Node.js allows files to be imported dynamically at runtime using ES modules, avoiding compilation steps.
* **Non-Blocking I/O**: The single-threaded event loop processes multiple downstream I/O requests (MinIO downloads, PostgreSQL logging) concurrently.

---

## 5. Architectural Evaluation & Security Critique

### 5.1 Critique of Current Isolation Model
The current isolation model uses dynamic JS module loading (`import()`) and runs code inside the main thread of the worker Node.js process. This has significant design flaws:

1. **Shared Process Context**: Code executed via dynamic import shares the exact same memory space and environment as the host worker process. A malicious function can access `process.env` and leak the PostgreSQL credentials, MinIO access keys, and Kafka connection details.
2. **Resource Exhaustion (Denial of Service)**: Because Node.js is single-threaded, a function containing an infinite loop (e.g., `while(true) {}`) will block the thread's event loop entirely. This halts all other executions assigned to that worker instance.
3. **Filesystem Compromise**: Dynamic imports run with host process privileges. A function can read or write to any location in the local filesystem, enabling access to other users' temporary code execution files.
4. **Prototype Pollution**: User code can modify global prototypes (`Object.prototype`), leading to runtime errors or security leaks in subsequent executions.

### 5.2 Future Scalability and Production-Grade Refactoring

To transform EventFlow into a production-grade, multi-tenant system, the following design improvements must be made:

```
                            +--------------------------+
                            |      Ingress Node        |
                            |   (Control Plane JVM)    |
                            +------------+-------------+
                                         |
                                         v (Kafka Trigger)
                            +--------------------------+
                            |       Worker Node        |
                            |   (Node.js Orchestrator) |
                            +-----+--------------+-----+
                                  |              |
                    gRPC (Low     |              | Spawn Sandbox
                    Latency)      |              v
                                  |     +------------------+
                                  |     | Docker Container |
                                  |     | (Firecracker VM) |
                                  |     |                  |
                                  |     | +--------------+ |
                                  +---->| | User JS Code | |
                                        | +--------------+ |
                                        +------------------+
```

#### A. Ephemeral Process Isolation (Docker / Micro-VMs)
* **Design Change**: Replace in-process execution with ephemeral containerized tasks.
* **Implementation**: The Worker Node orchestrates the execution lifecycle. Upon receiving a task, the worker spawns an isolated container (using Docker or gVisor) or a lightweight micro-VM (like AWS Firecracker).
* **Constraints**: Enforce strict resources allocations via cgroups (e.g., limit CPU shares and memory to `128MB`), configure a read-only root filesystem, disable network routing (except to approved endpoints), and terminate the container instantly if `timeoutMs` is breached.

#### B. Database-per-Service Pattern
* **Design Change**: Decouple the database schema. The Worker should not query the primary PostgreSQL database.
* **Implementation**: The Control Plane owns the metadata and configuration schema. The Worker operates in a separate database layer or relies entirely on configuration APIs, preventing data leaks across service boundaries.

#### C. gRPC for Control-Worker Communication
* **Design Change**: Replace direct database reads from the worker with high-performance APIs.
* **Implementation**: Implement a gRPC communication channel between the Control Plane and the Worker Pool. Workers fetch metadata (like environment variables, credentials, and source properties) and stream execution states and logs back using bidirectional streaming gRPC over HTTP/2. This increases performance and maintains microservices boundaries.

#### D. Distributed Caching via Redis
* **Design Change**: Introduce caching to eliminate PostgreSQL database round-trips.
* **Implementation**: Use Redis to cache active function versions, subscription graphs, and environment configurations using a cache-aside pattern. Since metadata changes infrequently compared to execution frequency, fetching configurations from Redis reduces PostgreSQL read load, allowing the system to scale to millions of executions.

#### E. Database Replication and Sharding
* **Design Change**: Partition tables to scale writes.
* **Implementation**: 
  * Implement master-slave database replication for metadata, routing reads to read replicas.
  * Shard the high-volume `execution_logs` table based on `project_id` or `execution_id`. Alternatively, offload execution logs to a write-optimized database system (e.g., Elasticsearch, ClickHouse) to prevent execution log storage from bottlenecking transactions.
