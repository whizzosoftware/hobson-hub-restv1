/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.image.ImageInputStream;
import com.whizzosoftware.hobson.api.image.ImageManager;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.IOException;

/**
 * A REST resource for setting/retrieving a hub image.
 *
 * @author Dan Noguerol
 */
public class HubImageResource extends SelfInjectingServerResource {
    public static final String REL = "image";
    public static final String PATH = "/users/{userId}/hubs/{hubId}/image";

    @Inject
    ImageManager imageManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/image Get Hub image
     * @apiVersion 0.4.4
     * @apiName GetHubImage
     * @apiDescription Retrieves the binary image data associated with the hub.
     * @apiGroup Hub
     * @apiSuccessExample Success Response:
     *   HTTP/1.1 200 OK
     *   Content-Type: image/jpeg
     *   ...
     */
    @Override
    protected Representation get() throws ResourceException {
        ImageInputStream iis = imageManager.getHubImage();
        return new InputRepresentation(iis.getInputStream(), MediaType.valueOf(iis.getMediaType()));
    }

    /**
     * @api {put} /api/v1/users/:userId/hubs/:hubId/image Set Hub image
     * @apiVersion 0.4.4
     * @apiName SetHubImage
     * @apiDescription Sets the binary image data associated with the hub. The PUT entity should be raw binary data with an appropriate Content-Type header (image/jpeg or image/png).
     * @apiGroup Hub
     * @apiSuccessExample Success Response:
     *   HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) throws ResourceException {
        try {
            imageManager.setHubImage(new ImageInputStream(entity.getMediaType().toString(), entity.getStream()));
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return new EmptyRepresentation();
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error reading image stream", e);
        }
    }
}