## Utils

### Simple rest Client

You can send simple requests directly from inside the program

#### GET/DELETE requests  

<pre>
GET https://www.google.com
Content-Type:application-json
</pre>

#### PUT/POST/OPTION with text only body

<pre>
POST https://www.google.com
Content-Type:application-json

{
    "data":"value"
}
</pre>

### JsonSchema extractor/verifier

Allow to verify xml and json data based on xsd/json schema or message templates

For xml validation based on template the template and the item to test are converted
to json and then from the template is extracted the schema against which the 
message will be verified

### Regex tester

A tester for regular expressions just to verify the regexps before 
using them on filters.

Mind the slashes! This regexp will be converted to a String so it
does not need the typical string slashes!

<u>Simple regexp</u>

<pre>
    Regexp: [a-zA-Z]+
    Input: I need to split this string
    Result:
        Found 6 matches:
        match:I
        match:need
        match:to
        match:split
        match:this
        match:string
        END OF RESULTS
</pre>

<u>Named groups</u>

<pre>
    Regexp: (?&lt;project>[A-Z]{3})(?&lt;sep>[-/])(?&lt;org>\w{3})\k&lt;sep>(?&lt;num>\d+)$
    Input: PRJ-CLD-42
    Result:
        Found 5 matches:
        group:
            0:PRJ-CLD-42
            1 (project):PRJ
            2 (sep):-
            3 (org):CLD
        END OF RESULTS
</pre>