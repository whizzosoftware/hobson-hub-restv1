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
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.variable.DeviceVariableDescriptor;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaProxyHandler;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.Representation;

import javax.inject.Inject;

/**
 * A REST resource that proxies images and video.
 *
 * @author Dan Noguerol
 */
public class MediaProxyResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/media/{mediaId}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    MediaProxyHandler proxyHandler;

    @Override
    public Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        DeviceVariableDescriptor hvar = deviceManager.getDevice(DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"))).getVariable(getAttribute("mediaId"));
        return proxyHandler.createRepresentation(ctx.getHubContext(), hvar, deviceManager.getDeviceVariable(hvar.getContext()), getQuery(), getResponse());
    }
}
