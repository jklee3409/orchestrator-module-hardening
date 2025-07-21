-- KEYS[1]: 최고가(highest_price)를 저장할 키 (ex bids:{feedId}:highest_price)
-- KEYS[2]: 최고 입찰자 ID(highest_bidder_id)를 저장할 키 (ex bids:{feedId}:highest_bidder_id)
-- ARGV[1]: 새로 들어온 입찰가 (newBidAmount)
-- ARGV[2]: 입찰자 ID (bidderId)

local currentHighestBidder = redis.call('GET', KEYS[2])
if currentHighestBidder and currentHighestBidder == ARGV[2] then
    -- 현재 최고 입찰자가 새 입찰자와 동일한 경우, 최고가를 업데이트하지 않음
    return 'SAME_BIDDER'
end

local currentHighestPrice = redis.call('GET', KEYS[1])
local newBidAmount = tonumber(ARGV[1])

if not currentHighestPrice or newBidAmount > tonumber(currentHighestPrice) then
    -- 새 입찰가가 현재 최고가보다 높은 경우
    redis.call('SET', KEYS[1], newBidAmount)
    redis.call('SET', KEYS[2], ARGV[2])
    return 'SUCCESS'
else
    -- 새 입찰가가 현재 최고가보다 낮거나 같은 경우
    return 'BID_TOO_LOW'
end