package org.kendar.ham;

public class GlobalSettings {

    public static HamBasicBuilder builder() {
        return HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1", 1080)
                .withDns("127.0.0.1");
    }

    public static HamBasicBuilder builderHttpProxy() {
        return HamBuilder
                .newHam("www.local.test")
                .withHttpProxy("127.0.0.1", 1081)
                .withDns("127.0.0.1");
    }


}
