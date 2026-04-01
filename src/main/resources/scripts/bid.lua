-- KEYS[1]: highest_price 저장
-- KEYS[2]: highest_bidder_id 저장
-- KEYS[3]: state_version 저장
--
-- ARGV[1]: newBidAmount
-- ARGV[2]: bidderId
-- ARGV[3]: salesPrice
--
-- RETURN[1]: status ('SUCCESS' | 'SAME_BIDDER' | 'BID_TOO_LOW')
-- RETURN[2]: prev_bidder_id (없으면 '__nil__')
-- RETURN[3]: prev_price (없으면 '__nil__')
-- RETURN[4]: new_version (실패 시 -1)
-- RETURN[5]: prev_version

local NULL_TOKEN = '__nil__'

local prev_price = redis.call('get', KEYS[1])
local prev_bidder_id = redis.call('get', KEYS[2])
local prev_version = tonumber(redis.call('get', KEYS[3]) or '0')

local new_price = tonumber(ARGV[1])
local new_bidder_id = ARGV[2]
local sales_price = tonumber(ARGV[3])

-- 현재 최고 입찰자가 다시 자기 자신을 올리려는 경우
if prev_bidder_id and prev_bidder_id == new_bidder_id then
    return { 'SAME_BIDDER', NULL_TOKEN, NULL_TOKEN, '-1', tostring(prev_version) }
end

local floor_price = prev_price and tonumber(prev_price) or sales_price

if new_price > floor_price then
    local new_version = prev_version + 1

    redis.call('set', KEYS[1], tostring(new_price))
    redis.call('set', KEYS[2], new_bidder_id)
    redis.call('set', KEYS[3], tostring(new_version))

    return {
        'SUCCESS',
        prev_bidder_id or NULL_TOKEN,
        prev_price or NULL_TOKEN,
        tostring(new_version),
        tostring(prev_version)
    }
else
    return { 'BID_TOO_LOW', NULL_TOKEN, NULL_TOKEN, '-1', tostring(prev_version) }
end