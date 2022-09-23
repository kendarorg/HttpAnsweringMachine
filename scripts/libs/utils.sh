#!/bin/sh

function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}

function read_password(){
  stty_orig=$(stty -g) # save original terminal setting.
  stty -echo           # turn-off echoing.
  IFS= read -r passwd  # read the password
  stty "$stty_orig"    # restore terminal setting.
  echo $passwd
}