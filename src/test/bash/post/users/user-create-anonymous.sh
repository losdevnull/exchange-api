# Adds a user
source `dirname $0`/../../functions.sh POST $*

curl $copts -X POST -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{
  "password": "'$EXCHANGE_PW'",
  "admin": false,
  "email": "'$EXCHANGE_EMAIL'"
}' $EXCHANGE_URL_ROOT/v1/orgs/public/users/$EXCHANGE_USER | $parse
