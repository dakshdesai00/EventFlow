import pool from "../db.js";

export async function sendToDlq(executionId, error) {
  await pool.query(
    `
    UPDATE executions
    SET
      status='FAILED_PERMANENTLY',
      ended_at=NOW(),
      error_message=$2
    WHERE id=$1
    `,
    [executionId, error],
  );

  if (process.env.QUEUE_MODE === "POSTGRES") {
    return;
  }

  const { producer } = await import("../kafka.js");
  await producer.send({
    topic: "function-executions-dlq",
    messages: [
      {
        value: JSON.stringify({
          executionId,
          error,
        }),
      },
    ],
  });
}
