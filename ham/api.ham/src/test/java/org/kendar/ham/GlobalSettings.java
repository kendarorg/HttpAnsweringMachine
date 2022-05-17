package org.kendar.ham;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalSettings {

    public static HamBasicBuilder builder(){
        return HamBuilder
                .newHam("www.local.test")
                .withSocksProxy("127.0.0.1",1080)
                .withDns("127.0.0.1");
    }


}
