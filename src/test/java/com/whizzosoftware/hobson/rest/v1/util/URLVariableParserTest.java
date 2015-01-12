/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

public class URLVariableParserTest {
    @Test
    public void testParseWithStraightURL() throws Exception {
        URIInfo ui = URLVariableParser.parse("http://www.foo.com");
        assertEquals("http://www.foo.com", ui.getURI().toASCIIString());
        assertFalse(ui.hasHeaders());
        assertFalse(ui.hasAuthInfo());
    }

    @Test
    public void testParseWithJSONNoURL() throws Exception {
        try {
            URLVariableParser.parse("{}");
            fail("Should have thrown exception");
        } catch (ParseException ignored) {}
    }

    @Test
    public void testParseWithJSONURLOnly() throws Exception {
        URIInfo ui = URLVariableParser.parse("{\"url\":\"http://www.foo.com\"}");
        assertEquals("http://www.foo.com", ui.getURI().toASCIIString());
        assertFalse(ui.hasHeaders());
        assertFalse(ui.hasAuthInfo());
    }

    @Test
    public void testParseWithJSONWithOneHeader() throws Exception {
        URIInfo ui = URLVariableParser.parse("{\"url\":\"http://www.foo.com\",\"headers\":{\"Accept\":\"text/xml\"}}");
        assertEquals("http://www.foo.com", ui.getURI().toASCIIString());
        assertFalse(ui.hasAuthInfo());
        assertTrue(ui.hasHeaders());
        assertEquals(1, ui.getHeaders().size());
        assertEquals("text/xml", ui.getHeaders().get("Accept"));
    }

    @Test
    public void testParseWithJSONWithAuth() throws Exception {
        URIInfo ui = URLVariableParser.parse("{\"url\":\"http://www.foo.com\",\"auth\":{\"username\":\"user\",\"password\":\"pwd\",\"type\":\"basic\"}}");
        assertEquals("http://www.foo.com", ui.getURI().toASCIIString());
        assertFalse(ui.hasHeaders());
        assertTrue(ui.hasAuthInfo());
        assertEquals("user", ui.getAuthInfo().getUsername());
        assertEquals("pwd", ui.getAuthInfo().getPassword());
        assertEquals("basic", ui.getAuthInfo().getType());
    }
}
