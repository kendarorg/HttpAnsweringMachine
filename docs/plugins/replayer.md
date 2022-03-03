
The filter called is of type
function(request,response,expectedresponse){

	//YOUR DATA
}

To make diffs you can

var diffEngine = new org.kendar.xml.DiffInferrer();
var template = {id:"test",data:"data"};
var message = {id:"test"}
try{
  diffEngine.diff(JSON.stringify(template),JSON.stringify(message));
}catch(error){
  response.responseText = JSON.stringify(error);
}