
function runFilter(request,response){
    var today = new Date().toISOString();
    response.headers["access-control-allow-credentials"]="false";
    response.headers["access-control-allow-headers"]="authorization,content-type";
    response.headers["access-control-allow-methods"]="*";
    response.headers["access-control-allow-origin"]="*";
    response.statusCode = 200;
    var result ={
        request:request,
        response:response,
        continue:false
    };
    return result;
}