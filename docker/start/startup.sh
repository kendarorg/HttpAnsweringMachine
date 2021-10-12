#!/bin/bash

chmod 777 /start/*.sh|true
chmod 777 /start/runonce/*.sh|true
chmod 777 /start/services/*.sh|true

if [ -d "/start/runonce" ]; then
	for file in /start/runonce/*.sh
	do
		echo "Running $file"
	  	"$file"
	  	status=$?
		if [ $status -ne 0 ]; then
			echo "Failed to start runonce $file: $status"
			exit $status
		else
			echo "Running $file: $status"
		fi
	done
fi

export multitail=tail

if [ -d "/start/services" ]; then
	#COUNTER=-1
	#for file in /start/services/*.sh
	#do
	#	COUNTER=$((COUNTER+1))s
	#  	"$file"
	#  	currentPid=$?
	#  	echo $currentPid > "$file.pid"
	#  	# "$file" > "/start/logs/$COUNTER.log" &
	#  	if [ $currentPid -ne 0 ]; then
	#		echo "Started process $file with PID:$currentPid"
	#	else
	#		echo "Error starting $file"
	#	fi
	#done
	
	

	# Naive check runs checks once a minute to see if either of the processes exited.
	# This illustrates part of the heavy lifting you need to do if you want to run
	# more than one service in a container. The container exits with an error
	# if it detects that either of the processes has exited.
	# Otherwise it loops forever, waking up every 60 seconds

	while sleep 10; do
		
		COUNTER=-1
		for file in /start/services/*.sh
		do
			COUNTER=$((COUNTER+1))
			currentPid=0
			
			if [ -f "$file.pid" ]; then
				currentPid=$(cat "$file.pid")
			fi
			pidExpression=\\s$currentPid$
			ps -o pid|grep -E $pidExpression > /dev/null
			psResult=$?
			if [ $psResult -ne 0 ];then
				echo "$file not running, restarting"
				"$file"
			  	currentPid=$?
			  	echo $currentPid > "$file.pid"
			fi
		done
	done
fi
#grep -E '[\s]1[\s]'