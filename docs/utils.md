## Utils

### JsonSchema extractor/verifier

### Regex tester

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