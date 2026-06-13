import fs from "fs";
import path from "path";

export async function runCode(filePath, payload, env, timeoutMs, logger) {
  const copiedFile = path.resolve(`${filePath}-${Date.now()}.mjs`);

  fs.copyFileSync(filePath, copiedFile);

  const mod = await import(`file://${copiedFile}`);

  if (!mod.default) {
    throw new Error("No default export found");
  }

  const timeoutPromise = new Promise((_, reject) =>
    setTimeout(() => reject(new Error("Execution timeout")), timeoutMs),
  );

  return Promise.race([mod.default(payload, env, logger), timeoutPromise]);
}
