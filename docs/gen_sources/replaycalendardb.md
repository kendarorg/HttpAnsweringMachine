
## Replay everything!

* "Download" the recording as "FullDb.json"
* You can now delete all the request with at least one of the following, leaving only db calls
  * Host: www.sample.test
  * Path: /int/
* "Download" the recording as "DbOnly.json"
* Now stop the H2 database AND the BE application
* Start replaying the recording with "Play" when started
* Restart the BE with the be.bat/sh
* When the initialisation is complete go again of www.sample.test and redo all the steps (John Doe etc)
* Everything will work :D