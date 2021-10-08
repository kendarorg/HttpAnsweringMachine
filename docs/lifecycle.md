The phases to which filters can be applied are

* NONE: The filter will never be executed
* PRE_RENDER
* API: Here are all the API calls, all calls are blocking
* STATIC: Here  the static pages are rendered, all calls are blocking
* Proxy translation [see the proxy](proxy.md)
* PRE_CALL: Before the call to the external sites
* Call external site. If nothing happened the call is forwarded to the external site
* POST_CALL: With the response from external site
* Return data to the caller
* POST_RENDER: With all request response