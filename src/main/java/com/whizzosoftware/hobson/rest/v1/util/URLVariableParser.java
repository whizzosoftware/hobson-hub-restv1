/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * A class with static methods for parsing variable values containing URL information.
 *
 * @author Dan Noguerol
 */
public class URLVariableParser {
    private static final String PROP_URL = "url";
    private static final String PROP_HEADERS = "headers";
    private static final String PROP_AUTH = "auth";
    private static final String PROP_USERNAME = "username";
    private static final String PROP_PASSWORD = "password";
    private static final String PROP_TYPE = "type";

    /**
     * Parse a String value into a URLInfo object.
     *
     * @param value the String value to parse
     *
     * @return a URIInfo object
     * @throws ParseException on failure
     * @throws URISyntaxException on failure
     */
    public static URIInfo parse(String value) throws ParseException, URISyntaxException {
        String ts = value.trim();

        // assume that value starting with '{' is a JSON object
        if (ts.charAt(0) == '{') {
            JSONObject json = new JSONObject(new JSONTokener(value));

            // "url" is a required pair
            if (!json.has(PROP_URL)) {
                throw new ParseException("'url' is a required key in JSON object", 0);
            }

            URIInfo info = new URIInfo(json.getString(PROP_URL));

            // add headers if any are defined
            if (json.has(PROP_HEADERS)) {
                JSONObject headers = json.getJSONObject(PROP_HEADERS);
                for (Object o : headers.keySet()) {
                    String key = o.toString();
                    info.addHeader(key, headers.getString(key));
                }
            }

            // add auth information if any is defined
            if (json.has(PROP_AUTH)) {
                JSONObject auth = json.getJSONObject(PROP_AUTH);
                if (!auth.has(PROP_USERNAME) || !auth.has(PROP_PASSWORD) || !auth.has(PROP_TYPE)) {
                    throw new ParseException("'username', 'password' and 'type' are required in 'auth' JSON object", 0);
                }
                info.setAuthInfo(new URLAuthInfo(auth.getString(PROP_USERNAME), auth.getString(PROP_PASSWORD), auth.getString(PROP_TYPE)));
            }

            return info;

        // otherwise, simply treat the value as a full URL
        } else {
            return new URIInfo(ts);
        }
    }
}
