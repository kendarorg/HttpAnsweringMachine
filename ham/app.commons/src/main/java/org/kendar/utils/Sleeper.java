package org.kendar.utils;

public class Sleeper {
    public static void sleep(long timeoutMillis){
        try {
            Object obj = new Object();
            synchronized (obj) {
                obj.wait(timeoutMillis);
            }
        }catch(Exception ex){

        }
    }
}
