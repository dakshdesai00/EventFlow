import pool from "../db.js";

export async function enqueueRetry(executionId, attempt) {
  if (process.env.QUEUE_MODE === "POSTGRES") {
    setTimeout(async () => {
      try {
        await pool.query(
          `
          UPDATE executions
          SET
            status='PENDING',
            started_at=NULL,
            ended_at=NULL,
            error_message=NULL
          WHERE id=$1
          `,
          [executionId]
        );
      } catch (err) {
        console.error("Failed to enqueue retry in DB:", err);
      }
    }, 5000);
    return;
  }

  const { producer } = await import("../kafka.js");
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
