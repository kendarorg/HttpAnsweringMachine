#!/bin/bash

echo Default User $MYSQL_USER with $MYSQL_PASSWORD on $MYSQL_DBS with scripts on $MYSQL_DATA

IFS=';'
read -r -a DATABASES <<< $MYSQL_DBS
#DATABASES=($(echo $MYSQL_DBS | tr ";" "\n"))
export CHECK_DB=${DATABASES[0]}

# Wait for mysql to start
server="localhost"
echo "Waiting for MySQL at ${server}"
echo

while true
do
    /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "select date(now())" &> /dev/null
    if [ $? == 0 ]; 
    then 
        break
    else
        sleep 100
    fi
  # loop infinitely
done

if /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "use $CHECK_DB";
then
    echo Databases already present
else
    echo Create standard user
    /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "CREATE USER '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';"
    /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'%' WITH GRANT OPTION;"
    /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "CREATE USER '$MYSQL_USER'@'localhost' IDENTIFIED BY '$MYSQL_PASSWORD';"
    /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'localhost' WITH GRANT OPTION;"
    /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "FLUSH PRIVILEGES;"
    echo Create database
    for DB_NAME in "${DATABASES[@]}"
    do
        echo "Creating > [$DB_NAME]"
        /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE $DB_NAME"
        for f in $MYSQL_DATA/$DB_NAME/*.*; do
            # do some stuff here with "$f"
            # remember to quote it or spaces may misbehave
            echo Importing $f
            /usr/bin/mysql -u root --password="$MYSQL_ROOT_PASSWORD" -D $DB_NAME < $f
        done

    done

fi

# for f in /Users/edaros/Work/HAMLm/ham.obe/*.*; do echo $f; done

# for f in "$DDD"; do echo $f; done