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

}
