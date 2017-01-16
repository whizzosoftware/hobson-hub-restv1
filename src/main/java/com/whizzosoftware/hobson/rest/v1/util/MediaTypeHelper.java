/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.dto.EntityDTO;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;

public class MediaTypeHelper {
    static public MediaType createMediaType(Request request, EntityDTO dto) {
        return createMediaType(request, dto.getJSONMediaType());
    }

    static public MediaType createMediaType(Request request, String mediaType) {
        for (Preference<MediaType> p : request.getClientInfo().getAcceptedMediaTypes()) {
            if (p.getMetadata().equals(MediaType.APPLICATION_JSON)) {
                return MediaType.APPLICATION_JSON;
            }
        }
        return new MediaType(mediaType);
    }
}
