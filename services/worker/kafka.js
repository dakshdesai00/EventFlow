import { Kafka } from "kafkajs";

const kafka = new Kafka({
  clientId: "worker",
  brokers: [process.env.KAFKA_BROKER],
});

export const consumer = kafka.consumer({
  groupId: "function-runners",
});

export const retryConsumer = kafka.consumer({
  groupId: "retry-runners",
});

export const producer = kafka.producer();
