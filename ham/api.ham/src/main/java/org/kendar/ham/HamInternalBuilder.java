package org.kendar.ham;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.List;
import java.util.function.Supplier;

/**
 * Extended builder functions exposed only for custom builders
 */
public interface HamInternalBuilder extends HamBasicBuilder {
    /**
     * Create a new http request builder
     * @return
     */
    HamRequestBuilder newRequest();

    /**
     * Execute a call through HAM remote invocation
     * @param request
     * @return
     * @throws HamException
     */
    Response call(Request request) throws HamException;

    /**
     * Throw if wrong code is returned
     * @param response
     * @param code
     * @param getExceptionMessage Generator for the message
     * @return
     * @throws HamException
     */
    Response expectCode(Response response, int code, Supplier<String> getExceptionMessage) throws HamException;

    /**
     * Throw if wrong code is returned
     * @param response
     * @param code
     * @param exceptionMessage
     * @return
     * @throws HamException
     */
    Response expectCode(Response response,int code,String exceptionMessage) throws HamException;

    /**
     * Make a call to HAM remote invocation and return a parsed JSON object
     * @param request
     * @param clazz
     * @return
     * @param <T>
     * @throws HamException
     */
    <T> T callJson(Request request,Class<T> clazz) throws HamException;

    /**
     * Make a call to HAM remote invocation and return a parsed List of JSON object
     * @param request
     * @param clazz
     * @return
     * @param <T>
     * @throws HamException
     */
    <T> List<T> callJsonList(Request request, Class<T> clazz) throws HamException;
}
