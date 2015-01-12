/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that encapsulates information about a URL.
 *
 * @author Dan Noguerol
 */
public class URIInfo {
    private URI uri;
    private Map<String,String> headers = new HashMap<>();
    private URLAuthInfo authInfo;

    public URIInfo(String uri) throws URISyntaxException {
        this.uri = new URI(uri);
    }

    public URI getURI() {
        return uri;
    }

    public boolean hasHeaders() {
        return (headers.size() > 0);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public boolean hasAuthInfo() {
        return (authInfo != null);
    }

    public URLAuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(URLAuthInfo authInfo) {
        this.authInfo = authInfo;
    }
}
