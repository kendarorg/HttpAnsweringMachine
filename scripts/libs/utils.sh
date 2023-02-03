#!/bin/bash

function pause{
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

function read_password{
  stty_orig=$(stty -g) # save original terminal setting.
  stty -echo           # turn-off echoing.
  IFS= read -r passwd  # read the password
  stty "$stty_orig"    # restore terminal setting.
  echo $passwd
}

function set_parent_dir{
  INIT_START_DIR=$1
  INIT_START_DIR=$( cd -- "$( dirname -- "$INIT_START_DIR" )" &> /dev/null && pwd )
  echo $INIT_START_DIR
}

function is_set { [[ $var ]]; echo $?; }