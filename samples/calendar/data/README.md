


12:
POST	gateway.sample.test	/api/v1/employees	

var diffEngine = new org.kendar.xml.DiffInferrer();
diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());

var realResponse = JSON.parse(response.getResponseText());
var realId = realResponse['id'];
var cache = new org.kendar.replayer.Cache();
cache.set(runid,'eid',realId);

27:
POST	gateway.sample.test	/api/v1/appointments/##eid##

var diffEngine = new org.kendar.xml.DiffInferrer();
diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());

var realResponse = JSON.parse(response.getResponseText());
var realId = realResponse['id'];
var cache = new org.kendar.replayer.Cache();
cache.set(runid,'aid',realId);