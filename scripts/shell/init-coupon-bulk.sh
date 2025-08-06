#!/bin/bash

START_ID=4001
END_ID=5000
COUPON_STOCK=1000
TTL=7200
SCRIPT_PATH=/scripts/init-coupon.lua

for ((id=START_ID; id<=END_ID; id++)); do
  docker exec -i redis \
    redis-cli -a root --eval $SCRIPT_PATH , "$id" $COUPON_STOCK $TTL
done
