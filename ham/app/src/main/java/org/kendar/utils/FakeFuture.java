package org.kendar.utils;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FakeFuture implements Future<Object> {
    @Override
    public boolean cancel(boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Object get()  {
        return null;
    }

    @Override
    public Object get(long l, TimeUnit timeUnit) {
        return null;
    }
}
