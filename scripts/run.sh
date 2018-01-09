#!/bin/sh
# Script that runs this module against the folio/testing box
# We run this module locally and calls ther other modules inside the box
set -x
OKAPIURL=http://localhost:9130

java -jar ../target/mod-codex-inventory-fat.jar >>fat.log 2>&1 &
PID=$!
sleep 3
tail -20 fat.log
cat > login.json <<END
{
  "username" : "diku_admin",
  "password" : "admin"
} 
END
curl -s -D login.res -o login.txt -HContent-Type:application/json -HX-Okapi-Tenant:diku -XPOST -d@login.json http://localhost:9130/authn/login
TOK=`grep -i x-okapi-token login.res`
H1=$TOK
H2=X-Okapi-Tenant:diku
H3=X-Okapi-URL:$OKAPIURL

# id
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=id%3De54b1f4d-7d05-4b1a-9368-3c36b75d8ac6'

# title
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=title%3Dwater'

# contributor
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=contributor="Sosa,+Omar"'

# publisher
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=publisher%3D"Otá+Records,+"'

# identifier
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=identifier%3D"ocn968777846"'
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=identifier%3D6316800312'
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=identifier%3D"OTA-1031+Otá+Records"'
# curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=identifier%3D%3D"*\"value\":+\"ocn968777846\",+\"identifierTypeId\":+\"5d164f4b-0b15-4e42-ae75-cfcf85318ad9\"*"'
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=identifier%3D%2Ftype%3DISBN+"6316800312"'
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=identifier%3D%2Ftype%3Disbn+"6316800312"'

# resourceType
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=resourceType%3Dmusic+and+title%3Dwater'

curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=a+and('
curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?query=abe%3D9'

#curl "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances?limit=20'
#sleep 1
#curl -D- "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances/1234'
#curl -D- "-H$H1" -H$H2 -H$H3 'http://localhost:8081/codex-instances/106ce3b4-433a-406c-b584-e5a6242258f1'

kill -9 $PID
