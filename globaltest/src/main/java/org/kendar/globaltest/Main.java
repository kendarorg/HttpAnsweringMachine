package org.kendar.globaltest;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    public static void main(String[] args) throws Exception {
        var queue = new ConcurrentLinkedQueue<String>();
        new ProcessRunner().
                withCommand("ps").
                withParameter("-ef").
                withStorage(queue).
                run();
        for(var item:queue){
            System.out.println(item);
        }

    }
}
