#!/bin/bash

# Start the Python script in the background
cd /app/scripts || exit
nohup python3 jumper.py > /app/data/jumper.log 2>&1 &

# Wait for a moment to ensure the script has started
sleep 2

# Start the Java API (as the main process)
cd /app || exit
exec java -jar app.jar