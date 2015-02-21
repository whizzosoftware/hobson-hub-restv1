/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.image.ImageInputStream;
import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

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
     * @apiVersion 0.5.0
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
     * @apiVersion 0.5.0
     * @apiName SetHubImage
     * @apiDescription Sets the image associated with the hub. The PUT request should specify a Content-Type header and can be either a JSON image library reference (application/json) or raw binary data (image/jpeg or image/png).
     * @apiGroup Hub
     * @apiParamExample {json} Example Request (Image library reference):
     * {
     *   "imageLibRef": "/api/v1/users/local/hubs/local/imageLibrary/images/light7.png"
     * }
     * @apiParamExample {json} Example Request (Image data):
     * {
     *   "image": {
     *     "mediaType": "image/jpeg",
     *     "data": "Base64-encoded image data string"
     *   }
     * }
     * @apiSuccessExample Success Response:
     *   HTTP/1.1 202 Accepted
     */
    @Override
    protected Representation put(Representation entity) throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        JSONObject json = JSONMarshaller.createJSONFromRepresentation(entity);
        if (json.has("imageLibRef")) {
            String path = json.getString("imageLibRef");
            String imageId = path.substring(path.lastIndexOf('/') + 1, path.length());
            ImageInputStream iis = imageManager.getImageLibraryImage(ctx.getUserId(), ctx.getHubId(), imageId);
            try {
                imageManager.setHubImage(new ImageInputStream(MediaType.IMAGE_PNG.toString(), iis.getInputStream()));
                getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            } finally {
                iis.close();
            }
        } else if (json.has("image")) {
            JSONObject image = json.getJSONObject("image");
            ImageInputStream iis = new ImageInputStream(image.getString("mediaType"), new ByteArrayInputStream(Base64.decodeBase64(image.getString("data"))));
            try {
                imageManager.setHubImage(iis);
                getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            } finally {
                iis.close();
            }
        }
        return new EmptyRepresentation();
    }
}
