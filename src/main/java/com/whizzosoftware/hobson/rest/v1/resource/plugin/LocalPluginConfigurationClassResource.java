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

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.plugin.HobsonLocalPluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class LocalPluginConfigurationClassResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/configurationClass";

    @Inject
    PluginManager pluginManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        HobsonLocalPluginDescriptor plugin = pluginManager.getLocalPlugin(pctx);
        if (plugin != null) {
            PropertyContainerClass pcc = plugin.getConfigurationClass();
            if (pcc != null) {
                PropertyContainerClassDTO dto = new PropertyContainerClassDTO.Builder(bctx, bctx.getIdProvider().createLocalPluginConfigurationClassId(pctx), pcc, true).build();
                dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());
                JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
                jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
                return jr;
            } else {
                throw new HobsonNotFoundException("Plugin configuration class not found");
            }
        } else {
            throw new HobsonNotFoundException("Plugin not found");
        }
    }
}
