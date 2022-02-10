package org.kendar.utils;

@FunctionalInterface
public interface ThreeParamsFunction<A,B,C,D> {
    D apply(A a,B b, C c);
}
