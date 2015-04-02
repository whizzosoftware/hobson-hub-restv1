/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.json.JSONSerializationHelper;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkHelper;
import org.json.JSONArray;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.List;

public class ImageLibraryGroupResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/imageLibrary/groups/{groupId}";

    @Inject
    Authorizer authorizer;
    @Inject
    ImageManager imageManager;
    @Inject
    HATEOASLinkHelper linkHelper;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/imageLibrary/groups/{groupId} Get library group
     * @apiVersion 0.4.4
     * @apiName GetImageLibraryGroup
     * @apiDescription Retrieves a list of all images in an image library group.
     * @apiGroup Images
     * @apiSuccessExample {json} Success Response:
     * [
     *   {
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/imageLibrary/images/surveillance85.png"
     *     }
     *   },
     *   {
     *     "links": {
     *       "self": "/api/v1/users/local/hubs/local/imageLibrary/images/camera1.png"
     *     }
     *   }
     * ]
     */
    @Override
    public Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getUserId(), ctx.getHubId());
        JSONArray results = new JSONArray();
        List<String> ids = imageManager.getImageLibraryImageIds(ctx.getUserId(), ctx.getHubId(), getAttribute("groupId"));
        for (String id : ids) {
            results.put(linkHelper.addImageLibraryImageLinks(ctx, JSONSerializationHelper.createImageLibraryImageJSON(id), id));
        }
        return new JsonRepresentation(results);
    }
}
