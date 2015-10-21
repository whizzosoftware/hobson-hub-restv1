/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.presence.PresenceEntityContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassType;
import com.whizzosoftware.hobson.api.task.TaskContext;
import com.whizzosoftware.hobson.api.task.action.TaskActionClass;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.resource.activity.ActivityLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.hub.*;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryGroupResource;
import com.whizzosoftware.hobson.rest.v1.resource.image.ImageLibraryImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntityResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.*;
import com.whizzosoftware.hobson.rest.v1.resource.user.UserResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariableResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariablesResource;
import org.json.JSONObject;
import org.restlet.routing.Template;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for populating DTOs with HATEOAS links.
 *
 * @author Dan Noguerol
 */
public class LinkProvider {
    public static final String API_ROOT = "/api/v1";
    public static final String LINKS_NAME = "_links";

    private String apiRoot;

    public LinkProvider() {
        this.apiRoot = API_ROOT;
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

    public Map<String,Object> createTripleEntryMap(HobsonRestContext ctx, String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    public String createShutdownLink(HubContext ctx) {
        Template t = new Template(apiRoot + ShutdownResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        return t.format(values);
    }

    public String createTaskLink(TaskContext ctx) {
        Template t = new Template(apiRoot + TaskResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("taskId", ctx.getTaskId());
        return t.format(values);
    }

    public String createTaskConditionClassesLink(HubContext ctx) {
        Template t = new Template(apiRoot + TaskConditionClassesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        return t.format(values);
    }

    public String createTaskConditionClassLink(PropertyContainerClassContext ctx) {
        Template t = new Template(apiRoot + TaskConditionClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("pluginId", ctx.getPluginId());
        values.put("conditionClassId", ctx.getContainerClassId());
        return t.format(values);
    }

    public PropertyContainerClassContext createTaskConditionClassContext(String link) {
        Template t = new Template(apiRoot + TaskConditionClassResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return PropertyContainerClassContext.create(PluginContext.create(HubContext.create((String) vars.get("userId"), (String) vars.get("hubId")), (String) vars.get("pluginId")), (String) vars.get("conditionClassId"));
    }

    public String createTaskActionClassesLink(HubContext ctx) {
        Template t = new Template(apiRoot + TaskActionClassesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        return t.format(values);
    }

    public String createTaskActionClassLink(PropertyContainerClassContext ctx) {
        Template t = new Template(apiRoot + TaskActionClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("pluginId", ctx.getPluginId());
        values.put("actionClassId", ctx.getContainerClassId());
        return t.format(values);
    }

    public PropertyContainerClassContext createTaskActionClassContext(String link) {
        Template t = new Template(apiRoot + TaskActionClassResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return PropertyContainerClassContext.create((String) vars.get("userId"), (String) vars.get("hubId"), (String) vars.get("pluginId"), null, (String) vars.get("actionClassId"));
    }

    public String createTaskActionSetLink(HubContext ctx, String actionSetId) {
        Template t = new Template(apiRoot + TaskActionSetResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", ctx.getUserId());
        values.put("hubId", ctx.getHubId());
        values.put("actionSetId", actionSetId);
        return t.format(values);
    }

    public String createUserLink(String id) {
        Template t = new Template(apiRoot + UserResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", id);
        return t.format(values);
    }

    public String createHubLink(HubContext context) {
        Template t = new Template(apiRoot + HubResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createHubsLink(String userId) {
        Template t = new Template(apiRoot + HubsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", userId);
        return t.format(values);
    }

    public String createHubConfigurationClassLink(HubContext context) {
        Template t = new Template(apiRoot + HubConfigurationClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createHubConfigurationLink(HubContext context) {
        Template t = new Template(apiRoot + HubConfigurationResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createPropertyContainerLink(PropertyContainerClass pcc) {
        switch (pcc.getType()) {
            case CONDITION:
                return createTaskConditionLink(pcc.getContext());
            case ACTION:
                return createTaskActionLink(pcc.getContext());
            case HUB_CONFIG:
                return createHubConfigurationLink(pcc.getContext().getHubContext());
            case PLUGIN_CONFIG:
                return createLocalPluginConfigurationLink(pcc.getContext().getPluginContext());
            default:
                return null;
        }
    }

    public String createPropertyContainerClassLink(PropertyContainerClassContext pccc, PropertyContainerClassType type) {
        if (pccc != null) {
            switch (type) {
                case CONDITION:
                    return createTaskConditionClassLink(pccc);
                case ACTION:
                    return createTaskActionClassLink(pccc);
                case HUB_CONFIG:
                    return createHubConfigurationClassLink(pccc.getHubContext());
                case PLUGIN_CONFIG:
                    return createLocalPluginConfigurationClassLink(pccc.getPluginContext());
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public String createTaskActionLink(PropertyContainerClassContext pccc) {
        Template t = new Template(apiRoot + TaskActionClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", pccc.getUserId());
        values.put("hubId", pccc.getHubId());
        values.put("pluginId", pccc.getPluginId());
        values.put("actionClassId", pccc.getContainerClassId());
        return t.format(values);
    }

    public String createTaskConditionLink(PropertyContainerClassContext pccc) {
        Template t = new Template(apiRoot + TaskConditionClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", pccc.getUserId());
        values.put("hubId", pccc.getHubId());
        values.put("pluginId", pccc.getPluginId());
        values.put("conditionClassId", pccc.getContainerClassId());
        return t.format(values);
    }

    public String createTasksLink(HubContext context) {
        Template t = new Template(apiRoot + TasksResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }


    public String createDevicesLink(HubContext context) {
        Template t = new Template(apiRoot + DevicesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createLocalPluginLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }


    public String createLocalPluginsLink(HubContext context) {
        Template t = new Template(apiRoot + LocalPluginsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createRemotePluginLink(PluginContext context, String version) {
        Template t = new Template(apiRoot + RemotePluginResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("pluginVersion", version);
        return t.format(values);
    }


    public String createRemotePluginsLink(HubContext context) {
        Template t = new Template(apiRoot + RemotePluginsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }


    public String createDeviceLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }

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


    public String createDeviceConfigurationLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceConfigurationResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }


    public String createDeviceConfigurationClassLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceConfigurationClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }


    public String createLocalPluginConfigurationLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginConfigurationResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }


    public String createLocalPluginConfigurationClassLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginConfigurationClassResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }


    public String createDeviceVariablesLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceVariablesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }


    public String createRemotePluginInstallLink(PluginContext context, String version) {
        Template t = new Template(apiRoot + RemotePluginInstallResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("pluginVersion", version);
        return t.format(values);
    }


    public String createTaskActionSetsLink(HubContext context) {
        Template t = new Template(apiRoot + TaskActionSetsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public DeviceContext createDeviceContext(String link) {
        Template t = new Template(apiRoot + DeviceResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(link, vars);
        return DeviceContext.create(HubContext.create((String) vars.get("userId"), (String) vars.get("hubId")), (String) vars.get("pluginId"), (String) vars.get("deviceId"));
    }

    public String createHubLogLink(HubContext context) {
        Template t = new Template(apiRoot + HubLogResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createLocalPluginIconLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginImageResource.PATH);
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

    public String createDeviceTelemetryLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceTelemetryResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }

    public String createDeviceTelemetryDatasetsLink(DeviceContext context) {
        Template t = new Template(apiRoot + DeviceTelemetryDatasetsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        return t.format(values);
    }

    public String createDeviceTelemetryDatasetLink(DeviceContext context, String varName) {
        Template t = new Template(apiRoot + DeviceTelemetryDatasetResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        values.put("deviceId", context.getDeviceId());
        values.put("datasetId", varName);
        return t.format(values);
    }

    public String createActivityLogLink(HubContext context) {
        Template t = new Template(apiRoot + ActivityLogResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createPluginDevicesLink(HubContext context, String pluginId) {
        Template t = new Template(apiRoot + PluginDevicesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", pluginId);
        return t.format(values);
    }

    public String createPresenceEntitiesLink(HubContext context) {
        Template t = new Template(apiRoot + PresenceEntitiesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createPresenceEntityLink(PresenceEntityContext context) {
        Template t = new Template(apiRoot + PresenceEntityResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("entityId", context.getEntityId());
        return t.format(values);
    }

    public String createLocalPluginReloadLink(PluginContext context) {
        Template t = new Template(apiRoot + LocalPluginReloadResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("pluginId", context.getPluginId());
        return t.format(values);
    }

    public String createRepositoryLink(HubContext context, String uri) {
        Template t = new Template(apiRoot + HubRemoteRepositoryResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("repositoryId", uri);
        return t.format(values);
    }

    public String createRepositoriesLink(HubContext context) {
        Template t = new Template(apiRoot + HubRemoteRepositoriesResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createDeviceBootstrapsLink(HubContext context) {
        Template t = new Template(apiRoot + DeviceBootstrapsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        return t.format(values);
    }

    public String createDeviceBootstrapLink(HubContext context, String bootstrapId) {
        Template t = new Template(apiRoot + DeviceBootstrapResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put("userId", context.getUserId());
        values.put("hubId", context.getHubId());
        values.put("bootstrapId", bootstrapId);
        return t.format(values);
    }
}
