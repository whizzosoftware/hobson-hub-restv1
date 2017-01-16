/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.google.inject.Inject;
import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.action.ActionClass;
import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.action.job.AsyncJobHandle;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.action.ActionClassDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

public class DeviceActionClassResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/actionClasses/{actionClassId}";

    @Inject
    ActionManager actionManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        final DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        final String actionClassId = getAttribute("actionClassId");

        ActionClass ac = actionManager.getActionClass(PropertyContainerClassContext.create(dctx, actionClassId));

        PropertyContainerClassDTO dto = new ActionClassDTO.Builder(bctx, bctx.getIdProvider().createActionClassId(ac.getContext()), ac, true).build();
        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation post(Representation entity) {
        if (!isInRole(HobsonRole.administrator.name()) && !isInRole(HobsonRole.userWrite.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        final DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        final String actionClassId = getAttribute("actionClassId");

        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return actionManager.getActionClass(PropertyContainerClassContext.create(dctx, actionClassId));
            }
        };
        AsyncJobHandle result = actionManager.executeAction(DTOMapper.mapPropertyContainerDTO(dto, pccp, idProvider));
        try {
            result.getStartFuture().sync();
            getResponse().setStatus(Status.SUCCESS_CREATED);
            getResponse().setLocationRef(idProvider.createJobId(ctx.getHubContext(), result.getJobId()).getId());
            return new EmptyRepresentation();
        } catch (InterruptedException e) {
            throw new HobsonRuntimeException("Unable to execute action");
        }
    }
}
