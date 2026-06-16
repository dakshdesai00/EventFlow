import pool from "./db.js";
import { executeFunction } from "./executeFunction.js";

const queueMode = process.env.QUEUE_MODE || "KAFKA";

if (queueMode === "POSTGRES") {
  console.log("Running in POSTGRES queue mode. Starting poller...");
  
  const pollInterval = parseInt(process.env.DB_POLL_INTERVAL_MS) || 50;
  
  const pollAndExecute = async () => {
    while (true) {
      try {
        const result = await pool.query(
          `
          UPDATE executions
          SET
            status='RUNNING',
            started_at=NOW(),
            worker_id=$1
          WHERE id = (
            SELECT id
            FROM executions
            WHERE status='PENDING'
            ORDER BY id ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
          )
          RETURNING id, attempt_count
          `,
          [process.env.HOSTNAME || "worker"]
        );
        
        if (result.rows.length > 0) {
          const row = result.rows[0];
          await executeFunction(row.id, row.attempt_count, true);
        } else {
          await new Promise((resolve) => setTimeout(resolve, pollInterval));
        }
      } catch (err) {
        console.error("Poller error:", err);
        await new Promise((resolve) => setTimeout(resolve, 1000));
      }
    }
  };
  
  pollAndExecute();
} else {
  const { consumer, producer } = await import("./kafka.js");
  
  await producer.connect();
  await consumer.connect();
  await consumer.subscribe({
    topic: "function-executions",
  });
  await consumer.run({
    eachMessage: async ({ message }) => {
      const data = JSON.parse(message.value.toString());
      await executeFunction(data.executionId, data.attempt, false);
    },
  });
  console.log("Function worker running in KAFKA mode");
}
