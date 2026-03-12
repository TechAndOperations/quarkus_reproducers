#!/bin/bash

# Wait for the ports file to be created
while [ ! -f /etc/kafka/docker/ports ]; do
  sleep 0.1;
done;

# Source the file to load PORT_* variables
source /etc/kafka/docker/ports;

# Use the port mappings
export KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:$PORT_9092;
# Run the Kafka broker executable
/etc/kafka/docker/run
