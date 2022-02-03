#!/bin/bash

/usr/sbin/sshd -D &
	 
export lastPid=$!
exit $lastPid