import fs from "fs";
import os from "os";
import path from "path";

import pool from "./db.js";
import minio from "./minio.js";

import { runCode } from "./sandbox/runCode.js";

import { log } from "./services/logService.js";
import { sendToDlq } from "./services/dlqService.js";
import { enqueueRetry } from "./services/retryService.js";

import { loadExecution } from "./loaders/executionLoader.js";
import { loadEnv } from "./loaders/envLoader.js";
import { loadSecrets } from "./loaders/secretLoader.js";

export async function executeFunction(executionId, attempt = 0, skipRunningUpdate = false) {
  const execution = await loadExecution(executionId);

  if (!execution) {
    return;
  }

  if (!skipRunningUpdate) {
    await pool.query(
      `
      UPDATE executions
      SET
        status='RUNNING',
        started_at=NOW(),
        worker_id=$2
      WHERE id=$1
      `,
      [executionId, process.env.HOSTNAME || "worker"],
    );
  }

  try {
    const env = await loadEnv(execution.function_id, execution.project_id);

    const secrets = await loadSecrets(
      execution.function_id,
      execution.project_id,
    );

    const payload = execution.payload ? JSON.parse(execution.payload) : {};

    const tempFile = path.join(
      os.tmpdir(),
      `fn-${executionId}-${Date.now()}.js`,
    );

    await minio.fGetObject(
      process.env.MINIO_BUCKET,
      execution.storage_key,
      tempFile,
    );

    const logger = {
      info: async (msg) => await log(executionId, "INFO", String(msg)),

      warn: async (msg) => await log(executionId, "WARN", String(msg)),

      error: async (msg) => await log(executionId, "ERROR", String(msg)),

      debug: async (msg) => await log(executionId, "DEBUG", String(msg)),
    };

    const result = await runCode(
      tempFile,
      payload,
      {
        ...env,
        ...secrets,
      },
      execution.timeout_ms,
      logger,
    );

    await log(executionId, "INFO", JSON.stringify(result));

    await pool.query(
      `
      UPDATE executions
      SET
        status='SUCCESS',
        ended_at=NOW(),
        duration_ms =
          EXTRACT(
            EPOCH FROM (
              NOW() - started_at
            )
          ) * 1000
      WHERE id=$1
      `,
      [executionId],
    );

    try {
      fs.unlinkSync(tempFile);
    } catch {}
  } catch (err) {
    await log(executionId, "ERROR", err.message);

    await pool.query(
      `
      UPDATE executions
      SET
        status='FAILED',
        ended_at=NOW(),
        error_message=$2,
        attempt_count = attempt_count + 1
      WHERE id=$1
      `,
      [executionId, err.message],
    );

    if (attempt < 5) {
      await enqueueRetry(executionId, attempt + 1);
    } else {
      await sendToDlq(executionId, err.message);
    }
  }
}
