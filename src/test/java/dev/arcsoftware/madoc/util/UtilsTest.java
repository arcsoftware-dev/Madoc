package dev.arcsoftware.madoc.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void toCamelCase() {
        assertEquals("Jacob Arsenault", Utils.toCamelCase("JACOB ARSENAULT"));
        assertEquals("Jacob Arsenault", Utils.toCamelCase("JACOB ARSENAULT    "));
        assertEquals("Jaret Woo Sam", Utils.toCamelCase("JARET WOO SAM"));
        assertEquals("Mike O'Dwyer", Utils.toCamelCase("MIKE O'DWYER"));
        assertEquals("Mike O'Dwyer Two", Utils.toCamelCase("MIKE O'DWYER TWO"));
    }
}