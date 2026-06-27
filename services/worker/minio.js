import dotenv from "dotenv";
import { Client } from "minio";

dotenv.config();

const minio = new Client({
  endPoint: process.env.MINIO_ENDPOINT,
  port: process.env.MINIO_PORT ? Number(process.env.MINIO_PORT) : undefined,
  useSSL: process.env.MINIO_USE_SSL === "true",
  accessKey: process.env.MINIO_ACCESS_KEY,
  secretKey: process.env.MINIO_SECRET_KEY,
});

export default minio;
