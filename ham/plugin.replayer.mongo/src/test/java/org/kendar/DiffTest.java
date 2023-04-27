package org.kendar;

import org.junit.jupiter.api.Test;
import org.kendar.replayer.engine.mongo.Levenshtein;

public class DiffTest {


    @Test
    void testLevenshtein() {
        var delta = Levenshtein.calculate(
                "c'era una volta una capretta",
                "era una una bella capretta");
        System.out.println(delta);

        delta = Levenshtein.calculate(
                "c'era una una bella capretta",
                "c'era una volta una capretta"
        );
        System.out.println(delta);

        delta = Levenshtein.calculate(
                "xxxx",
                "c'era una una bella capretta"
        );
        System.out.println(delta);
    }


    @Test
    void testNormalizedLevenshtein() {
        var delta = Levenshtein.normalized(
                "c'era una volta una capretta",
                "era una una bella capretta", 100);
        System.out.println(delta);

        delta = Levenshtein.normalized(
                "c'era una una bella capretta",
                "c'era una volta una capretta", 100
        );
        System.out.println(delta);

        delta = Levenshtein.normalized(
                "c'era una una bella capretta",
                "c'era una una bella capretta", 100
        );
        System.out.println(delta);
        delta = Levenshtein.normalized(
                "xxxx",
                "c'era una una bella capretta", 100
        );
        System.out.println(delta);
    }

    @Test
    void testNormalizedLevenshteinNone() {
        var delta = Levenshtein.normalized(
                "xxxx",
                "c'era una una bella capretta", 100
        );
        System.out.println(delta);
    }
}
