package org.tkendar.cucumber;


import org.kendar.ham.HamBasicBuilder;
import org.kendar.ham.HamBuilder;

public class GlobalSettings {
    public static HamBasicBuilder builder(){
        return HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1",1080)
                .withDns("127.0.0.1");
    }

}
