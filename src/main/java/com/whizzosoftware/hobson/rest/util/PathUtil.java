/*
 *******************************************************************************
 * Copyright (c) 2017 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.util;

public class PathUtil {
    /**
     * Converts a REST URL path into a colon-separated path (removing the API version prefix).
     *
     * @param apiRoot the API root to remove
     * @param path the REST URL path
     *
     * @return a String
     */
    static public String convertPath(String apiRoot, String path) {
        if (apiRoot != null && path != null && path.startsWith(apiRoot)) {
            return path.substring(apiRoot.length() + 1).replace('/', ':');
        } else {
            return (path != null) ? path.replace('/', ':') : null;
        }
    }
}
