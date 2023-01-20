
## Configure proxy<a id="proxy_01"></a>

Should set the proxy to 127.0.0.1 And port 1080 for socks5 or 1081 for http/https

<details>
  <summary>Click me for more explanations</summary>

* Chrome:
    * Install [Proxy Switch Omega](https://chrome.google.com/webstore/detail/proxy-switchyomega/padekgcemlokbadohgkifijomclgjgif)
    * Go to options
    * Add http and https proxy server with
        * Address: 127.0.0.1
        * Port 1081.
      
          <img alt="Ham Proxyes" src="../images/chrome_proxy.gif" width="500"/>
    * Select "proxy" from the extension menu and back to "direct" when you want to disconnect
    * 
      <img alt="Ham Proxyes" src="../images/chrome_proxy_switch.gif" width="100"/>
     
* Firefox
    * Navigate to [about:preferences](about:preferences)
    * Search for "proxy"
    * Click on "Settings"
    * Go to "Manual proxy Configuration"
    * Select the socks5 proxy
        * Address: 127.0.0.1
        * Port 1080
    * Check the "Proxy DNS when using SOCKS v5" flag
    * Clean the settings when needed
  
      <img alt="Ham Proxyes" src="../images/firefox_proxy.gif" width="500"/>
    
</details>

