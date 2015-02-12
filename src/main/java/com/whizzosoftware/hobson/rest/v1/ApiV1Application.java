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
import com.whizzosoftware.hobson.rest.v1.resource.image.HubImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryGroupResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryRootResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.ExecuteTaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TasksResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariableResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariablesResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.Status;
import org.restlet.ext.guice.ResourceInjectingApplication;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;

import java.util.ArrayList;

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
        // create the router with all of our resource classes attached
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
        router.attach(HubImageResource.PATH, HubImageResource.class);
        router.attach(HubPasswordResource.PATH, HubPasswordResource.class);
        router.attach(ImageLibraryGroupResource.PATH, ImageLibraryGroupResource.class);
        router.attach(ImageLibraryImageResource.PATH, ImageLibraryImageResource.class);
        router.attach(ImageLibraryRootResource.PATH, ImageLibraryRootResource.class);
        router.attach(LogResource.PATH, LogResource.class);
        router.attach(MediaProxyResource.PATH, MediaProxyResource.class);
        router.attach(PluginConfigurationResource.PATH, PluginConfigurationResource.class);
        router.attach(PluginCurrentVersionResource.PATH, PluginCurrentVersionResource.class);
        router.attach(PluginDevicesResource.PATH, PluginDevicesResource.class);
        router.attach(PluginIconResource.PATH, PluginIconResource.class);
        router.attach(PluginInstallResource.PATH, PluginInstallResource.class);
        router.attach(PluginReloadResource.PATH, PluginReloadResource.class);
        router.attach(PluginResource.PATH, PluginResource.class);
        router.attach(PluginsResource.PATH, PluginsResource.class);
        router.attach(PresenceEntitiesResource.PATH, PresenceEntitiesResource.class);
        router.attach(ShutdownResource.PATH, ShutdownResource.class);
        router.attach(TaskResource.PATH, TaskResource.class);
        router.attach(TasksResource.PATH, TasksResource.class);

        // create a filter that prevents caching of API responses
        Filter cache = new Filter(getContext(), router) {
            protected void afterHandle(Request request, Response response) {
                super.afterHandle(request, response);
                if (response != null && response.getEntity() != null) {
                    if (response.getStatus().equals(Status.SUCCESS_OK)) {
                        response.setCacheDirectives(new ArrayList<CacheDirective>());
                        response.getCacheDirectives().add(CacheDirective.noCache());
                    }
                }
            }
        };

        authenticator.setNext(cache);

        // make the authenticator the first restlet in the chain
        return authenticator;
    }

    public String getApiRoot() {
        return apiRoot;
    }
}
