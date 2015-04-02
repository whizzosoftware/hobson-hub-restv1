/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.action.HobsonAction;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.PluginType;
import com.whizzosoftware.hobson.api.util.VersionUtil;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.resource.LogResource;
import com.whizzosoftware.hobson.rest.v1.resource.ShutdownResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionsResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubPasswordResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubsResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.HubImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryGroupResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryRootResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntityResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TasksResource;
import com.whizzosoftware.hobson.rest.v1.resource.user.UserResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariableResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariablesResource;
import org.json.JSONObject;
import org.restlet.routing.Template;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for populating DAOs with HATEOAS links.
 *
 * @author Dan Noguerol
 */
public class HATEOASLinkHelper {
    public JSONObject addActionLinks(HobsonRestContext ctx, JSONObject json, HobsonAction action) {
        JSONObject links = new JSONObject();
        json.put("links", links);
        links.put("self", ctx.getApiRoot() + new Template(ActionResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", action.getPluginId(), "actionId", action.getId())));
        return json;
    }

    public JSONObject addDeviceLinks(HobsonRestContext ctx, JSONObject json, HobsonDevice device, boolean details) {
        JSONObject links = new JSONObject();
        json.put("links", links);

        links.put("self", ctx.getApiRoot() + new Template(DeviceResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", device.getPluginId(), "deviceId", device.getId())));
        if (details) {
            Map<String,Object> propMap = createDoubleEntryMap(ctx, "pluginId", device.getPluginId(), "deviceId", device.getId());
            links.put("config", ctx.getApiRoot() + new Template(DeviceConfigurationResource.PATH).format(propMap));
            links.put("variables", ctx.getApiRoot() + new Template(DeviceVariablesResource.PATH).format(propMap));
            links.put("variableEvents", ctx.getApiRoot() + new Template(DeviceVariableChangeIdsResource.PATH).format(propMap));
            if (device.hasTelemetry()) {
                links.put("enableTelemetry", ctx.getApiRoot() + new Template(EnableDeviceTelemetryResource.PATH).format(propMap));
                links.put("telemetry", ctx.getApiRoot() + new Template(DeviceTelemetryResource.PATH).format(propMap));
            }
        }
        return json;
    }

    public JSONObject addDeviceVariableLinks(HobsonRestContext ctx, JSONObject json, String pluginId, String deviceId, String varName) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(DeviceVariableResource.PATH).format(createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "variableName", varName)));
        json.put("links", links);
        return json;
    }

    public JSONObject addGlobalVariableLinks(HobsonRestContext ctx, JSONObject json, String varName) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(GlobalVariableResource.PATH).format(createSingleEntryMap(ctx, "name", varName)));
        json.put("links", links);
        return json;
    }

    public JSONObject addHubDetailsLinks(HobsonRestContext ctx, JSONObject json, String hubId) {
        String apiRoot = ctx.getApiRoot();
        Map<String,Object> emptyMap = createEmptyMap(ctx);

        // create HATEOAS links
        JSONObject links = new JSONObject();
        links.put(ActionsResource.REL, apiRoot + new Template(ActionsResource.PATH).format(emptyMap));
        links.put(DevicesResource.REL, apiRoot + new Template(DevicesResource.PATH).format(emptyMap));
        links.put(GlobalVariablesResource.REL, apiRoot + new Template(GlobalVariablesResource.PATH).format(emptyMap));
        links.put(HubImageResource.REL, apiRoot + new Template(HubImageResource.PATH).format(emptyMap));
        links.put(HubPasswordResource.REL, apiRoot + new Template(HubPasswordResource.PATH).format(emptyMap));
        links.put(ImageLibraryRootResource.REL, apiRoot + new Template(ImageLibraryRootResource.PATH).format(emptyMap));
        links.put(LogResource.REL, apiRoot + new Template(LogResource.PATH).format(emptyMap));
        links.put(PluginsResource.REL, apiRoot + new Template(PluginsResource.PATH).format(emptyMap));
        links.put(PresenceEntitiesResource.REL, apiRoot + new Template(PresenceEntitiesResource.PATH).format(emptyMap));
        links.put(ShutdownResource.REL, apiRoot + new Template(ShutdownResource.PATH).format(emptyMap));
        links.put(TasksResource.REL, apiRoot + new Template(TasksResource.PATH).format(emptyMap));
        links.put("self", apiRoot + new Template(HubResource.PATH).format(createSingleEntryMap(ctx, "hubId", hubId)));
        json.put("links", links);

        return json;
    }

    public JSONObject addHubSummaryLinks(HobsonRestContext ctx, JSONObject json, String hubId) {
        String apiRoot = ctx.getApiRoot();
        JSONObject links = new JSONObject();
        links.put("self", apiRoot + new Template(HubResource.PATH).format(createSingleEntryMap(ctx, "hubId", hubId)));
        json.put("links", links);
        return json;
    }

    public JSONObject addImageLibraryGroupLinks(HobsonRestContext ctx, JSONObject json, String groupId) {
        JSONObject groupLinks = new JSONObject();
        groupLinks.put("self", ctx.getApiRoot() + new Template(ImageLibraryGroupResource.PATH).format(createSingleEntryMap(ctx, "groupId", groupId)));
        json.put("links", groupLinks);
        return json;
    }

    public JSONObject addImageLibraryImageLinks(HobsonRestContext ctx, JSONObject json, String imageId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(ImageLibraryImageResource.PATH).format(createSingleEntryMap(ctx, "imageId", imageId)));
        json.put("links", links);
        return json;
    }

    public JSONObject addPluginDescriptorLinks(HobsonRestContext ctx, JSONObject json, PluginDescriptor pd, Boolean details) {
        // attempt to URL encode the plugin ID
        String encodedId;
        try {
            encodedId = URLEncoder.encode(pd.getId(), "UTF8");
        } catch (UnsupportedEncodingException e) {
            encodedId = pd.getId();
        }

        JSONObject links = new JSONObject();
        json.put("links", links);
        links.put("self", ctx.getApiRoot() + new Template(PluginResource.PATH).format(createSingleEntryMap(ctx, "pluginId", encodedId)));
        if (pd.getType() == PluginType.PLUGIN && pd.getStatus().getStatus() == PluginStatus.Status.RUNNING) {
            links.put(DevicesResource.REL, ctx.getApiRoot() + DevicesResource.PATH);
        }

        if (details != null && details) {
            // determine whether there are current and newer versions of the plugin
            String currentVersionString = pd.getCurrentVersionString();
            String latestVersionString = pd.getLatestVersionString();

            Map<String,Object> pluginIdMap = createSingleEntryMap(ctx, "pluginId", encodedId);

            boolean hasCurrentVersion = (currentVersionString != null);
            boolean hasNewerVersion = (VersionUtil.versionCompare(latestVersionString, currentVersionString) == 1);

            if (hasNewerVersion) {
                String rel;
                if (hasCurrentVersion) {
                    rel = "update";
                } else {
                    rel = "install";
                }
                links.put(rel, ctx.getApiRoot() + new Template(PluginInstallResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", encodedId, "pluginVersion", latestVersionString)));
            }
            if (hasCurrentVersion) {
                links.put("reload", ctx.getApiRoot() + new Template(PluginReloadResource.PATH).format(pluginIdMap));
            }
            links.put("icon", ctx.getApiRoot() + new Template(PluginIconResource.PATH).format(pluginIdMap));
            if (pd.getStatus().getStatus() != PluginStatus.Status.NOT_INSTALLED && pd.isConfigurable()) {
                links.put("configuration", ctx.getApiRoot() + new Template(PluginConfigurationResource.PATH).format(pluginIdMap));
            }
        }

        return json;
    }

    public JSONObject addPresenceEntityLinks(HobsonRestContext ctx, JSONObject json, String entityId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(PresenceEntityResource.PATH).format(createSingleEntryMap(ctx, "entityId", entityId)));
        json.put("links", links);
        return json;
    }

    public JSONObject addTaskLinks(HobsonRestContext ctx, JSONObject json, String providerId, String taskId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(TaskResource.PATH).format(createDoubleEntryMap(ctx, "providerId", providerId, "taskId", taskId)));
        json.put("links", links);
        return json;
    }

    public JSONObject addUserLinks(HobsonRestContext ctx, JSONObject json) {
        JSONObject links = new JSONObject();
        Map<String,Object> emptyMap = createEmptyMap(ctx);
        links.put("self", ctx.getApiRoot() + new Template(UserResource.PATH).format(emptyMap));
        links.put("hubs", ctx.getApiRoot() + new Template(HubsResource.PATH).format(emptyMap));
        json.put("links", links);
        return json;
    }

    public Map<String,Object> createEmptyMap(HobsonRestContext ctx) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        return map;
    }

    public Map<String,Object> createSingleEntryMap(HobsonRestContext ctx, String key, Object value) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        map.put(key, value);
        return map;
    }

    public Map<String,Object> createDoubleEntryMap(HobsonRestContext ctx, String key1, String value1, String key2, String value2) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public Map<String,Object> createTripleEntryMap(HobsonRestContext ctx, String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    public HobsonVariable createMediaVariableOverride(HobsonRestContext ctx, String pluginId, String deviceId, HobsonVariable v) {
        if (v != null && (VariableConstants.IMAGE_STATUS_URL.equals(v.getName()) || VariableConstants.VIDEO_STATUS_URL.equals(v.getName()))) {
            return new HobsonVariableValueOverrider(
                v,
                ctx.getApiRoot() + new Template(MediaProxyResource.PATH).format(createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "mediaId", v.getName())
            ));
        } else {
            return v;
        }
    }
}
