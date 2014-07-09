#!/bin/bash
# Launcher script, tested on Ubuntu 14.04
LOG_FILE="Afrobob.log"
LOG_FILE_SIZE=$(stat -c%s $LOG_FILE)
if [ $LOG_FILE_SIZE -gt 2000000 ]; then # (re)move log files larger than 2 MB
	mv $LOG_FILE $LOG_FILE~
	echo "log moved" > $LOG_FILE
fi
echo === Log from `date` === >> $LOG_FILE
java -version 2>&1 | tee -a $LOG_FILE
java -jar Charly_in_Madagascar.jar 2>&1 | tee -a $LOG_FILE
echo === EXIT $? === >> $LOG_FILE
