
### Request address rewriters

It is possible to apply a sort of Apache "mod_rewrite" to the system

For example a request to https://www.sample.test can be redirected to 
http://localhost:8080/sample. HAM is taking care of checking that the 
target server is up and running. 

If it's not the case and the server does not run it tries to call https://www.sample.test