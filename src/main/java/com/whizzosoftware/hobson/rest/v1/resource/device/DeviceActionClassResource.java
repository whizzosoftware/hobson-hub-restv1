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
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.action.ActionClass;
import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.action.job.AsyncJobHandle;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.action.ActionClassDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
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
    AccessManager accessManager;
    @Inject
    ActionManager actionManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DEVICE_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

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
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        final String actionClassId = getAttribute("actionClassId");

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DEVICE_EXECUTE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

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
