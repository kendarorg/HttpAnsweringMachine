This is a simple socks5 implementation to allow the usage of
the applications outside docker without changing the hosts file

The default setting is to listen on port 1080, while using
ham internal dns service. Following this you can intercept 
everything you need.

Here is the basic configuration

    {
    "id": "socks5.server",
    "port": 1080,
    "active": true
    }