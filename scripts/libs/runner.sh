#!/bin/bash

#terminate sh_file java 4.0.1-SNAPSHOT
function terminate_app {
  sh_file=$1
  first_grep=$2
  second_grep=$3
  screen -ls $sh_file | grep -E '\s+[0-9]+\.' | awk -F ' ' '{print $1}' | while read s; do screen -XS $s quit; done
  ps aux  |  grep -i "$first_grep" | grep -i "$second_grep"| awk '{print $2}'|  xargs kill -9
}

function terminate_ham {
  first_grep=$1
  second_grep=$2
  screen -ls $sh_file | grep -E '\s+[0-9]+\.' | awk -F ' ' '{print $1}' | while read s; do screen -XS $s quit; done
  ps aux  |  grep -i "$first_grep" | grep -i "$second_grep"| awk '{print $2}'|  xargs kill -9 2>&1 > /dev/null
}

# run_till_start seconds sh_file get_req
function run_till_start {
  seconds=$1
  sh_file=$2
  get_req=$3
  runtime=$(date +%s)
  endtime=`expr $runtime + $seconds`

  #screen -A -m -d -S $sh_file ./$sh_file &
  nohup ./$sh_file 2>&1 > /dev/null &

  #./$sh_file &

  seconds=60
  runtime=$(date +%s)
  endtime=`expr $runtime + $seconds`

  echo -n "[INFO] Testing $get_req: "
  while [[ $(date +%s) -le $endtime ]]
  do
      sleep 10
      export IS_RUNNING=$(curl -m 2 -s -o /dev/null -w "%{http_code}" $get_req)
      if [ "$IS_RUNNING" -eq "200" ]
      then
        echo -n " OK"
        break
      else
        echo -n .
      fi
  done
  if [[ "$IS_RUNNING" != "200" ]]
  then
    echo
    echo "UNABLE TO START $sh_file"
    terminate_app $sh_file java HttpAnswer
    exit 1
  else
    echo ""
  fi
}