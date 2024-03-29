
## Simulate the calendar back-end<a id="manualtestcalendar_01"></a>

### Test the interaction between front and mock gateway

* Stop the application and restart!
* Delete the script and re-upload Sample.json
* Select all the calls to www.sample.test with the filter and delete them all

<img src="../images/remove_wwwsampletest.gif" width="500"/>

* Select all the calls to path /int/be.sample.test with the filter and delete them all

<img src="../images/remove_wwwsamplebe.gif" width="500"/>

* Stop the "be" application
* Stop the "gateway" application
* Download and save the script as NullGateway.json
* Play the Script with "Play"
* Do the navigation as you did while recording
* Everything will work as if be is up!

### Test the interaction between fe-gateway and mock be

* Stop all the applications and restart!
* Delete the script and re-upload Sample.json
* Select all the calls to www.sample.test with the filter and delete them all

<img src="../images/remove_wwwsampletest.gif" width="500"/>

* Select all the calls to path /int/gateway.sample.test with the filter and delete them all

<img src="../images/remove_wwwsamplegateway.gif" width="500"/>

* Stop the "be" application
* Download and save the script as NullBe.json
* Play the Script clicking on "Play"
* Do the navigation as you did while recording
* Everything will work as if gateway is up!
