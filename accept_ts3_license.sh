#!/bin/bash

# Ensure the configuration directory exists
CONFIG_DIR="$HOME/.ts3client"
mkdir -p "$CONFIG_DIR"

# Define the settings database path
SETTINGS_DB="$CONFIG_DIR/settings.db"

# Ensure sqlite3 is installed
if ! command -v sqlite3 &> /dev/null; then
    echo "sqlite3 is required but not installed. Please install it and run this script again."
    exit 1
fi

# Create or update the settings.db to accept the license
sqlite3 "$SETTINGS_DB" <<EOF
CREATE TABLE IF NOT EXISTS Properties (key TEXT PRIMARY KEY, value TEXT);
INSERT OR REPLACE INTO Properties (key, value) VALUES ('LicenseAccepted', '1');
EOF

echo "TeamSpeak 3 client license accepted successfully."
