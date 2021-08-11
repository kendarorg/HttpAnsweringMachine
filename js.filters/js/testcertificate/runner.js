
function runFilter(request,response){
    try {
        var doContinue = true;
        if ("template" in request.query && "countrycode" in request.query) {
            if (request.query['template'] == "urn:template:qes:remote:oneshot" && request.query['countrycode'] == "IT") {


                var oneshotRequest = JSON.parse(request.requestText)['values'];
                var credentialId = "NONE";
                for (var i = 0; i < oneshotRequest.length; i++) {
                    if (oneshotRequest[i]['key'] == 'urn:field:credential.id') {
                        credentialId = oneshotRequest[i]['value'];
                        break;
                    }
                }
                var onehsotResponse = '{"errors":[],"certificateData":[{"key":"urn:output:subject:country","value":"IT"},'+
                    '{"key":"urn:output:subject:dnQualifier","value":"dnq"},'+
                    '{"key":"urn:output:subject:givenName","value":"Bianca"},'+
                    '{"key":"urn:output:subject:surname","value":"Rossi"},'+
                    '{"key":"urn:output:subject:serialNumber","value":"TINIT-RSSBNC64T70G677R"},'+
                    '{"key":"urn:output:subject:organization","value":"ACME"},'+
                    '{"key":"urn:output:subject:organizationIdentifier","value":"VATIT-01234567890"},'+
                    '{"key":"urn:output:subjectAlternativeNames.name.0","value":"xxyy@infocert.it"},'+
                    '{"key":"urn:output:credential.id","value":"'+credentialId+'"},'+
                    '{"key":"urn:output:subject:commonName","value":"BiancaRossi"},'+
                    '{"key":"urn:output:subject:organizationUnit1","value":"explosives"},'+
                    '{"key":"urn:output:subject:organizationUnit0","value":"coyotes"},'+
                    '{"key":"urn:output:certificateType","value":"QC"},'+
                    '{"key":"urn:output:evidence:identification","value":"mandatory"},'+
                    '{"key":"urn:output:evidence:registry","value":"mandatory"},'+
                    '{"key":"urn:output:subjectDn","value":"dnQualifier=dnq,CN=BiancaRossi,GIVENNAME=Bianca,SURNAME=Rossi,'+
                    'SN=TINIT-RSSBNC64T70G677R,C=IT,O=ACME,OU=explosives,OU=coyotes,OID=VATIT-01234567890"}]}';

                response.statusCode = 200;
                response.responseText = onehsotResponse;
                response.headers["Content-Type"] = "application/json";
                doContinue = false;
            }
        }
    }catch (e) {
        response.statusCode = 500;
        response.responseText = JSON.stringify(e);
        doContinue = false;
    }
    var result ={
        request:request,
        response:response,
        continue:doContinue
    };
    return result;
}