
To have some example you can check

* [PACT tests](pact.md)
* [NULL infrastructure tests](null.md)
* [UI tests](ui.md)

### Functions

This plugin has several roles

* Record api calls
* Allow modifications on recordings
* Generate a mock with recorded apis
* Create PACT-like tests to verify changes on remote apis and intercept them with js scripts
* Create NULL-infrastructure tests to test without...well...infrastructure and intercept them with js scripts
* Verify matching against json/xml schemas or template messages

### Phase

The filters involved acts in the following [phases](../lifecycle.md)

* replaying: pre-render
* recording: post-render

### JS Interception

The javascript filter calls are in the following form. In case of pact and null tests
the exception is directly logged

	function(request,response,expectedresponse){
		//HERE IS THE EDITABLE CODE
	}

To check the differences from templates you can add the following code.
This DOES NOT CHECK THE CONTENT. Any content matching is to be made by hand 
on the text contents (that must be unserialized beforehand)

	var diffEngine = new org.kendar.xml.DiffInferrer();
	diffEngine.diff(expectedresponse.responseText,response.responseText);