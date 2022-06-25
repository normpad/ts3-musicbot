#!/bin/sh

# This script lets you easily test ts3 clientquery commands on your musicbot system.
printf "Enter TeamSpeak ClientQuery API key: "
read -r API_KEY
printf "Enter teamspeak client address (defaults to localhost): "
read -r TS_ADDRESS
if [ -z "$TS_ADDRESS" ]; then
  TS_ADDRESS="localhost"
fi
while true; do
  printf "Enter teamspeak command: "
  read -r TS_COMMAND
  if [ "$TS_COMMAND" != "exit" ] && [ "$TS_COMMAND" != "quit" ]; then
    (echo "auth apikey=$API_KEY"; echo "$TS_COMMAND"; echo quit) | nc "$TS_ADDRESS" 25639; echo
  else
    break
  fi
done
