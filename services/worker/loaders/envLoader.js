import pool from "../db.js";

export async function loadEnv(functionId, projectId) {
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

  return env;
}
