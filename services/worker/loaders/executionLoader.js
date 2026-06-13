import pool from "../db.js";

export async function loadExecution(executionId) {
  const result = await pool.query(
    `
    SELECT
      e.*,
      f.project_id,
      f.timeout_ms,
      f.memory_limit_mb,
      fv.storage_key
    FROM executions e
    JOIN functions f
      ON e.function_id=f.id
    JOIN function_versions fv
      ON fv.id=f.active_version_id
    WHERE e.id=$1
  `,
    [executionId],
  );

  return result.rows[0];
}
