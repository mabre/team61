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

if java -version 2>&1 | grep -q 1.7 || java -version 2>&1 | grep -q 1.6; then # check for Java 6 und 7 and display error message if found
	notify-send -t 10 -a "Charly in Madagascar" -i dialog-error "Charly in Madagascar" "Please update to Java 8. Refer to the installation section of the manual."
	echo === EXIT NOJAVA8 === >> $LOG_FILE
	exit -1
fi

notify-send -t 1 -a "Charly in Madagascar" -i dialog-information "Charly in Madagascar" "Charly in Madagascar is starting …"
starttime=`date +%s`
java -jar Charly_in_Madagascar.jar 2>&1 | tee -a $LOG_FILE
endtime=`date +%s`
runtime=$(($endtime-$starttime))
echo runtime $runtime >> $LOG_FILE
echo === EXIT $? === >> $LOG_FILE

if [ $runtime -lt 2 ]; then # probably no correct start if runtime is less than 1 second
	notify-send -t 10 -a "Charly in Madagascar" -i dialog-warning "Charly in Madagascar" "Something probably went wrong. Please have a look at the manual and Afrobob.log."
	exit -2
fi

exit 0
