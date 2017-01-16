/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.representation.Representation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper utility for JSON-related functions.
 *
 * @author Dan Noguerol
 */
public class JSONHelper {
    public static JSONObject createJSONFromRepresentation(Representation r) {
        try {
            InputStream is = r.getStream();
            if (is != null) {
                try {
                    return new JSONObject(new JSONTokener(is));
                } finally {
                    is.close();
                }
            } else {
                return new JSONObject();
            }
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error reading JSON", e);
        }
    }

    public static JSONArray createJSONArrayFromRepresentation(Representation r) {
        try {
            InputStream is = r.getStream();
            if (is != null) {
                try {
                    return new JSONArray(new JSONTokener(is));
                } finally {
                    is.close();
                }
            } else {
                return new JSONArray();
            }
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error reading JSON", e);
        }
    }

    public static Map<String,Object> createMapFromJSONObject(JSONObject json) {
        Map<String,Object> map = new HashMap<>();
        for (Object o : json.keySet()) {
            String key = (String)o;
            map.put(key, json.get(key));
        }
        return map;
    }
}
