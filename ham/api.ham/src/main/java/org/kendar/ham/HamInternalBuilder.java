package org.kendar.ham;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.List;
import java.util.function.Supplier;

public interface HamInternalBuilder extends HamBasicBuilder {
    HamRequestBuilder newRequest();
    Response call(Request request) throws HamException;
    Response expectCode(Response response, int code, Supplier<String> getExceptionMessage) throws HamException;
    Response expectCode(Response response,int code,String getExceptionMessage) throws HamException;
    <T> T callJson(Request request,Class<T> clazz) throws HamException;
    <T> List<T> callJsonList(Request request, Class<T> clazz) throws HamException;
}
