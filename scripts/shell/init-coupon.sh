#!/bin/bash

COUPON_ID=4001
COUPON_STOCK=1000
TTL=720
SCRIPT_PATH=/scripts/init-coupon.lua

docker exec -i redis \
  redis-cli -a root --eval $SCRIPT_PATH , $COUPON_ID $COUPON_STOCK $TTL
