## Globals

Assuming a remote network of class 10.* these are the settings.
To check the reality follow the instructions on the specific
vpn

### Startup from docker compose

  vpn.local.self:
    container_name: vpn
    build: './vpn/'
    networks:
      - selfnet
    privileged: true
    cap_add:
      - NET_ADMIN
      - DAC_READ_SEARCH
    security_opt:
      - label:disable
	
### Routing to the Company VPN Cliente(CVC) from the OVP server

	iptables -t nat -A POSTROUTING -s 10.0.0.0/8 -o eth0 -j MASQUERADE
	
### Routing to the CVC from the docker clients

Should route to the CVC client network

	route add -net 10.0.0.0 netmask 255.0.0.0 gw {{CVC client machine}}
	
## For the various CVC clients

### Openconnect

Setup the routing with tun

	ip tuntap add name tun0 mode tun
	iptables -t nat -A POSTROUTING -o tun0 -j MASQUERADE
	iptables -A FORWARD -i eth0 -o tun0 -j ACCEPT
	iptables -A FORWARD -o tun0 -j ACCEPT
	iptables -A FORWARD -i tun0 -m conntrack --ctstate ESTABLISHED,RELATED   -j ACCEPT
	iptables -A INPUT -i tun0 -j ACCEPT
	iptables -L -v -n

Connect with something like this. Assuming 

* User: USERNAME
* Protocol: gp
* CSD Wrapper: /usr/libexec/openconnect/hipreport.sh
* User Group: gateway
* Gateway address:https://gp.MYCOMPANY.COM 

You can connect to the CVC and i can issue the following command (ask your IT for the exact one)

NOTICE THE TUN that will be used by iptables

	/usr/sbin/openconnect -i tun0 -u "USERNAME" --mtu=1422 \
		--protocol=gp \
		--csd-wrapper=/usr/libexec/openconnect/hipreport.sh \
		--usergroup=gateway \
		https://gp.MYCOMPANY.COM
		
To retrieve the routing to add to the other machines, the networks are 
the ones with tun0 gateway

	$> netstat -rn
	
	Routing tables
	Internet:
	Destination        Gateway            Flags        Netif Expire
	default            192.168.1.1        UGScg          en0       
	default            link#18            UCSIg         ppp0       
	2.47.45.190/32     tun0               USc           tun0       
	2.139.166.167/32   ppp0               USc           ppp0    
		
### Forti client

Setup the routing

	iptables --table nat --append POSTROUTING --out-interface ppp0 -j MASQUERADE
	iptables --append FORWARD --in-interface eth0 -j ACCEPT    

Connect with something like this. Assuming 

* Gateway: the gateway server
* GwPort: the gateway port, 443 if https
* Userid: your userid

You can connect to the CVC and i can issue the following command (ask your IT for the exact one)

	openfortivpn GATEWAY:GWPORT -u USERID --set-dns=0 --pppd-use-peerdns=1
	
To retrieve the routing to add to the other machines, the networks are 
the ones with ppp0 gateway

	$> netstat -rn
	
	Routing tables
	Internet:
	Destination        Gateway            Flags        Netif Expire
	default            192.168.1.1        UGScg          en0       
	default            link#18            UCSIg         ppp0       
	2.47.45.190/32     tun0               USc           tun0       
	2.139.166.167/32   ppp0               USc           ppp0    
	
## Reading the routing table

You can calculate the routing with one of the following

* [https://www.colocationamerica.com/ip-calculator](https://www.colocationamerica.com/ip-calculator)
* [http://jodies.de/ipcalc](http://jodies.de/ipcalc)

You can find special entries (just some example)

	10=>10.0.0.0/8
	10.1=>10.1.0.0/16
	10.2.3=>10.2.3.0/32
	10/8=>10.0.0.0/8

Then you can insert into the clients routing the address and the calculated
subnet mask