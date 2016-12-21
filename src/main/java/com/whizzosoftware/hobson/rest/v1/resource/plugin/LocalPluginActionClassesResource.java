/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.action.ActionClass;
import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.action.ActionClassDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;

public class LocalPluginActionClassesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/actionClasses";

    @Inject
    ActionManager actionManager;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        boolean itemExpand = expansions.has("item");

        Collection<ActionClass> actionClasses = actionManager.getActionClasses(pctx);
        ItemListDTO results = new ItemListDTO(idProvider.createActionClassesId(ctx.getHubContext()));
        for (ActionClass ac : actionClasses) {
            expansions.pushContext(JSONAttributes.ITEM);
            results.add(new ActionClassDTO.Builder(idProvider.createActionClassId(ac.getContext()), ac, itemExpand).build());
            expansions.popContext();
        }
        return new JsonRepresentation(results.toJSON());
    }
}