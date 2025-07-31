-- KEYS[1]: 최고가(highest_price)를 저장할 키 (ex bids:{feedId}:highest_price)
-- KEYS[2]: 최고 입찰자 ID(highest_bidder_id)를 저장할 키 (ex bids:{feedId}:highest_bidder_id)
-- ARGV[1]: 새로 들어온 입찰가 (newBidAmount)
-- ARGV[2]: 입찰자 ID (bidderId)

-- 현재 저장된 최고가와 최고 입찰자 정보
local prev_price = redis.call('get', KEYS[1])
local prev_bidder_id = redis.call('get', KEYS[2])

-- 새로운 입찰가와 입찰자 정보
local new_price = tonumber(ARGV[1])
local new_bidder_id = ARGV[2]
local sales_price = tonumber(ARGV[3])

-- 이전에 입찰한 사람이 현재 최고가로 다시 입찰하려는 경우
if prev_bidder_id and prev_bidder_id == new_bidder_id then
    return { 'SAME_BIDDER', '0', '0' }
end

local floor_price = prev_price and tonumber(prev_price) or sales_price

-- 새로운 입찰가가 비교 기준 가격보다 높은 경우
if new_price > floor_price then
    -- 새로운 정보로 갱신
    redis.call('set', KEYS[1], new_price)
    redis.call('set', KEYS[2], new_bidder_id)

    -- 성공 상태와 '이전' 입찰자 정보를 반환
    -- 만약 첫 입찰이라 이전 정보가 없으면, 0 을 반환
    return { 'SUCCESS', prev_bidder_id or '0', prev_price or '0' }
else
    -- 입찰가가 현재 최고가 또는 시작가보다 낮거나 같은 경우
    return { 'BID_TOO_LOW', '0', '0' }
end