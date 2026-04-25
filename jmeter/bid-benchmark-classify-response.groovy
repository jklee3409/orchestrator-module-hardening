import groovy.json.JsonSlurper

def body = prev.getResponseDataAsString()
if (!body) {
    prev.setSuccessful(false)
    prev.setResponseMessage('Empty response body')
    return
}

try {
    def parsed = new JsonSlurper().parseText(body)
    def status = parsed?.statusCode?.toString() ?: ''
    if (!status) {
        prev.setSuccessful(false)
        prev.setResponseMessage('Missing application statusCode')
        return
    }

    vars.put('appStatusCode', status)
    prev.setResponseCode(status)
    prev.setSampleLabel(prev.getSampleLabel() + ' [' + (vars.get('benchmarkCase') ?: 'unknown') + '] [app=' + status + ']')

    def expectedStatuses = ((vars.get('expectedAppStatuses') ?: '200').split(',')*.trim()).findAll { it } as Set
    if (!expectedStatuses.contains(status)) {
        prev.setSuccessful(false)
        prev.setResponseMessage('Unexpected application statusCode: ' + status)
    }
} catch (Exception e) {
    prev.setSuccessful(false)
    prev.setResponseMessage('Response parse failure: ' + e.getMessage())
}
