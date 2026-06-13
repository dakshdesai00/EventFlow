import { consumer, producer } from "./kafka.js";

import { executeFunction } from "./executeFunction.js";

await producer.connect();

await consumer.connect();

await consumer.subscribe({
  topic: "function-executions",
});

await consumer.run({
  eachMessage: async ({ message }) => {
    const data = JSON.parse(message.value.toString());

    await executeFunction(data.executionId, data.attempt);
  },
});

console.log("Function worker running");
