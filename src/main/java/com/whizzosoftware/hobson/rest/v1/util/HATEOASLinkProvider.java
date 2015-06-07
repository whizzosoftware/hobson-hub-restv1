/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.PluginType;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.task.TaskContext;
import com.whizzosoftware.hobson.dto.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.LinkProvider;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.ShutdownResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.hub.*;
import com.whizzosoftware.hobson.rest.v1.resource.image.HubImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryGroupResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryRootResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntityResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.*;
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
 * A helper class for populating DTOs with HATEOAS links.
 *
 * @author Dan Noguerol
 */
public class HATEOASLinkProvider implements LinkProvider {
    private static final String LINKS_NAME = "_links";

    private String apiRoot;

    public HATEOASLinkProvider() {
        this.apiRoot = "/api/v1";
    }

    public JSONObject addDeviceConfigurationLinks(HobsonRestContext ctx, JSONObject json, String pluginId, String deviceId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(DeviceConfigurationResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addDeviceLinks(HobsonRestContext ctx, JSONObject json, HobsonDevice device, boolean details) {
        JSONObject links = new JSONObject();
        json.put(LINKS_NAME, links);

        String pluginId = device.getContext().getPluginId();
        String deviceId = device.getContext().getDeviceId();

        links.put("self", ctx.getApiRoot() + new Template(DeviceResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId)));
        if (details) {
            Map<String,Object> propMap = createDoubleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId);
            links.put("config", ctx.getApiRoot() + new Template(DeviceConfigurationResource.PATH).format(propMap));
            links.put("variables", ctx.getApiRoot() + new Template(DeviceVariablesResource.PATH).format(propMap));

            if (json.has("telemetry")) {
                JSONObject tlinks = new JSONObject();
                tlinks.put("self", ctx.getApiRoot() + new Template(DeviceTelemetryResource.PATH).format(propMap));
                json.getJSONObject("telemetry").put(LINKS_NAME, tlinks);
            }
        }

        // add link for preferred variable if present
        if (json.has("preferredVariable")) {
            JSONObject pv = json.getJSONObject("preferredVariable");
            JSONObject pvLinks = new JSONObject();
            pvLinks.put("self", ctx.getApiRoot() + new Template(DeviceVariableResource.PATH).format(
                createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "variableName", pv.getString("name"))
            ));
            pv.put(LINKS_NAME, pvLinks);
        }

