/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.image.ImageInputStream;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
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
    public static final String PATH = "/hubs/{hubId}/plugins/{pluginId}/image";

    @Inject
    AccessManager accessManager;
    @Inject
    PluginManager pluginManager;

    @Override
    protected Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final String pluginId = getAttribute("pluginId");

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PLUGIN_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

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
