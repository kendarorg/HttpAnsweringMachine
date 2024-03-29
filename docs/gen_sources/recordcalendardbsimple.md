
## Record some interaction<a id="recordcalendar_01"></a>

* Now you can start the application on the command line :) and wait for its readiness
* Stop it
* Run the benogen.bat/sh but do not press a key to continue!

You can now check the configuration

* Going on [ham proxyes](http://www.local.test/proxy/index.html) you can verify that all proxies are ok if they don't work just "Refresh Status"

<img alt="Ham Proxyes" src="../images/ham_proxies.gif" width="500"/>

* And a db proxy for be

<img alt="Ham Proxyes" src="../images/dbproxy.gif" width="500"/>

* Then you can create a recording on the [recording page](http://www.local.test/plugins/recording)

<img alt="Create recording" src="../images/create_recording.gif" width="500"/>

* Set the recording with the simplified recording! This will record only "most important" stuffs. What can't be simulated will be recorded

<img alt="Create recording" src="../images/dbmod_01.gif" width="500"/>

* Once you create the recording you can start recording! <b>CHECKING THE "Record db calls" and the "Use simulated engine"</b>

<img alt="Start recording" src="../images/dbproxy_sample03_recorddb.gif" width="700"/>

* Now you can press the key on the benogen! And start the application
* Wait for its start

* Go then on the [application](http://www.sample.test) and do some interaction
  * Add an Employee "John Doe"
  * Go on Appointements
  * Add an Appointment with "Doctor" as description
  * Change the state of the appointament till it shows "Confirmed"
  * Delete the appointment
  * Back on employee, delete the employee too
* And stop the recording!
* Now you will se all the calls on the just created recording
* First the hibernate initialization phase

<img alt="Start recording" src="../images/dbproxy_sample04_hibernateinit.gif" width="500"/>

* ..or see how an employee is created. 
  * First a post to the gateway
  * Forwarded to the be
  * Then the be opens the connections and run the query

<img alt="Start recording" src="../images/dbmod_03.gif.gif" width="500"/>

* You can even see the details of the queries there are many visualisations available
  * JSON: the native format of Janus-Jdbc (editable)
  * Component: the "easy editable visualisation", when available
  * Tree: the structure of the json message (that include the data types)

<img alt="Start recording" src="../images/dbproxy_sample06_exampleview.gif" width="500"/>

Analyzing the Json we identify a prepared statement with one Long parameter with 
set the index 1 (the first to set JDBC prepared statement parameters)

<pre>
{
	"command": {
		"sql": "
            select 
              employee0_.id as id1_1_0_, 
              employee0_.name as name2_1_0_, 
              employee0_.role as role3_1_0_ 
            from 
              employee employee0_ 
            where employee0_.id=?",
		":sql:": "java.lang.String",
		"parameters": [
			{
				"_": {
					"columnindex": 1,
					":columnindex:": "java.lang.Integer",
					"value": 0,
					":value:": "java.lang.Long"
				},
				":_:": "org.kendar.janus.cmd.preparedstatement.parameters.LongParameter"
			}
		],
		":parameters:": "java.util.ArrayList"
	},
	":command:": "org.kendar.janus.cmd.preparedstatement.PreparedStatementExecuteQuery"
}
</pre>



