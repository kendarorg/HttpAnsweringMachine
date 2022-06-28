package org.kendar.swagger;

import com.google.common.primitives.Primitives;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuavaTests {
    @Test
    public void test(){
        assertTrue(Primitives.isWrapperType(Integer.class));
        assertTrue(Primitives.isWrapperType(int.class));
        assertTrue(int.class.isPrimitive());
        assertTrue(Primitives.unwrap(Integer.class).isPrimitive());
        assertTrue(int[].class.isArray());

    }
}
