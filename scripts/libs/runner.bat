@echo off
call:%~1 %~2 %~3 %~4 %~5 %~6 %~7 %~8 %~9 %~10
goto exit

REM terminate sh_file java 4.0.1-SNAPSHOT
:terminate_app
  sh_file=%~1
  first_grep=%~2
  second_grep=%~3
BY COMMAND FOR /F "delims=," %i IN (results.txt) DO @echo %i
stica.txt: VM-WIN10-JAVA,"C:\Program Files\Google\Chrome\Application\chrome.exe" ,VM-WIN10-JAVA,chrome.exe,C:\Program Files\Google\Chrome\Application\chrome.exe,,2008,1436,,28750000,1380,200,chrome.exe,Microsoft Windows 10 Pro|C:\Windows|\Device\Harddisk0\Partition2,33458,1052748,82153,66288,4920,80760,2255837044736,150172,8,67878912,2008,54,960,63,1020,19232,56762063,1,,,35,23125000,2255672991744,10.0.19042,149360640,15334,15967270
set /p STICA=<stica.txt
set STICA=%STICA:|=%
FOR /F "tokens=25 delims=\," %i IN ("%STICA%") DO @echo %i
  FOR /F "delims=;" %%i IN (csv_exports\account.csv) DO @echo %%i

  wmic process list /format:csv | grep -i "%first_grep%" | grep -i "%second_grep%"| awk '{print %~2goto :eof'|  xargs kill -9
goto :eof

:terminate_ham
  first_grep=%~1
  second_grep=%~2
  ps aux  |  grep -i "%first_grep%" | grep -i "%second_grep%"| awk '{print %~2goto :eof'|  xargs kill -9 2>&1 > /dev/null
goto :eof

REM  run_till_start seconds sh_file get_req
:run_till_start
  seconds=%~1
  sh_file=%~2
  get_req=%~3
  runtime=$(date +%s)
  endtime=`expr %runtime% + %seconds%`

  REM screen -A -m -d -S %sh_file% ./%sh_file% &
  nohup ./%sh_file% 2>&1 > /dev/null &

  REM ./%sh_file% &

  seconds=60
  runtime=$(date +%s)
  endtime=`expr %runtime% + %seconds%`

  echo -n "[INFO] Testing %get_req%: "
  while [[ $(date +%s) -le %endtime% ]]
  do
      sleep 10
      export IS_RUNNING=$(curl -m 2 -s -o /dev/null -w "%{http_codegoto :eof" %get_req%)
      if [ "%IS_RUNNING%" -eq "200" ]
      then
        echo -n " OK"
        break
      else
        echo -n .
      fi
  done
  if [[ "%IS_RUNNING%" != "200" ]]
  then
    echo
    echo "UNABLE TO START %sh_file%"
    terminate_app %sh_file% java HttpAnswer
    exit 1
  else
    echo ""
  fi
goto :eof

:wait_till_start
  seconds=%~1
  get_req=%~2
  runtime=$(date +%s)
  endtime=`expr %runtime% + %seconds%`


  REM ./%sh_file% &

  seconds=60
  runtime=$(date +%s)
  endtime=`expr %runtime% + %seconds%`

  echo -n "[INFO] Testing %get_req%: "
  while [[ $(date +%s) -le %endtime% ]]
  do
      sleep 10
      export IS_RUNNING=$(curl -m 2 -s -o /dev/null -w "%{http_codegoto :eof" %get_req%)
      if [ "%IS_RUNNING%" -eq "200" ]
      then
        echo -n " OK"
        break
      else
        echo -n .
      fi
  done
  if [[ "%IS_RUNNING%" != "200" ]]
  then
    echo
    echo "UNABLE TO START %sh_file%"
    exit 1
  else
    echo ""
  fi
goto :eof


:exit
exit /b