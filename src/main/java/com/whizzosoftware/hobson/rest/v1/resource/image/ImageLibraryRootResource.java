/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.image.ImageGroup;
import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MapUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Template;

import javax.inject.Inject;
import java.util.List;

public class ImageLibraryRootResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/imageLibrary";

    @Inject
    ImageManager imageManager;
    @Inject
    IdProvider idProvider;

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
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        JSONArray results = new JSONArray();
        List<ImageGroup> groups = imageManager.getImageLibraryGroups(ctx.getHubContext());
        for (ImageGroup group : groups) {
            results.put(addImageLibraryGroupLinks(
                    ctx,
                    createImageLibraryGroupJSON(group),
                    group.getId()
            ));
        }
        return new JsonRepresentation(results);
    }

    private static JSONObject createImageLibraryGroupJSON(ImageGroup group) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", group.getName());
            return json;
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }

    private JSONObject addImageLibraryGroupLinks(HobsonRestContext ctx, JSONObject json, String groupId) {
        JSONObject groupLinks = new JSONObject();
        groupLinks.put("self", ctx.getApiRoot() + new Template(ImageLibraryGroupResource.PATH).format(MapUtil.createSingleEntryMap(ctx, "groupId", groupId)));
        json.put(JSONAttributes.LINKS, groupLinks);
        return json;
    }

}
