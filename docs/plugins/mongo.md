## Integration with mongodb 

A new section of configuration is prepared for mongodb


### Config

The exposed port is the one that will be used by the application to monitor.

* The database will be mongodb://[HAM IP]:27097
* The real mongodb will be the one in connectionstring


At the moment you should specify login, password and auth method on the real
mongodb connection string. The connection to the ham proxy is without password 
and login

<pre>
{
  "id": "mongo",
  "active": true,
  "proxies": [
    {
      "id": "1",
      "active": true,
      "exposedPort": 27097,
      "remote": {
        "connectionString": "mongodb://realserver:27017",
        "login": "",
        "password": ""
      }
    }
  ]
}
</pre>

### Internals

All the calls to the exposedPort will result in a POST call to the http api
and can than be recorded and reproduced. The type of recording will be "mongodb"

http://localhost/mongo/[EXPOSED_PORT]/[DB]