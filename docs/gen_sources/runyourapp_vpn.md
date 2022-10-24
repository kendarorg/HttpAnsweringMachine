
## Docker compose for vpn access<a id="dockervpn"></a>

First complete the common steps:

* [Prepare the HAM container](#preparehamcontainer)
* [Prepare the Application container](#prepareappcontainer)
* [Docker with proxy access](#dockerproxy) On the same machine (e.g. local docker or Docker Desktop)
    * [Write the compose](#dockerproxy_composer)
    * [Modify the configuration](#dockerproxy_setupconfig)

Install [OpenVpn Connect Client](https://openvpn.net/) on the client machine

Download the [connection script](https://raw.githubusercontent.com/kendarorg/HttpAnsweringMachine/main/docker/images/openvpn/mainuser.local.ovpn)
and change the following line with the address of the machine running docker.

<pre>
remote 127.0.0.1 3000 udp
</pre>

Install the .ovpn file in your OpenVPN connect

Add the following instance to the docker compose. There is a custom image ready for that :)

<pre>
  testapp.vpn:  # The application
    container_name: testapp.vpn # The DNS name of the instance
    environment:
      - DNS_HIJACK_SERVER=testapp.master  # Use HAM as final DNS server
      - ROOT_PWD=root
    privileged: true # Needed for DNS hijacking
    cap_add: # Needed for DNS hijacking
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:  
      - 127.0.0.1
    image: ham.openvpn/latest
    ports:
      - "3000:1194/udp" # To expose the OpenVpn port
    networks:
      - testappnet
    depends_on: # Wait for HAM to start
      - testapp.master
</pre>

Start the composer wait for the whole system to start and connect the client machine :)
Now you are completely inside the HAM cage!
