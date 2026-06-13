import { producer } from "../kafka.js";

export async function enqueueRetry(executionId, attempt) {
  await producer.send({
    topic: "retry-executions",
    messages: [
      {
        value: JSON.stringify({
          executionId,
          attempt,
        }),
      },
    ],
  });
}
