import pool from "../db.js";

export async function loadSecrets(functionId, projectId) {
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

  return secrets;
}
