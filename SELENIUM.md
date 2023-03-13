## Selenium test procedure

-dns
-servers

Check http://localhost/api/dns/lookup/www.google.com Filled 
Remove all servers
Check http://localhost/api/dns/lookup/www.google.com Empty 
Add google server 8.8.8.8 (not running)
Check http://localhost/api/dns/lookup/www.google.com Empty 
set 8.8.8.8 running
Check http://localhost/api/dns/lookup/www.google.com Filled 
Hide the Id, refresh the page, show the id

-mappings
Verify Sort by dns name 
Check http://localhost/api/dns/lookup/www.google.com Filled 
Add 127.0.0.1 www.google.com
Check http://localhost/api/dns/lookup/www.google.com Filled 127.0.0.1

Verify it's with all data
Hide Id column
Reload and verify Id is hidden

- Resolved
Check http://localhost/api/dns/lookup/www.microsoft.com
Check http://localhost/api/dns/lookup/www.adobe.com
Reload resolved 
Check All/Toggle selected/Toggle selected
Select first 
GEnerate DNS
Verify mapping
Generate SSL
Verify certificates

### Proxies

-UrlDb Rewrites
-Url rewrites
Verify sort by when
Add a new Proxy

	When http://localhost/int/google.com
	Where https://www.google.asdfasd
	Test www.google.com
	Force true
	
Go on rest and test receiving a 404
	GET http://localhost/int/google.com
Modify where to 
	https://www.google.com
GO on rest and be happy

-JDbc rewrite
Creat copy of  the "local" db and change the host to "fake"
	test and veriy the error
Test the local test and verify ok

### SSL

Add/remove/modify an SSL item
Sort address


## API TEST

- DNS download hosts files
- Url db rewrites apply proxy to file
- Generate ssl certificate for website
