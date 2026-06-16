import pool from "../db.js";
import redisClient from "../redis.js";

export async function loadSecrets(functionId, projectId) {
  const cacheKey = `secrets:${projectId}:${functionId}`;

  if (process.env.CACHE_MODE === "REDIS" && redisClient) {
    try {
      const cached = await redisClient.get(cacheKey);
      if (cached) {
        return JSON.parse(cached);
      }
    } catch (err) {
      console.error("Redis error in loadSecrets:", err);
    }
  }

  const secrets = {};

  const projectSecrets = await pool.query(
    `
      SELECT *
      FROM secret_variables
      WHERE project_id=$1
      AND function_id IS NULL
    `,
    [projectId],
  );

  const functionSecrets = await pool.query(
    `
      SELECT *
      FROM secret_variables
      WHERE function_id=$1
    `,
    [functionId],
  );

  for (const row of projectSecrets.rows) secrets[row.key] = row.value;

  for (const row of functionSecrets.rows) secrets[row.key] = row.value;

  if (process.env.CACHE_MODE === "REDIS" && redisClient) {
    try {
      await redisClient.set(cacheKey, JSON.stringify(secrets), { EX: 60 });
    } catch (err) {
      console.error("Redis set error in loadSecrets:", err);
    }
  }

  return secrets;
}
