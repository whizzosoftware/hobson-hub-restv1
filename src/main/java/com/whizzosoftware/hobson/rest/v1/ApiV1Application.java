/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

import com.whizzosoftware.hobson.rest.v1.resource.HubInfoResource;
import com.whizzosoftware.hobson.rest.v1.resource.LogResource;
import com.whizzosoftware.hobson.rest.v1.resource.ShutdownResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionsResource;
import com.whizzosoftware.hobson.rest.v1.resource.config.HubConfigurationResource;
import com.whizzosoftware.hobson.rest.v1.resource.config.HubPasswordResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.ExecuteTaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TasksResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariableResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariablesResource;
import org.restlet.Restlet;
import org.restlet.ext.guice.ResourceInjectingApplication;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;

/**
 * The Hobson REST API v1.
 *
 * @author Dan Noguerol
 */
public class ApiV1Application extends ResourceInjectingApplication implements HobsonApiApplication {
    private String apiRoot;
    private Authenticator authenticator;

    public ApiV1Application() {
        super();
        System.out.println("ApiV1Application()");
        this.apiRoot = "/api/v1";
        setStatusService(new HobsonStatusService());
    }

    public ApiV1Application(String apiRoot, Authenticator authenticator) {
        super();
        this.apiRoot = apiRoot;
        this.authenticator = authenticator;
        setStatusService(new HobsonStatusService());
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = newRouter();
        router.attach(ActionResource.PATH, ActionResource.class);
        router.attach(ActionsResource.PATH, ActionsResource.class);
        router.attach(HubInfoResource.PATH, HubInfoResource.class);
        router.attach(DeviceResource.PATH, DeviceResource.class);
        router.attach(DeviceConfigurationResource.PATH, DeviceConfigurationResource.class);
        router.attach(DeviceTelemetryResource.PATH, DeviceTelemetryResource.class);
        router.attach(DeviceVariableChangeIdsResource.PATH, DeviceVariableChangeIdsResource.class);
        router.attach(DevicesResource.PATH, DevicesResource.class);
        router.attach(DeviceVariableResource.PATH, DeviceVariableResource.class);
        router.attach(DeviceVariablesResource.PATH, DeviceVariablesResource.class);
        router.attach(EnableDeviceTelemetryResource.PATH, EnableDeviceTelemetryResource.class);
        router.attach(ExecuteTaskResource.PATH, ExecuteTaskResource.class);
        router.attach(GlobalVariableResource.PATH, GlobalVariableResource.class);
        router.attach(GlobalVariablesResource.PATH, GlobalVariablesResource.class);
        router.attach(HubConfigurationResource.PATH, HubConfigurationResource.class);
        router.attach(HubPasswordResource.PATH, HubPasswordResource.class);
        router.attach(LogResource.PATH, LogResource.class);
        router.attach(MediaProxyResource.PATH, MediaProxyResource.class);
        router.attach(PluginConfigurationResource.PATH, PluginConfigurationResource.class);
        router.attach(PluginCurrentVersionResource.PATH, PluginCurrentVersionResource.class);
        router.attach(PluginDevicesResource.PATH, PluginDevicesResource.class);
        router.attach(PluginInstallResource.PATH, PluginInstallResource.class);
        router.attach(PluginReloadResource.PATH, PluginReloadResource.class);
        router.attach(PluginResource.PATH, PluginResource.class);
        router.attach(PluginsResource.PATH, PluginsResource.class);
        router.attach(PresenceEntitiesResource.PATH, PresenceEntitiesResource.class);
        router.attach(ShutdownResource.PATH, ShutdownResource.class);
        router.attach(TaskResource.PATH, TaskResource.class);
        router.attach(TasksResource.PATH, TasksResource.class);

        authenticator.setNext(router);

        return authenticator;
    }

    public String getApiRoot() {
        return apiRoot;
    }
}
