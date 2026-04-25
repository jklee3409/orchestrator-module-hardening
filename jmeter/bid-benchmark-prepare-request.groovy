def parts = (Parameters ?: '').split(',', -1)*.trim()
if (parts.size() != 4) {
    throw new IllegalStateException('Expected parameters: impl,phase,winnerFeedId,loserFeedId')
}

def impl = parts[0]
def phase = parts[1]
def winnerFeedId = parts[2]
def loserFeedId = parts[3]

def email = (vars.get('email') ?: '').trim()
if (!email) {
    throw new IllegalStateException('CSV email is required for bypass bid benchmarking')
}

def workloadType = (vars.get('workloadType') ?: '').trim()
if (!(workloadType in ['winner', 'loser'])) {
    throw new IllegalStateException("workloadType must be winner or loser: ${workloadType}")
}

def feedId = workloadType == 'winner' ? winnerFeedId : loserFeedId
if (!feedId) {
    throw new IllegalStateException("Missing feed id for ${impl}-${phase}-${workloadType}")
}

long seq = (vars.get('globalBidSeq') ?: '1') as long
long winnerBase = (vars.get('winnerBidStartAmount') ?: '0') as long
long step = (vars.get('bidIncrement') ?: '100') as long
long fixed = (vars.get('fixedBidAmount') ?: '0') as long
long bidAmount = workloadType == 'winner'
        ? winnerBase + ((seq - 1L) * step)
        : fixed

vars.put('authToken', email)
vars.put('feedId', feedId)
vars.put('bidAmount', String.valueOf(bidAmount))
vars.put('bidPath', impl == 'redis' ? '/orchestrator/bid' : '/orchestrator/bid/db-lock')
vars.put('benchmarkCase', "${impl}-${phase}-${workloadType}")
vars.put('expectedAppStatuses', workloadType == 'winner' ? '200,30012' : '30012')
