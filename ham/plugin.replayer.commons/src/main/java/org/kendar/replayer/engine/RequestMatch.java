package org.kendar.replayer.engine;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

public class RequestMatch {
    private long callIndex;
    private long rowIndex;

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

    private Request originalReq;
    private Request foundedReq;

    public void setFoundedRes(Response foundedRes) {
        this.foundedRes = foundedRes;
    }

    public RequestMatch(Request originalReq, Request foundedReq, Response foundedRes) {
        this.originalReq = originalReq;
        this.foundedReq = foundedReq;
        this.foundedRes = foundedRes;
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

    private Response foundedRes;
}
