package com.example.microservices.pointservice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HelloWorld test for Point Service
 */
public class HelloWorldTest {

    @Test
    public void testHelloWorld() {
        String message = "Hello World from Point Service";
        assertNotNull(message);
        assertEquals("Hello World from Point Service", message);
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
