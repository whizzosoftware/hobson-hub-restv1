/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource that accesses a device's configuration.
 *
 * @author Dan Noguerol
 */
public class DeviceConfigurationResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/configuration";
    public static final String TEMPLATE = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/{entity}";

    @Inject
    AccessManager accessManager;
    @Inject
    DeviceManager deviceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DEVICE_CONFIGURE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        final DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        PropertyContainer config = deviceManager.getDeviceConfiguration(dctx);

        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(
                bctx,
                config,
                new PropertyContainerClassProvider() {
                    @Override
                    public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                        return deviceManager.getDevice(dctx).getConfigurationClass();
                    }
                },
                PropertyContainerClassType.DEVICE_CONFIG,
                true
        ).build();

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation put(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.DEVICE_CONFIGURE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();

        deviceManager.setDeviceConfiguration(dctx, dto.getValues());

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}
