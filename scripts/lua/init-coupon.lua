local couponId = ARGV[1]
local stock = tonumber(ARGV[2])
local ttl = tonumber(ARGV[3])

local stockKey = "coupon:stock:" .. couponId
local issuedKey = "coupon:issued:" .. couponId

redis.call("DEL", stockKey)
redis.call("DEL", issuedKey)

redis.call("SET", stockKey, stock)
redis.call("EXPIRE", stockKey, ttl)

return true
