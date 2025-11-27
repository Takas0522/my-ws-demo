package com.example.microservices.bff;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HelloWorld test for BFF
 */
public class HelloWorldTest {

    @Test
    public void testHelloWorld() {
        String message = "Hello World from BFF";
        assertNotNull(message);
        assertEquals("Hello World from BFF", message);
    }

    @Test
    public void testAddition() {
        int result = 2 + 3;
        assertEquals(5, result);
    }

    @Test
    public void testStringConcatenation() {
        String hello = "Hello";
        String world = "World";
        String combined = hello + " " + world;
        assertEquals("Hello World", combined);
    }
}
