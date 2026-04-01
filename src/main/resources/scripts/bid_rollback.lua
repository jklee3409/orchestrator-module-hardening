-- KEYS[1]: highest_price 저장 키
-- KEYS[2]: highest_bidder_id 저장 키
-- KEYS[3]: state_version 저장 키
--
-- ARGV[1]: expectedAppliedVersion
-- ARGV[2]: previousBidderId or __nil__
-- ARGV[3]: previousBidAmount or __nil__
-- ARGV[4]: previousVersion

local NULL_TOKEN = '__nil__'

local current_version = tonumber(redis.call('get', KEYS[3]) or '-1')
local expected_version = tonumber(ARGV[1])
local previous_bidder_id = ARGV[2]
local previous_bid_amount = ARGV[3]
local previous_version = tonumber(ARGV[4] or '0')

-- 이미 더 새로운 입찰이 들어왔다면 롤백하지 않음
if current_version ~= expected_version then
    return 0
end

-- 첫 입찰 실패처럼 "이전 상태가 없음"이면 키 자체를 삭제
if previous_bidder_id == NULL_TOKEN or previous_bid_amount == NULL_TOKEN then
    redis.call('del', KEYS[1], KEYS[2], KEYS[3])
    return 1
end

redis.call('set', KEYS[1], previous_bid_amount)
redis.call('set', KEYS[2], previous_bidder_id)

if previous_version <= 0 then
    redis.call('del', KEYS[3])
else
    redis.call('set', KEYS[3], tostring(previous_version))
end

return 1