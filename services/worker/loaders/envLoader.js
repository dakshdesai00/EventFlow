import pool from "../db.js";
import redisClient from "../redis.js";

export async function loadEnv(functionId, projectId) {
  const cacheKey = `env:${projectId}:${functionId}`;

  if (process.env.CACHE_MODE === "REDIS" && redisClient) {
    try {
      const cached = await redisClient.get(cacheKey);
      if (cached) {
        return JSON.parse(cached);
      }
    } catch (err) {
      console.error("Redis error in loadEnv:", err);
    }
  }

  const env = {};

  const projectVars = await pool.query(
    `
      SELECT *
      FROM environment_variables
      WHERE project_id=$1
      AND function_id IS NULL
    `,
    [projectId],
  );

  const functionVars = await pool.query(
    `
      SELECT *
      FROM environment_variables
      WHERE function_id=$1
    `,
    [functionId],
  );

  for (const row of projectVars.rows) env[row.key] = row.value;

  for (const row of functionVars.rows) env[row.key] = row.value;

  if (process.env.CACHE_MODE === "REDIS" && redisClient) {
    try {
      await redisClient.set(cacheKey, JSON.stringify(env), { EX: 60 });
    } catch (err) {
      console.error("Redis set error in loadEnv:", err);
    }
  }

  return env;
}
