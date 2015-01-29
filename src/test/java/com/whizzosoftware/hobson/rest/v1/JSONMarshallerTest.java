/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

import com.whizzosoftware.hobson.api.hub.HubLocation;
import com.whizzosoftware.hobson.api.util.UserUtil;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class JSONMarshallerTest {
    @Test
    public void testCreateHubLocation() {
        // test text only
        JSONObject json = new JSONObject();
        json.put("text", "my_address");
        HubLocation loc = JSONMarshaller.createHubLocation(json);
        assertEquals("my_address", loc.getText());
        assertNull(loc.getLatitude());
        assertNull(loc.getLongitude());

        // test lat/long only
        json = new JSONObject();
        json.put("latitude", "39.3722");
        json.put("longitude", "-104.8561");
        loc = JSONMarshaller.createHubLocation(json);
        assertNull(loc.getText());
        assertEquals(39.3722, loc.getLatitude(), 4);
        assertEquals(-104.8561, loc.getLongitude(), 4);
    }

    @Test
    public void testCreateHubConfigurationJSON() {
        HobsonRestContext ctx = new HobsonRestContext(null, UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB);

        HubLocation loc = new HubLocation("home", null, null);
        JSONObject json = JSONMarshaller.createHubConfigurationJSON(ctx, "foo", null, loc, "INFO", true);
        assertEquals("foo", json.getString("name"));
        assertTrue(json.has("location"));
        assertEquals("home", json.getJSONObject("location").getString("text"));
        assertFalse(json.getJSONObject("location").has("latitude"));
        assertFalse(json.getJSONObject("location").has("longitude"));

        loc = new HubLocation(null, 39.3722, -104.8561);
        json = JSONMarshaller.createHubConfigurationJSON(ctx, null, null, loc, "INFO", true);
        assertFalse(json.has("name"));
        assertFalse(json.getJSONObject("location").has("text"));
        assertEquals(39.3722, json.getJSONObject("location").getDouble("latitude"), 4);
        assertEquals(-104.8561, json.getJSONObject("location").getDouble("longitude"), 4);
    }
}