        return json;
    }

    public JSONObject addDeviceVariableLinks(HobsonRestContext ctx, JSONObject json, String pluginId, String deviceId, String varName) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(DeviceVariableResource.PATH).format(createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "variableName", varName)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addGlobalVariableLinks(HobsonRestContext ctx, JSONObject json, String varName) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(GlobalVariableResource.PATH).format(createSingleEntryMap(ctx, "name", varName)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addHubDetailsLinks(HobsonRestContext ctx, JSONObject json, String hubId) {
        String apiRoot = ctx.getApiRoot();
        Map<String,Object> emptyMap = createEmptyMap(ctx);

        // create HATEOAS links
        JSONObject links = new JSONObject();
        links.put(DevicesResource.REL, apiRoot + new Template(DevicesResource.PATH).format(emptyMap));
        links.put(GlobalVariablesResource.REL, apiRoot + new Template(GlobalVariablesResource.PATH).format(emptyMap));
        links.put(HubImageResource.REL, apiRoot + new Template(HubImageResource.PATH).format(emptyMap));
        links.put(HubPasswordResource.REL, apiRoot + new Template(HubPasswordResource.PATH).format(emptyMap));
        links.put(ImageLibraryRootResource.REL, apiRoot + new Template(ImageLibraryRootResource.PATH).format(emptyMap));
        links.put(HubLogResource.REL, apiRoot + new Template(HubLogResource.PATH).format(emptyMap));
        links.put(PresenceEntitiesResource.REL, apiRoot + new Template(PresenceEntitiesResource.PATH).format(emptyMap));
        links.put(ShutdownResource.REL, apiRoot + new Template(ShutdownResource.PATH).format(emptyMap));
        links.put(TasksResource.REL, apiRoot + new Template(TasksResource.PATH).format(emptyMap));
        links.put("self", apiRoot + new Template(HubResource.PATH).format(createSingleEntryMap(ctx, "hubId", hubId)));
        json.put(LINKS_NAME, links);

        return json;
    }

    public JSONObject addHubSummaryLinks(HobsonRestContext ctx, JSONObject json, String hubId) {
        String apiRoot = ctx.getApiRoot();
        JSONObject links = new JSONObject();
        links.put("self", apiRoot + new Template(HubResource.PATH).format(createSingleEntryMap(ctx, "hubId", hubId)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addImageLibraryGroupLinks(HobsonRestContext ctx, JSONObject json, String groupId) {
        JSONObject groupLinks = new JSONObject();
        groupLinks.put("self", ctx.getApiRoot() + new Template(ImageLibraryGroupResource.PATH).format(createSingleEntryMap(ctx, "groupId", groupId)));
        json.put(LINKS_NAME, groupLinks);
        return json;
    }

    public JSONObject addImageLibraryImageLinks(HobsonRestContext ctx, JSONObject json, String imageId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(ImageLibraryImageResource.PATH).format(createSingleEntryMap(ctx, "imageId", imageId)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addPluginConfigurationLinks(HobsonRestContext ctx, JSONObject json, String pluginId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(LocalPluginConfigurationResource.PATH).format(createSingleEntryMap(ctx, "pluginId", pluginId)));
        json.put(LINKS_NAME, links);
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
        json.put(LINKS_NAME, links);
        links.put("self", ctx.getApiRoot() + new Template(PluginResource.PATH).format(createSingleEntryMap(ctx, "pluginId", encodedId)));
        if (pd.getType() == PluginType.PLUGIN && pd.getStatus().getStatus() == PluginStatus.Status.RUNNING) {
            links.put(DevicesResource.REL, ctx.getApiRoot() + DevicesResource.PATH);
        }

        if (details != null && details) {
            // determine whether there are current and newer versions of the plugin
//            String currentVersionString = pd.getCurrentVersionString();
//            String latestVersionString = pd.getLatestVersionString();

            Map<String,Object> pluginIdMap = createSingleEntryMap(ctx, "pluginId", encodedId);

//            boolean hasCurrentVersion = (currentVersionString != null);
//            boolean hasNewerVersion = (VersionUtil.versionCompare(latestVersionString, currentVersionString) == 1);

//            if (hasNewerVersion) {
//                String rel;
//                if (hasCurrentVersion) {
//                    rel = "update";
//                } else {
//                    rel = "install";
//                }
//                links.put(rel, ctx.getApiRoot() + new Template(RemotePluginInstallResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", encodedId, "pluginVersion", latestVersionString)));
//            }
//            if (hasCurrentVersion) {
//                links.put("reload", ctx.getApiRoot() + new Template(PluginReloadResource.PATH).format(pluginIdMap));
//            }
            links.put("icon", ctx.getApiRoot() + new Template(LocalPluginIconResource.PATH).format(pluginIdMap));
            if (pd.getStatus().getStatus() != PluginStatus.Status.NOT_INSTALLED && pd.isConfigurable()) {
                links.put("configuration", ctx.getApiRoot() + new Template(LocalPluginConfigurationResource.PATH).format(pluginIdMap));
            }
        }

        return json;
    }

    public JSONObject addPresenceEntityLinks(HobsonRestContext ctx, JSONObject json, String entityId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(PresenceEntityResource.PATH).format(createSingleEntryMap(ctx, "entityId", entityId)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addTaskLinks(HobsonRestContext ctx, JSONObject json, String pluginId, String taskId) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(TaskResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", pluginId, "taskId", taskId)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addTelemetryLinks(HobsonRestContext ctx, String pluginId, String deviceId, JSONObject json) {
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(DeviceTelemetryResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId)));
        json.put(LINKS_NAME, links);
        return json;
    }

    public JSONObject addUserLinks(HobsonRestContext ctx, JSONObject json) {
        JSONObject links = new JSONObject();
        Map<String,Object> emptyMap = createEmptyMap(ctx);
        links.put("self", ctx.getApiRoot() + new Template(UserResource.PATH).format(emptyMap));
        links.put("hubs", ctx.getApiRoot() + new Template(HubsResource.PATH).format(emptyMap));
        json.put(LINKS_NAME, links);
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

    @Override
    public HubContext createHubContext(String link) {
        Template t = new Template(apiRoot + HubResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return HubContext.create((String)vars.get("userId"), (String)vars.get("hubId"));
    }

    @Override
    public String createTaskLink(TaskContext ctx) {
        Template t = new Template(apiRoot + TaskResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("pluginId", ctx.getPluginId());
        values.put("taskId", ctx.getTaskId());
        return t.format(values);
    }

    @Override
    public TaskContext createTaskContext(String string) {
        return null;
    }

    @Override
    public String createTaskConditionClassesLink(HubContext ctx) {
        Template t = new Template(apiRoot + TaskConditionClassesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        return t.format(values);
    }

    @Override
    public String createTaskConditionClassLink(PropertyContainerClassContext ctx) {
        Template t = new Template(apiRoot + TaskConditionClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("pluginId", ctx.getPluginId());
        values.put("conditionClassId", ctx.getContainerClassId());
        return t.format(values);
    }

    @Override
    public PropertyContainerClassContext createTaskConditionClassContext(String link) {
        Template t = new Template(apiRoot + TaskConditionClassResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return PropertyContainerClassContext.create(PluginContext.create(HubContext.create((String) vars.get("userId"), (String) vars.get("hubId")), (String)vars.get("pluginId")), (String)vars.get("conditionClassId"));
    }

    @Override
    public String createTaskActionClassesLink(HubContext ctx) {
        Template t = new Template(apiRoot + TaskActionClassesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        return t.format(values);
    }

    @Override
    public String createTaskActionClassLink(PropertyContainerClassContext ctx) {
        Template t = new Template(apiRoot + TaskActionClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("pluginId", ctx.getPluginId());
        values.put("actionClassId", ctx.getContainerClassId());
        return t.format(values);
    }

    @Override
    public PropertyContainerClassContext createTaskActionClassContext(String link) {
        Template t = new Template(apiRoot + TaskActionClassResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return PropertyContainerClassContext.create((String)vars.get("userId"), (String)vars.get("hubId"), (String)vars.get("pluginId"), (String)vars.get("actionClassId"));
    }

    @Override
    public String createTaskActionSetLink(HubContext ctx, String actionSetId) {
        Template t = new Template(apiRoot + TaskActionSetResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("actionSetId", actionSetId);
        return t.format(values);
    }

    @Override
    public PluginContext createPluginContext(String link) {
        Template t = new Template(apiRoot + PluginResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return PluginContext.create(HubContext.create((String) vars.get("userId"), (String) vars.get("hubId")), (String) vars.get("pluginId"));
    }

    @Override
    public String createUserLink(String id) {
        Template t = new Template(apiRoot + UserResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", id);
        return t.format(values);
    }

    @Override
    public String createHubLink(HubContext context) {
        Template t = new Template(apiRoot + HubResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public String createHubsLink(String userId) {
        Template t = new Template(apiRoot + HubsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", userId);
        return t.format(values);
    }

    @Override
    public String createHubConfigurationClassLink(HubContext context) {
        Template t = new Template(apiRoot + HubConfigurationClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public String createHubConfigurationLink(HubContext context) {
        Template t = new Template(apiRoot + HubConfigurationResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public String createPropertyContainerLink(HubContext context, int type) {
        if (type == LinkProvider.CONDITION_CONTAINER) {
            return createTaskConditionLink();
        } else if (type == LinkProvider.ACTION_CONTAINER) {
            return createTaskActionLink();
        } else if (type == LinkProvider.HUB_CONFIG_CONTAINER) {
            return createHubConfigurationLink(context);
        } else {
            return null;
        }
    }

    @Override
    public String createPropertyContainerClassLink(int type, PropertyContainerClassContext pccc) {
        if (type == LinkProvider.CONDITION_CONTAINER) {
            return createTaskConditionClassLink(pccc);
        } else if (type == LinkProvider.ACTION_CONTAINER) {
            return createTaskActionClassLink(pccc);
        } else if (type == LinkProvider.HUB_CONFIG_CONTAINER) {
            return createHubConfigurationClassLink(pccc.getHubContext());
        } else {
            return null;
        }
    }

    public String createTaskActionLink() {
        return null;
    }

    public String createTaskConditionLink() {
        return null;
    }

    @Override
    public String createTasksLink(HubContext context) {
        Template t = new Template(apiRoot + TasksResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public String createDevicesLink(HubContext context) {
        Template t = new Template(apiRoot + DevicesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public String createLocalPluginLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }

    @Override
    public String createLocalPluginsLink(HubContext context) {
        Template t = new Template(apiRoot + LocalPluginsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public String createRemotePluginLink(PluginContext context) {
        Template t = new Template(apiRoot + RemotePluginResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }

    @Override
    public String createRemotePluginsLink(HubContext context) {
        Template t = new Template(apiRoot + RemotePluginsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public String createDeviceLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }

    @Override
    public String createDeviceVariableLink(DeviceContext context, String preferredVariableName) {
        Template t = new Template(apiRoot + DeviceVariableResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        values.put("variableName", preferredVariableName);
        return t.format(values);
    }

    @Override
    public String createDeviceConfigurationLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceConfigurationResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }

    @Override
    public String createDeviceConfigurationClassLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceConfigurationClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }

    @Override
    public String createLocalPluginConfigurationLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginConfigurationResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }

    @Override
    public String createLocalPluginConfigurationClassLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginConfigurationClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }

    @Override
    public String createDeviceVariablesLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceVariablesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }

    @Override
    public String createRemotePluginInstallLink(PluginContext context, String version) {
        Template t = new Template(apiRoot + RemotePluginInstallResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("pluginVersion", version);
        return t.format(values);
    }

    @Override
    public String createTaskActionSetsLink(HubContext context) {
        Template t = new Template(apiRoot + TaskActionSetsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    @Override
    public DeviceContext createDeviceContext(String link) {
        Template t = new Template(apiRoot + DeviceResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return DeviceContext.create(HubContext.create((String)vars.get("userId"), (String)vars.get("hubId")), (String)vars.get("pluginId"), (String)vars.get("deviceId"));
    }

    public String createHubLogLink(HubContext context) {
        Template t = new Template(apiRoot + HubLogResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public void addRemotePluginLinks(HobsonRestContext ctx, PluginDescriptor pd, HobsonPluginDTO dto) {
        String encodedId;
        try {
            encodedId = URLEncoder.encode(pd.getId(), "UTF8");
        } catch (UnsupportedEncodingException e) {
            encodedId = pd.getId();
        }
        dto.addLink("install", ctx.getApiRoot() + new Template(RemotePluginInstallResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", encodedId, "pluginVersion", pd.getVersionString())));
    }

    public String createLocalPluginIconLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginIconResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }

    public String createGlobalVariablesLink(HubContext context) {
        Template t = new Template(apiRoot + GlobalVariablesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createGlobalVariableLink(HubContext context, String name) {
        Template t = new Template(apiRoot + GlobalVariableResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("name", name);
        return t.format(values);
    }
}
