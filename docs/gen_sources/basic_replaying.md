
### Replaying

#### Basisc

When replaying the request/response resources are catalogued as "static". Or 
resources that never changes during the replay session. And Dynamic. 

The dynamic are executed only -once- during the entire session and a tracker 
is kept to avoid double calls

#### The types

Essentially the replayer has 2 ways to replay

* Play: You should invoke the "front" service and HAM will respond to the invocations. You are responsible for running the "initiator calls"
* Play stimulator: Start to run the stimulator calls (and to respond if not already pressed the play)

At the end of a test the result is shown in a specific page/API

#### Extensions

During the replay is possible to associate a PRE and POST Javascript function to modify and verify
the request.

A storage, local to the specific run, is available. And automatic substitutions can be set on path,
headers, query params and bodies