-- KEYS[1] = coupon:stock:{couponId}
-- KEYS[2] = coupon:issued:{couponId}
-- ARGV[1] = customerId
-- ARGV[2] = TTL seconds

-- 사용자의 쿠폰 보유 여부 확인
if redis.call("SISMEMBER", KEYS[2], ARGV[1]) == 1 then
    return -2
end

-- 쿠폰 남은 수량 확인
local stock = redis.call("GET", KEYS[1])
if not stock then
    return -1
end

-- 쿠폰 차감
stock = redis.call("DECR", KEYS[1])
if tonumber(stock) < 0 then return -3
end

-- 사용자에게 쿠폰 발급
if redis.call("SADD", KEYS[2], ARGV[1]) == 1 then
    redis.call("EXPIRE", KEYS[2], ARGV[2])
end
return 1
