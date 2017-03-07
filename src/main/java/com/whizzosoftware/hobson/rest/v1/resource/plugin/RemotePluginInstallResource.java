/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.MapUtil;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Template;

import javax.inject.Inject;

/**
 * A REST resource for installing an available plugin.
 *
 * @author Dan Noguerol
 */
public class RemotePluginInstallResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/remote/{pluginId}/{pluginVersion}/install";

    @Inject
    AccessManager accessManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation post(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PLUGIN_INSTALL, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        String pluginId = getAttribute("pluginId");
        String pluginVersion = getAttribute("pluginVersion");

        pluginManager.installRemotePlugin(PluginContext.create(ctx.getHubContext(), pluginId), pluginVersion);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        getResponse().setLocationRef(ctx.getApiRoot() + new Template(LocalPluginResource.PATH).format(MapUtil.createSingleEntryMap(ctx, "pluginId", pluginId)));

        return new EmptyRepresentation();
    }
}
