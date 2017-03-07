/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.image.ImageGroup;
import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
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
    public static final String PATH = "/hubs/{hubId}/imageLibrary";

    @Inject
    AccessManager accessManager;
    @Inject
    ImageManager imageManager;
    @Inject
    IdProvider idProvider;

    @Override
    public Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

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
