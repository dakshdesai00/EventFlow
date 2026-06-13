import pool from "../db.js";

export async function log(executionId, level, message) {
  await pool.query(
    `
    INSERT INTO execution_logs
    (
      execution_id,
      level,
      timestamp,
      message
    )
    VALUES
    (
      $1,
      $2,
      NOW(),
      $3
    )
    `,
    [executionId, level, message],
  );
}
