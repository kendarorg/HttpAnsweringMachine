
The filter called is of type
function(request,response,expectedresponse){

	//YOUR DATA
}

To make diffs you can write in the code

var diffEngine = new org.kendar.xml.DiffInferrer();
diffEngine.diff(expectedresponse.responseText,response.responseText);