package com.example.microservices.userservice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HelloWorld test for User Service
 */
public class HelloWorldTest {

    @Test
    public void testHelloWorld() {
        String message = "Hello World from User Service";
        assertNotNull(message);
        assertEquals("Hello World from User Service", message);
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
