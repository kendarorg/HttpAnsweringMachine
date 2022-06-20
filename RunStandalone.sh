#!/bin/sh

mypath=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )/

cd $mypath


function pause(){
 read -s -n 1 -p "Press any key to continue . . ."
 echo ""
}



echo run chrome with: --proxy-server="socks5://localhost:1080"
pause

echo '#!/bin/bash' > $mypath/Kill.sh

cd "$mypath"ham/app/target
mkdir -p "$mypath"ham/app/target/libs
rm -rf "$mypath"ham/app/target/libs/*.*
cp -f "$mypath"ham/libs/*.jar "$mypath"ham/app/target/libs/

ls -lA|grep -oE '[^ ]+$'|grep .jar$ > tmp_txt
export JAR_NAME=$(head -1 tmp_txt)


tokill=$1
#echo "kill -9 $tokill" >> $mypath/Kill.sh  
	

cd "$mypath"ham/app/target
mkdir -p "$mypath"ham/app/target/external
cp -f "$mypath"ham/*.json "$mypath"ham/app/target/
cp -f "$mypath"ham/external/*.json "$mypath"ham/app/target/external/
java "-Dloader.path=$mypath/ham/app/target/libs"  -Dloader.main=org.kendar.Main  \
  	-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5025 \
	-jar "$JAR_NAME" org.springframework.boot.loader.PropertiesLauncher &


pause
