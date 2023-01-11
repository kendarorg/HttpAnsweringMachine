
With the standard setup the plugin can be found on [http://www.local.test/plugins/replayer](http://www.local.test/plugins/replayer)

### TLDR

* [Recording](recording.md)
* [Simple Replay](simple.md) For UI and Manual verification
* [Null Infrastructure](null.md) For tests in isolation, optionally to verify the data
* [PACT](pact.md) Verify the data returned by external systems is correct

### Functions

This plugin has several roles

* Record api/DB calls
* Allow modifications on recordings
* Generate a mock with recorded apis
* Create PACT-like tests to verify changes on remote apis and intercept them with js scripts
* Create NULL-infrastructure tests to test without...well...infrastructure and intercept them with js scripts
* Verify matching against json/xml schemas or template messages
* Execute arbitrary scripts (with variable shared between all execution) during the replaying

### Phase

The filters involved acts in the following [phases](../lifecycle.md)

* replaying: pre-render
  * Run pre scripts
  * Handle internal proxying
  * Execute the request against the recorded data
  * Run post scripts
* recording: post-render

### JS Interception

The javascript filter calls are in the following form. In case of pact and null tests
the exception is directly logged. Notice that for the simple replaying no script is
run AT ALL

	function(runid,request,response,expectedresponse){
		//HERE IS THE EDITABLE CODE
	}

The function never returns. The modifications made to the request/response/expected
will affect directly the objects used further in the execution outside JS

To check the differences from templates you can add the following code.
This DOES NOT CHECK THE CONTENT. Any content matching is to be made by hand 
on the text contents (that must be unserialized beforehand)

	var diffEngine = new org.kendar.xml.DiffInferrer();
	diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());

A cache for string variables is available, null means not present. This
can be used to change values in response/request according to the real
context

	var cache = new org.kendar.replayer.Cache();
	cache.get(runid,"key");
	cache.set(runid,"key","value");

The variables are automatically replaced inside the request and response 
and set with the following inside the whole content of the Request object

    ##varname##

A simple idea to verify the contents

    if(expectedresponse.getResponseText()!=response.getResponseText()) throw "ERROR!";