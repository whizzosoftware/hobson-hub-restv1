/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    static public Map<String,Object> createEmptyMap(HobsonRestContext ctx) {
        Map<String,Object> map = new HashMap<>();
        map.put(JSONAttributes.HUB_ID, ctx.getHubId());
        return map;
    }

    static public Map<String,Object> createSingleEntryMap(HobsonRestContext ctx, String key, Object value) {
        Map<String,Object> map = new HashMap<>();
        map.put(JSONAttributes.HUB_ID, ctx.getHubId());
        map.put(key, value);
        return map;
    }

    static public Map<String,Object> createTripleEntryMap(HobsonRestContext ctx, String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String,Object> map = new HashMap<>();
        map.put(JSONAttributes.HUB_ID, ctx.getHubId());
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

}
