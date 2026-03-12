#!/bin/sh

vault server -dev -dev-root-token-id="root" -dev-listen-address="0.0.0.0:8200" &
VAULT_PID=$!

echo "Waiting for Vault to start..."
sleep 2

echo "Writing secret..."
VAULT_ADDR="http://127.0.0.1:8200" vault kv put secret/my-app apiKey="12345"

wait $VAULT_PID