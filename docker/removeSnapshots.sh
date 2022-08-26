#!/bin/sh

# curl -L -o /usr/bin/jq.exe https://github.com/stedolan/jq/releases/latest/download/jq-win64.exe
# ./removeSnapshots.sh login pwd snapshot
# ./removeSnapshots.sh login pwd v3.0.8-SNAPSHOT



USERNAME=$1
PASSWORD=$2
ORGANIZATION="kendarorg"
TAG=$3

login_data() {
cat <<EOF
{
  "username": "$USERNAME",
  "password": "$PASSWORD"
}
EOF
}

TOKEN=`curl -s -H "Content-Type: application/json" -X POST -d "$(login_data)" "https://hub.docker.com/v2/users/login/" | jq -r .token`



removetag () {
  IMAGE=$1
  curl "https://hub.docker.com/v2/repositories/${ORGANIZATION}/${IMAGE}/tags/${TAG}/" \
  -X DELETE \
  -H "Authorization: JWT ${TOKEN}"
}

removetag "ham.base"
removetag "ham.client"
removetag "ham.apache"
removetag "ham.apache.php8"
removetag "ham.master"
removetag "ham.openvpn"
removetag "ham.mysql"

removetag "ham.sampleapp.be"
removetag "ham.sampleapp.fe"
removetag "ham.sampleapp.gateway"
removetag "ham.sampleapp.multi"


removetag "ham.quotes.master"
removetag "ham.quotes.core"


