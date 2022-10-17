
### Filters

On each request several filters are executed, dynamically loaded from the libs directory
or for the Javascript filters, from a specific configuration directory.

The filters are listed in the [filters lifecycle](../lifecycle.md) page

They intercept the request according to their selection mechanism and can
change the request and response.

When they are "blocking" the data is directly returned to the client

The filters are the way HAM can do its magic :)

For more infos check the [java filters](../plugins/java/jfilters.md) or the
[Js Filters](../plugins/js.md) plugin.