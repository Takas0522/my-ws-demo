package com.example.microservices.authservice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HelloWorld test for Auth Service
 */
public class HelloWorldTest {

    @Test
    public void testHelloWorld() {
        String message = "Hello World from Auth Service";
        assertNotNull(message);
        assertEquals("Hello World from Auth Service", message);
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
