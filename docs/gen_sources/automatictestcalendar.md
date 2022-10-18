
## Automatically test and verify backend<a id="automaticcalendar_01"></a>

### Run the interaction with back-end (stateless) and test the GATEWAY in isolation

* Stop the application and restart!
* Delete the script and re-upload Sample.json
* Stop the "fa" application
* Stop the "gateway" application
* Select all the calls to path /int/be.sample.test with the filter and set them as "Stimulated"

<img src="../images/remove_wwwsamplebe.gif" width="300"/>

* Select all the calls to path /int/gateway.sample.test with the filter and set them as "Stimulated"

<img src="../images/remove_wwwsamplegateway.gif" width="300"/>

* Thist a part of result:

<img src="../images/null_gateway_prepare.gif" width="300"/>

* Run the "Null test"
* Check the [results](http://www.local.test/plugins/recording/results.html)!

### Verify the content structure

* Select all the "Stimulator" calls (use "true" as filter on the "Stimulator" column)
* Use the global Edit JS and set for all call the verification script in the "post" part

<pre>
    var diffEngine = new org.kendar.xml.DiffInferrer();
    diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());
    if(expectedresponse.getStatusCode()!=response.getStatusCode()){
        throw "Expected status code "+expectedresponse.getStatusCode()+" but received "+response.getStatusCode();
    }
</pre>

<img src="../images/verify_structure_script.gif" height="300"/>

* Re run the "Null test"! And be happy
* Check the [results](http://www.local.test/plugins/recording/results.html)!
* Download and save the script as NullAutoTestGateway.json

### Fail the test! To verify it's real

* Find the response to a front-end call. A good example can be the first get call to the "/int/gateway.sample.test/api/v1/employees" after the POST inserting the new employee

<img src="../images/edit_the_get.gif" width="300"/>

* Click on the Edit button on the list and open the Request/Response editor

<img src="../images/edit_response_data.gif" width="300"/>

* Replace then the response data with something like this. Adding the field "unexpected"

<pre>
[{"id":1,"name":"John","role":"Doe","unexpected":"field"}]
</pre>

* Run the test again
* Check the FAILED results!

<img src="../images/unexpected_field.gif" width="300"/>


