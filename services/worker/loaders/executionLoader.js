import pool from "../db.js";
import redisClient from "../redis.js";

export async function loadExecution(executionId) {
  // Always query execution details from database as payload and attempts are dynamic per-execution
  const executionRes = await pool.query(
    "SELECT * FROM executions WHERE id = $1",
    [executionId]
  );
  const execution = executionRes.rows[0];
  if (!execution) return null;

  const functionId = execution.function_id;
  let fnConfig = null;

  if (process.env.CACHE_MODE === "REDIS" && redisClient) {
    try {
      const cached = await redisClient.get(`fn_config:${functionId}`);
      if (cached) {
        fnConfig = JSON.parse(cached);
      }
    } catch (err) {
      console.error("Redis error in loadExecution:", err);
    }
  }

  if (!fnConfig) {
    const dbResult = await pool.query(
      `
      SELECT
        f.project_id,
        f.timeout_ms,
        f.memory_limit_mb,
        fv.storage_key
      FROM functions f
      JOIN function_versions fv
        ON fv.id=f.active_version_id
      WHERE f.id=$1
      `,
      [functionId]
    );
    fnConfig = dbResult.rows[0];

    if (fnConfig && process.env.CACHE_MODE === "REDIS" && redisClient) {
      try {
        await redisClient.set(
          `fn_config:${functionId}`,
          JSON.stringify(fnConfig),
          { EX: 60 } // 60 seconds TTL
        );
      } catch (err) {
        console.error("Redis set error in loadExecution:", err);
      }
    }
  }

  return {
    ...execution,
    ...fnConfig
  };
}
