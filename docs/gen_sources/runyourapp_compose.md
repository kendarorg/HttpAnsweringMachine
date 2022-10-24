
## Docker compose for proxy access<a id="dockerproxy"></a>

### Docker compose<a id="dockerproxy_composer"></a>

Prepare a docker compose to connect to the application

<pre>
version: "2"
networks:
  testappnet:  # Setup a network for the system:
    driver: bridge
    ipam:
      config:
        - subnet: 172.25.7.0/24 # Define a subnet not already used
services:
  testapp.master:  # The HAM instance
    container_name: testapp.master # The DNS name of the instance
    privileged: true  # Needed for DNS hijacking
    environment:
      - ROOT_PWD=root
    cap_add:    # Needed for DNS hijacking
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:
      - 127.0.0.1
    image: testapp.master
    networks:
      - testappnet
    ports:
      - "5025:5025" # Ham debug port (optional)
      - "1080:1080" # Socks5 Proxy
      - "1081:1081" # Http/s Proxy
  testapp.app:  # The application
    container_name: www.testapp.com # The DNS name of the instance
    environment:
      - DNS_HIJACK_SERVER=testapp.master  # Use HAM as final DNS server
      - ROOT_PWD=root
    privileged: true # Needed for DNS hijacking
    cap_add: # Needed for DNS hijacking
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:  
      - 127.0.0.1
    image: testapp.app
    ports:
      - "8080:80" # To expose the application directly (optional)
    networks:
      - testappnet
    depends_on: # Wait for HAM to start
      - testapp.master
</pre>

### Adapt the configuration<a id="dockerproxy_setupconfig"></a>

To make the test "real" we will add a DNS and SSL entry to
the configuration.

In the section DNS of the "external.json" we tells that HAM should
intercept all requests to our application. They will then
be forwarded to the real instance

<pre>
[
  {
    "id": "dns",
    ...,
    "resolved": [
      ...,
      {
        "id": "123456",
        "dns": "www.testapp.com",
        "ip": "127.0.0.1"
      }
    ] ...
</pre>

In the section SSL of the "external.json" we tells that HAM should
build certificates for all testapp.com domains

<pre>
  {
    "id": "ssl",
    ...,
    "domains": [
      {
        "id": "123456",
        "address": "*.testapp.com"
      }
    ] ...

</pre>

### Run and connect!<a id="dockerproxy_run"></a>

Run the compose

<pre>
docker-compose up
</pre>

Setup the proxy on Firefox or Chrome (For the latter install [Switch Omega](https://chrome.google.com/webstore/detail/proxy-switchyomega/padekgcemlokbadohgkifijomclgjgif))
or for the application you are using to stimulate the application, like Postman
