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

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.*;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.api.util.VersionUtil;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Map;

public class LocalPluginsResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local";
    public static final String TEMPLATE = "/hubs/{hubId}/plugins/{pluginType}";

    @Inject
    AccessManager accessManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.PLUGIN_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        ItemListDTO dto = new ItemListDTO(idProvider.createLocalPluginsId(ctx.getHubContext()).getId());

        Map<String,String> remoteVersions = pluginManager.getRemotePluginVersions(ctx.getHubContext());

        boolean itemExpand = expansions.has("item");
        for (HobsonLocalPluginDescriptor plugin : pluginManager.getLocalPlugins(ctx.getHubContext())) {
            expansions.pushContext(JSONAttributes.ITEM);
            HobsonPluginDTO.Builder builder = new HobsonPluginDTO.Builder(
                    bctx,
                    ctx.getHubContext(),
                    plugin,
                    plugin.getDescription(),
                    null,
                    itemExpand
            );
            String rv = remoteVersions.get(plugin.getId());
            if (rv != null && VersionUtil.versionCompare(rv, plugin.getVersion()) > 0) {
                builder.addLink("update", idProvider.createRemotePluginInstallId(ctx.getHubContext(), plugin.getId(), rv).getId());
            }
            dto.add(builder.build());
            expansions.popContext();
        }

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }
}
