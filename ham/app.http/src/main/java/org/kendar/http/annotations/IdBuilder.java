package org.kendar.http.annotations;

public class IdBuilder {

    public static String buildId(HttpTypeFilter type, HttpMethodFilter method, Object clazz) {
        if(method.id()!=null && method.id().length()>0){
            return method.id();
        }
        String result = "";
        if(clazz!=null){
            if(clazz.equals(String.class)){
                result += clazz + ":";
            }else {
                result += clazz.getClass().getSimpleName() + ":";
            }
        }
        result += method.method()+":";
        if(type.hostPattern()!=null && type.hostPattern().length()>0){
            result += type.hostPattern()+"/";
        }else{
            result += type.hostAddress()+"/";
        }
        if(method.pathPattern()!=null && method.pathPattern().length()>0){
            result += method.pathPattern();
        }else{
            result += method.pathAddress();
        }
        result+=":"+method.phase().toString()+":"+type.priority();
        return result;
    }
}
