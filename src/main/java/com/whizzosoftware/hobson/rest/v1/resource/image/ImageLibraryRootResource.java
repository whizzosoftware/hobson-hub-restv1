/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.image.ImageGroup;
import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.JSONMarshaller;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.List;

public class ImageLibraryRootResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/imageLibrary";
    public static final String REL = "imageLibrary";

    @Inject
    ImageManager imageManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/imageLibrary Get library groups
     * @apiVersion 0.4.4
     * @apiName GetImageLibraryGroups
     * @apiDescription Retrieves all image library groups.
     * @apiGroup Images
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "name": "Bulbs & Lights",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/imageLibrary/groups/bulbs"
     *     }
     *   },
     *   {
     *     "name": "Cameras",
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/imageLibrary/groups/cameras"
     *     }
     *   }
     * ]
     */
    @Override
    public Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        List<ImageGroup> groups = imageManager.getImageLibraryGroups(ctx.getUserId(), ctx.getHubId());
        return new JsonRepresentation(JSONMarshaller.createImageLibraryGroupListJSON(ctx, groups));
    }
}
