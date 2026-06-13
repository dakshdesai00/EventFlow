import { retryConsumer } from "./kafka.js";
import { executeFunction } from "./executeFunction.js";

await retryConsumer.connect();

await retryConsumer.subscribe({
  topic: "retry-executions",
});

await retryConsumer.run({
  eachMessage: async ({ message }) => {
    const data = JSON.parse(message.value.toString());

    await new Promise((resolve) => setTimeout(resolve, 5000));

    await executeFunction(data.executionId, data.attempt);
  },
});

console.log("Retry worker running");
