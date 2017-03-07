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

import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource for reloading a plugin.
 *
 * @author Dan Noguerol
 */
public class LocalPluginReloadResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/reload";

    @Inject
    AccessManager accessManager;
    @Inject
    PluginManager pluginManager;

    @Override
    protected Representation post(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PLUGIN_EXECUTE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        pluginManager.reloadLocalPlugin(PluginContext.create(ctx.getHubContext(), getAttribute("pluginId")));

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}
