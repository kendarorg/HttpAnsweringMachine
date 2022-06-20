package org.kendar.servers.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.utils.models.RegexpData;
import org.kendar.servers.utils.models.RegexpResult;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class RegexpAPI implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/utils/regexp",
            method = "POST",
            id = "1000a4b4-29tad-11ec-9621-0242ac130002")
    public void testRegexp(Request req, Response res) throws JsonProcessingException {
        var result = new RegexpResult();
        var data = mapper.readValue(req.getRequestText(), RegexpData.class);
        var flags = data.isCaseInsensitive()? Pattern.CASE_INSENSITIVE:0;
        flags |=data.isLiteral()? Pattern.LITERAL:0;
        flags |=data.isUnicodeCase()? Pattern.UNICODE_CASE:0;
        flags |=data.isMultiline()? Pattern.MULTILINE:0;

        try {
            Pattern pattern = Pattern.compile(data.getRegexp(), flags);
            Matcher matcher = pattern.matcher(data.getMatcherString());

            result.setFailed(false);
            final List<String> matches = new ArrayList<>();
            var matchesCount = 0;
            Method method = pattern.getClass().getDeclaredMethod("namedGroups");
            method.setAccessible(true);
            while (matcher.find()) {
                matchesCount++;
                var namedGroups = (Map<String, Integer>)method.invoke(pattern);
                parseGroup(matches,matcher,namedGroups);
            }


            result.setMatchFound(matchesCount>0);
            result.setMatches(matches);
        }catch (PatternSyntaxException ex){
            result.setFailed(true);
            result.setError(ex.getMessage()+" "+ex.getDescription()+" "+ex.getPattern()+" "+ex.getIndex());
        }catch (Exception ex){
            result.setFailed(true);
            result.setError(ex.getMessage()+" "+ ex);
        }
        res.setStatusCode(200);
        res.addHeader("content-type","application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    private void parseGroup(List<String> matches, Matcher matcher, Map<String, Integer> named) {
        var size= matcher.groupCount();

        var founded = false;
        for(int i=0;i<size;i++){
            final int index = i;
            if(i==0)matches.add("group:");
            var name = "";
            if(named!=null){
                var groupName = named.entrySet().stream().filter(g->g.getValue().intValue()==index).findFirst();
                if(groupName.isPresent()){
                    name = " ("+groupName.get().getKey()+")";
                }
            }
            matches.add("\t"+i+name+":"+matcher.group(i));
            founded= true;
        }
        if(size==0 && !founded){
            var name="";
            if(named!=null){
                var groupName = named.entrySet().stream().filter(g->g.getValue().intValue()==0).findFirst();
                if(groupName.isPresent()){
                    name = " ("+groupName.get().getKey()+")";
                }
            }
            matches.add("match"+name+":"+matcher.group(0));
        }
    }
}
