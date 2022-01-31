
function runFilter(request,response){
    var today = new Date().toISOString();
    response.responseText = '{"value":"This is a calculated javascript response","date":"'+today+'"}';
    response.headers["Content-Type"]="application/json";
    response.statusCode = 200;
    request.headers["Host"]="test.com";
    return {
        request:request,
        response:response,
        continue:false
    };
}