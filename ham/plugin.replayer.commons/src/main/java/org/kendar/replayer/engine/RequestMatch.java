package org.kendar.replayer.engine;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

public class RequestMatch {
    private final Request originalReq;
    private final Request foundedReq;
    private long callIndex;
    private long rowIndex;
    private Response foundedRes;

    public RequestMatch(Request originalReq, Request foundedReq, Response foundedRes) {
        this.originalReq = originalReq;
        this.foundedReq = foundedReq;
        this.foundedRes = foundedRes;
    }

    public long getCallIndex() {
        return callIndex;
    }

    public void setCallIndex(long callIndex) {
        this.callIndex = callIndex;
    }

    public long getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Request getOriginalReq() {
        return originalReq;
    }

    public Request getFoundedReq() {
        return foundedReq;
    }

    public Response getFoundedRes() {
        return foundedRes;
    }

    public void setFoundedRes(Response foundedRes) {
        this.foundedRes = foundedRes;
    }
}
