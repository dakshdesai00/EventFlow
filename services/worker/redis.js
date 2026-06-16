import { createClient } from "redis";
import dotenv from "dotenv";

dotenv.config();

const redisUrl = process.env.REDIS_URL || "redis://localhost:6379";
let redisClient = null;

if (process.env.CACHE_MODE === "REDIS") {
  redisClient = createClient({
    url: redisUrl,
  });

  redisClient.on("error", (err) => console.error("Redis Client Error", err));

  await redisClient.connect();
  console.log("Connected to Redis at", redisUrl);
}

export default redisClient;
