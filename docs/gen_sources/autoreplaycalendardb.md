
### Replay everything!

* "Download" the recording as "FullDb.json"
* You can now delete all the request with at least one of the following, leaving only db calls
  * Host: www.sample.test
  * Path: /int/gateway
* "Download" the recording as "BeOnly.json"
* Stop frontend, be and gateway
* Select all the /int/be calls, check all of them and mark them as stimulator (and save!)

* Select all the "Stimulator" calls (use "true" as filter on the "Stimulator" column)
* Use the global Edit JS and set for all call the verification script in the "post" part. The last part is because running in isolation, without generated values we want ALL data to be the same

<pre>
    var diffEngine = new org.kendar.xml.DiffInferrer();
    diffEngine.diff(expectedresponse.getResponseText(),response.getResponseText());
    if(expectedresponse.getStatusCode()!=response.getStatusCode()){
        throw "Expected status code "+expectedresponse.getStatusCode()+" but received "+response.getStatusCode();
    }

    if( (expectedresponse.getResponseText()+"")!=(""+response.getResponseText())){
   	  throw "Different responses";
    }
</pre>

<img src="../images/verify_structure_script.gif" height="300"/>

* "Download" the recording as "BeNullTest.json"
* Start replaying the recording with "Play", when started
* Restart the BE with the be.bat/sh
* When the BE initialization is completed
* STart the "Play Stimulator test"
* Everything will work :D And you could find the correct result 

### Fail to be sure

* Shutdown the BE
* Select the first get after the post on the /int/be (after the employee insertion)

<img src="../images/dbproxy_second_get_after_post.gif" height="300"/>

* Change the response to something "wrong"

<img src="../images/dbproxy_second_fake.gif" height="300"/>

* Again
* Start replaying the recording with "Play", when started
* Restart the BE with the be.bat/sh
* When the BE initialization is completed
* STart the "Play Stimulator test"
* When the test is finished you can find the test... failed!

  <img src="../images/dbproxy_second_fail.gif" height="300"/>
