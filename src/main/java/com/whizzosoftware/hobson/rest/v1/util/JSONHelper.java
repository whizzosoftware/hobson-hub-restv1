/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.representation.Representation;

import java.io.IOException;

/**
 * A helper utility for JSON-related functions.
 *
 * @author Dan Noguerol
 */
public class JSONHelper {
    public static JSONObject createJSONFromRepresentation(Representation r) {
        try {
            return new JSONObject(new JSONTokener(r.getStream()));
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error reading JSON", e);
        }
    }
}
