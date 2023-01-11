## Null basics

This kind of test is ideal for

* Tests in docker container
* Services regression tests

These are automated tests, just select the "Stimulator". All the selected requests will be
reproduced. And you can even verify the data. 

An [example](../../generated/automatictestcalendar.md)

Simply push "Play" and you will act as "stimulator" for the application you are verifying

* If you have recorded all the calls made by a UI you could redo all the actions you recorded
* If you are working with a db and http calls everything recorded will be returned

For tentatives just change the headers/contents/query params to find how your application reacts