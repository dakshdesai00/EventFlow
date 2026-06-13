import pool from "../db.js";
import { producer } from "../kafka.js";

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
