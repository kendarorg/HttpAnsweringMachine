package org.kendar.ham;

import com.sun.net.httpserver.HttpServer;

import java.util.ArrayList;
import java.util.List;

public class BaseStates {
    protected static String resultData;
    protected static HttpServer httpServer = null;
    protected static List<String> dnses = new ArrayList<>();
    protected static List<String> proxies = new ArrayList<>();
    protected static HamBuilder hamBuilder = (HamBuilder)GlobalSettings.builder();
}
