/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.image.ImageInputStream;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.apache.commons.codec.binary.Base64InputStream;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.InputStream;

/**
 * A REST resource for retrieving a plugin's icon.
 *
 * @author Dan Noguerol
 */
public class LocalPluginImageResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/image";

    @Inject
    PluginManager pluginManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/plugins/:pluginId/image Get local plugin image
     * @apiVersion 0.5.0
     * @apiName GetPluginImage
     * @apiDescription Retrieves the image for a plugin
     * @apiGroup Plugin
     * @apiSuccessExample {json} Success Response:
     *   HTTP/1.1 200 OK
     *   Content-Type: image/jpeg
     *   ...
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        String pluginId = getAttribute("pluginId");

        String s = getQueryValue("base64");
        final boolean base64 = (s != null) && Boolean.parseBoolean(s);

        ImageInputStream iis = pluginManager.getLocalPluginIcon(PluginContext.create(ctx.getHubContext(), pluginId));
        InputStream is = iis.getInputStream();
        if (base64) {
            is = new Base64InputStream(is, true);
        }

        return new InputRepresentation(is, MediaType.valueOf(iis.getMediaType()));
    }
}
