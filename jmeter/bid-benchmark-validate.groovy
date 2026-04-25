import org.apache.jmeter.services.FileServer

def errors = []

def requirePositiveInt = { String name ->
    def raw = (vars.get(name) ?: '').trim()
    if (!raw) {
        errors << "${name} is required"
        return null
    }
    try {
        def value = Integer.parseInt(raw)
        if (value <= 0) {
            errors << "${name} must be greater than zero: ${raw}"
            return null
        }
        return value
    } catch (NumberFormatException ignored) {
        errors << "${name} must be an integer: ${raw}"
        return null
    }
}

def requirePositiveLong = { String name ->
    def raw = (vars.get(name) ?: '').trim()
    if (!raw) {
        errors << "${name} is required"
        return null
    }
    try {
        def value = Long.parseLong(raw)
        if (value <= 0L) {
            errors << "${name} must be greater than zero: ${raw}"
            return null
        }
        return value
    } catch (NumberFormatException ignored) {
        errors << "${name} must be a long: ${raw}"
        return null
    }
}

def workloadType = (vars.get('workloadType') ?: '').trim()
if (!(workloadType in ['winner', 'loser'])) {
    errors << "workloadType must be winner or loser: ${workloadType}"
}

def bidderCount = requirePositiveInt('bidderCount')
def warmupThreads = requirePositiveInt('warmupThreads')
def measureThreads = requirePositiveInt('measureThreads')
def warmupLoops = requirePositiveInt('warmupLoops')
def measureLoops = requirePositiveInt('measureLoops')

def bidderInitialPay = requirePositiveLong('bidderInitialPay')
def winnerFeedSalesPrice = requirePositiveLong('winnerFeedSalesPrice')
def winnerBidStartAmount = requirePositiveLong('winnerBidStartAmount')
def bidIncrement = requirePositiveLong('bidIncrement')
def fixedBidAmount = requirePositiveLong('fixedBidAmount')
def loserSeedBidAmount = requirePositiveLong('loserSeedBidAmount')

if (warmupLoops != null && warmupLoops != 1) {
    errors << "warmupLoops must stay 1 for one-request-per-user hot path comparison"
}
if (measureLoops != null && measureLoops != 1) {
    errors << "measureLoops must stay 1 for one-request-per-user hot path comparison"
}

if (bidderCount != null && warmupThreads != null && warmupThreads > bidderCount) {
    errors << "warmupThreads must be <= bidderCount (${bidderCount})"
}
if (bidderCount != null && measureThreads != null && measureThreads > bidderCount) {
    errors << "measureThreads must be <= bidderCount (${bidderCount})"
}

if (winnerBidStartAmount != null && winnerBidStartAmount % 100L != 0L) {
    errors << "winnerBidStartAmount must be divisible by 100"
}
if (bidIncrement != null && bidIncrement % 100L != 0L) {
    errors << "bidIncrement must be divisible by 100"
}
if (fixedBidAmount != null && fixedBidAmount % 100L != 0L) {
    errors << "fixedBidAmount must be divisible by 100"
}
if (winnerBidStartAmount != null && winnerFeedSalesPrice != null && winnerBidStartAmount <= winnerFeedSalesPrice) {
    errors << "winnerBidStartAmount must be greater than winnerFeedSalesPrice"
}
if (fixedBidAmount != null && loserSeedBidAmount != null && fixedBidAmount >= loserSeedBidAmount) {
    errors << "fixedBidAmount must stay below loserSeedBidAmount for deterministic loser-heavy runs"
}

if (measureThreads != null && winnerBidStartAmount != null && bidIncrement != null && bidderInitialPay != null) {
    def maxMeasuredBid = winnerBidStartAmount + ((measureThreads - 1L) * bidIncrement)
    if (maxMeasuredBid > bidderInitialPay) {
        errors << "bidderInitialPay (${bidderInitialPay}) must cover maxMeasuredBid (${maxMeasuredBid})"
    }
}

def feedIdNames = [
        'redisWarmupWinnerFeedId',
        'redisWarmupLoserFeedId',
        'redisWinnerFeedId',
        'redisLoserFeedId',
        'dbLockWarmupWinnerFeedId',
        'dbLockWarmupLoserFeedId',
        'dbLockWinnerFeedId',
        'dbLockLoserFeedId'
]
feedIdNames.each { requirePositiveLong(it) }

[
        redis : ['redisWarmupWinnerFeedId', 'redisWarmupLoserFeedId', 'redisWinnerFeedId', 'redisLoserFeedId'],
        dbLock: ['dbLockWarmupWinnerFeedId', 'dbLockWarmupLoserFeedId', 'dbLockWinnerFeedId', 'dbLockLoserFeedId']
].each { impl, names ->
    def values = names.collect { (vars.get(it) ?: '').trim() }
    if (values.toSet().size() != values.size()) {
        errors << "${impl} feed ids must all be distinct to isolate warm-up and measured runs"
    }
}

def baseDir = new File(FileServer.getFileServer().getBaseDir())
['redisUsersCsv', 'dbLockUsersCsv'].each { String name ->
    def fileName = (vars.get(name) ?: '').trim()
    if (!fileName) {
        errors << "${name} is required"
        return
    }
    def csvFile = new File(baseDir, fileName)
    if (!csvFile.isFile()) {
        errors << "${name} does not exist under ${baseDir}: ${fileName}"
        return
    }
    def rowCount = Math.max(csvFile.readLines('UTF-8').size() - 1, 0)
    if (bidderCount != null && rowCount < bidderCount) {
        errors << "${name} has ${rowCount} bidder rows but bidderCount is ${bidderCount}"
    }
}

if (errors) {
    throw new IllegalStateException("Bid benchmark config validation failed:\n - " + errors.join('\n - '))
}

log.info(
        '[BidBenchmark] Config validated. workloadType={}, warmupThreads={}, measureThreads={}, bidderCount={}',
        workloadType,
        warmupThreads,
        measureThreads,
        bidderCount
)
