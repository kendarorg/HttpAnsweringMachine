
## Automatically test and verify backend<a id="automaticcalendar_01"></a>

### Run the interaction with back-end (stateless) and test the GATEWAY in isolation

* Stop the application and restart!
* Delete the script and re-upload Sample.json
* Stop the "fe" application
* Stop the "be" application

* Select all the calls to path /int/gateway.sample.test with the filter and set them as "Stimulator". These will be run automatically.

<img src="../images/stimulator_wwwsamplegateway.gif" width="500"/>

* Run the test
* Check the results on the results tab!


<img src="../images/stimulator_result1.gif" width="500"/>

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

* Replay And be happy
* Check the [results](http://www.local.test/plugins/recording/results.html)!
* Download and save the script as NullAutoTestGateway.json

### Fail the test! To verify it's real

* Find the response to a front-end call. A good example can be the first get call to the "/int/gateway.sample.test/api/v1/employees" after the POST inserting the new employee

<img src="../images/edit_the_get.gif" width="500"/>

* Click on the Edit button on the list and open the Request/Response editor

<img src="../images/edit_response_data.gif" width="500"/>

* Replace then the response data with something like this. Adding the field "unexpected"

<pre>
[{"id":1,"name":"John","role":"Doe","unexpected":"field"}]
</pre>

* Run the test again
* Check the FAILED results! Notice that the field "unexpected" is the one we added!

<img src="../images/unexpected_field.gif" width="500"/>


