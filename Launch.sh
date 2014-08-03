#!/bin/bash
# Launcher script, tested on Ubuntu 14.04
# IF YOU SEE THIS FILE, PLEASE REFER TO THE MANUAL (FAQ)
LOG_FILE="Afrobob.log"
if [ ! -w . ]; then
    echo cannot write to .
    LOG_FILE="/tmp/Afrobob.log"
    echo trying /tmp instead
fi
if [ -d $LOG_FILE ]; then
	LOG_FILE_SIZE=$(stat -c%s $LOG_FILE)
	if [ $LOG_FILE_SIZE -gt 2000000 ]; then # (re)move log files larger than 2 MB
		mv $LOG_FILE $LOG_FILE~
		echo "log moved" > $LOG_FILE
	fi
fi
echo === Log from `date` === >> $LOG_FILE
java -version 2>&1 | tee -a $LOG_FILE
uname -a | tee -a $LOG_FILE
java -jar Charly_in_Madagascar.jar 2>&1 | tee -a $LOG_FILE
echo === EXIT $? === >> $LOG_FILE
