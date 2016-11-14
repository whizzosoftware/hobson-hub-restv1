/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.presence.PresenceEntityContext;
import com.whizzosoftware.hobson.api.presence.PresenceLocationContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassType;
import com.whizzosoftware.hobson.api.task.TaskContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import com.whizzosoftware.hobson.api.variable.GlobalVariableContext;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionClassesResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionSetResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionSetsResource;
import com.whizzosoftware.hobson.rest.v1.resource.activity.ActivityLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubLogResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.hub.*;
import com.whizzosoftware.hobson.rest.v1.resource.job.JobResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceLocationResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntityResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceLocationsResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.*;
import com.whizzosoftware.hobson.rest.v1.resource.data.DataStreamDataResource;
import com.whizzosoftware.hobson.rest.v1.resource.data.DataStreamResource;
import com.whizzosoftware.hobson.rest.v1.resource.data.DataStreamsResource;
import com.whizzosoftware.hobson.rest.v1.resource.user.UserResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariableResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariablesResource;
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
public class RestResourceIdProvider implements IdProvider {
    public static final String API_ROOT = "/api/v1";

    private String apiRoot;

    public RestResourceIdProvider() {
        this.apiRoot = API_ROOT;
    }

    @Override
    public String createShutdownId(HubContext ctx) {
        return new Template(apiRoot + ShutdownResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createTaskId(TaskContext ctx) {
        Template t = new Template(apiRoot + TaskResource.PATH);
        Map<String,String> values = createHubValues(ctx.getHubContext());
        values.put(JSONAttributes.TASK_ID, ctx.getTaskId());
        return t.format(values);
    }

    @Override
    public String createTaskPropertiesId(TaskContext ctx) {
        return null;
    }

    @Override
    public String createTaskConditionClassesId(HubContext ctx) {
        return new Template(apiRoot + TaskConditionClassesResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createTaskConditionClassId(PropertyContainerClassContext ctx) {
        Template t = new Template(apiRoot + TaskConditionClassResource.PATH);
        Map<String,String> values = createPluginValues(ctx.getPluginContext());
        values.put(JSONAttributes.CONDITION_CLASS_ID, ctx.getContainerClassId());
        return t.format(values);
    }

    @Override
    public String createTaskConditionId(TaskContext ctx, String propertyContainerId) {
        return null;
    }

    @Override
    public String createTaskConditionPropertiesId(TaskContext ctx, String propertyContainerId) {
        return null;
    }

    @Override
    public String createTaskConditionsId(TaskContext ctx) {
        return null;
    }

    @Override
    public String createActionClassesId(HubContext ctx) {
        return new Template(apiRoot + ActionClassesResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createActionClassId(PropertyContainerClassContext ctx) {
        Template t;
        Map<String,String> values;
        if (ctx.hasDeviceContext()) {
            t = new Template(apiRoot + DeviceActionClassResource.PATH);
            values = createDeviceValues(DeviceContext.create(ctx.getPluginContext(), ctx.getDeviceId()));
        } else {
            t = new Template(apiRoot + LocalPluginActionClassResource.PATH);
            values = createPluginValues(ctx.getPluginContext());
        }
        values.put(JSONAttributes.ACTION_CLASS_ID, ctx.getContainerClassId());
        return t.format(values);
    }

    @Override
    public String createTaskActionSetId(HubContext ctx, String actionSetId) {
        Template t = new Template(apiRoot + ActionSetResource.PATH);
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.ACTION_SET_ID, actionSetId);
        return t.format(values);
    }

    @Override
    public String createPersonId(String userId) {
        Template t = new Template(apiRoot + UserResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put(JSONAttributes.USER_ID, userId);
        return t.format(values);
    }

    @Override
    public PluginContext createPluginContext(String pluginId) {
        Template t = new Template(apiRoot + LocalPluginResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(pluginId, vars);
        return PluginContext.create(HubContext.create((String)vars.get(JSONAttributes.HUB_ID)), (String)vars.get(JSONAttributes.PLUGIN_ID));
    }

    @Override
    public String createHubId(HubContext ctx) {
        return new Template(apiRoot + HubResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createUserHubsId(String userId) {
        Template t = new Template(apiRoot + HubsResource.PATH);
        Map<String,String> values = new HashMap<>();
        values.put(JSONAttributes.USER_ID, userId);
        return t.format(values);
    }

    @Override
    public String createUsersId() {
        return null;
    }

    @Override
    public DeviceVariableContext createDeviceVariableContext(String variableId) {
        Template t = new Template(apiRoot + DeviceVariableResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(variableId, vars);
        return DeviceVariableContext.create(HubContext.create((String)vars.get(JSONAttributes.HUB_ID)), (String)vars.get(JSONAttributes.PLUGIN_ID), (String)vars.get(JSONAttributes.DEVICE_ID), (String)vars.get(JSONAttributes.VARIABLE_NAME));
    }

    @Override
    public String createJobId(HubContext ctx, String jobId) {
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.JOB_ID, jobId);
        return new Template(apiRoot + JobResource.PATH).format(values);
    }

    @Override
    public String createHubConfigurationClassId(HubContext ctx) {
        return new Template(apiRoot + HubConfigurationClassResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createHubConfigurationId(HubContext ctx) {
        return new Template(apiRoot + HubConfigurationResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createPresenceLocationId(PresenceLocationContext ctx) {
        Template t = new Template(apiRoot + PresenceLocationResource.PATH);
        Map<String,String> values = createHubValues(ctx.getHubContext());
        values.put(JSONAttributes.LOCATION_ID, ctx.getLocationId());
        return t.format(values);
    }

    @Override
    public String createPropertyContainerId(String id, PropertyContainerClass pcc) {
        switch (pcc.getType()) {
            case CONDITION:
            case ACTION: {
                return id;
            }
            case HUB_CONFIG: {
                return createHubConfigurationId(pcc.getContext().getHubContext());
            }
            case PLUGIN_CONFIG: {
                return createLocalPluginConfigurationId(pcc.getContext().getPluginContext());
            }
            case DEVICE_CONFIG: {
                return createDeviceConfigurationId(DeviceContext.create(pcc.getContext().getHubContext(), pcc.getContext().getPluginId(), pcc.getContext().getDeviceId()));
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public String createPropertyContainerClassesId(PluginContext pctx) {
        return null;
    }

    @Override
    public String createPropertyContainerClassId(PropertyContainerClassContext pccc, PropertyContainerClassType type) {
        if (pccc != null) {
            switch (type) {
                case CONDITION:
                    return createTaskConditionClassId(pccc);
                case ACTION:
                    return createActionClassId(pccc);
                case HUB_CONFIG:
                    return createHubConfigurationClassId(pccc.getHubContext());
                case PLUGIN_CONFIG:
                    return createLocalPluginConfigurationClassId(pccc.getPluginContext());
                case DEVICE_CONFIG:
                    return createDeviceConfigurationClassId(DeviceContext.create(pccc.getHubContext(), pccc.getPluginId(), pccc.getDeviceId()));
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String createTasksId(HubContext ctx) {
        return new Template(apiRoot + TasksResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createUserId(String userId) {
        return null;
    }

    @Override
    public String createDevicesId(HubContext ctx) {
        return new Template(apiRoot + DevicesResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createLocalPluginId(PluginContext ctx) {
        return new Template(apiRoot + LocalPluginResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public String createLocalPluginsId(HubContext ctx) {
        return new Template(apiRoot + LocalPluginsResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createRemotePluginsId(HubContext ctx) {
        return new Template(apiRoot + RemotePluginsResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createRemotePluginId(HubContext ctx, String pluginId, String version) {
        Template t = new Template(apiRoot + RemotePluginResource.PATH);
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.PLUGIN_ID, pluginId);
        values.put(JSONAttributes.PLUGIN_VERSION, version);
        return t.format(values);
    }

    @Override
    public String createDeviceId(DeviceContext ctx) {
        return new Template(apiRoot + DeviceResource.PATH).format(createDeviceValues(ctx));
    }

    @Override
    public String createDeviceConfigurationId(DeviceContext ctx) {
        return new Template(apiRoot + DeviceConfigurationResource.PATH).format(createDeviceValues(ctx));
    }

    @Override
    public String createDeviceNameId(DeviceContext ctx) {
        return new Template(apiRoot + DeviceNameResource.PATH).format(createDeviceValues(ctx));
    }

    @Override
    public String createDeviceVariableDescriptionId(DeviceVariableContext vctx) {
        return null;
    }

    @Override
    public String createDeviceVariableId(DeviceVariableContext vctx) {
        Template t = new Template(apiRoot + DeviceVariableResource.PATH);
        Map<String,String> values = createDeviceValues(vctx.getDeviceContext());
        values.put(JSONAttributes.VARIABLE_NAME, vctx.getName());
        return t.format(values);
    }

    @Override
    public String createDeviceConfigurationClassId(DeviceContext ctx) {
        return new Template(apiRoot + DeviceConfigurationClassResource.PATH).format(createDeviceValues(ctx));
    }

    @Override
    public String createLocalPluginActionClassesId(PluginContext ctx) {
        return new Template(apiRoot + LocalPluginActionClassesResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public String createPluginDeviceConfigurationClassesId(PluginContext ctx) {
        return new Template(apiRoot + LocalPluginDeviceConfigurationClassesResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public String createPluginDeviceConfigurationClassId(PluginContext ctx, String name) {
        Template t = new Template(apiRoot + LocalPluginDeviceConfigurationClassResource.PATH);
        Map<String,String> map = createPluginValues(ctx);
        map.put(JSONAttributes.NAME, name);
        return t.format(map);
    }

    @Override
    public String createLocalPluginConfigurationId(PluginContext ctx) {
        return new Template(apiRoot + LocalPluginConfigurationResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public String createLocalPluginConfigurationClassId(PluginContext ctx) {
        return new Template(apiRoot + LocalPluginConfigurationClassResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public String createDeviceVariablesId(DeviceContext ctx) {
        return new Template(apiRoot + DeviceVariablesResource.PATH).format(createDeviceValues(ctx));
    }

    @Override
    public String createRemotePluginInstallId(HubContext ctx, String pluginId, String version) {
        Template t = new Template(apiRoot + RemotePluginInstallResource.PATH);
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.PLUGIN_ID, pluginId);
        values.put(JSONAttributes.PLUGIN_VERSION, version);
        return t.format(values);
    }

    @Override
    public String createTaskActionSetsId(HubContext ctx) {
        return new Template(apiRoot + ActionSetsResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createHubLogId(HubContext ctx) {
        return new Template(apiRoot + HubLogResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createHubPasswordId(HubContext ctx) {
        return new Template(apiRoot + HubPasswordResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createHubSerialPortsId(HubContext ctx) {
        return new Template(apiRoot + HubSerialPortsResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createHubSerialPortId(HubContext ctx, String name) {
        return name;
    }

    @Override
    public String createLocalPluginIconId(PluginContext ctx) {
        return new Template(apiRoot + LocalPluginImageResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public String createGlobalVariablesId(HubContext ctx) {
        return new Template(apiRoot + GlobalVariablesResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createGlobalVariableId(GlobalVariableContext gvctx) {
        Template t = new Template(apiRoot + GlobalVariableResource.PATH);
        Map<String,String> values = createHubValues(gvctx.getHubContext());
        values.put(JSONAttributes.NAME, gvctx.getName());
        return t.format(values);
    }

    @Override
    public String createActionId(HubContext ctx, String actionId) {
        return null;
    }

    @Override
    public String createActionSetId(HubContext ctx, String actionSetId) {
        return null;
    }

    @Override
    public String createActionSetActionsId(HubContext ctx, String actionSetId) {
        return null;
    }

    @Override
    public String createActionSetsId(HubContext ctx) {
        return null;
    }

    @Override
    public String createActionPropertiesId(HubContext ctx, String actionId) {
        return null;
    }

    @Override
    public String createActivityLogId(HubContext ctx) {
        return new Template(apiRoot + ActivityLogResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createDataStreamsId() {
        return new Template(apiRoot + DataStreamsResource.PATH).format((Map<String,?>)null);
    }

    @Override
    public String createDataStreamId(String dataStreamId) {
        return createDataStreamPathId(DataStreamResource.PATH, dataStreamId);
    }

    @Override
    public String createDataStreamDataId(String dataStreamId) {
        return createDataStreamPathId(DataStreamDataResource.PATH, dataStreamId);
    }

    private String createDataStreamPathId(String path, String dataStreamId) {
        Template t = new Template(apiRoot + path);
        Map<String,String> values = new HashMap<>();
        values.put(JSONAttributes.DATA_STREAM_ID, dataStreamId);
        return t.format(values);
    }

    @Override
    public String createDataStreamFieldsId(String dataStreamId) {
        return null;
    }

    @Override
    public String createDataStreamTagsId(String dataStreamId) {
        return null;
    }

    @Override
    public String createDataStreamFieldId(String dataStreamId, String fieldId) {
        return null;
    }

    @Override
    public String createPluginDevicesId(PluginContext ctx) {
        return new Template(apiRoot + PluginDevicesResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public String createPresenceEntitiesId(HubContext ctx) {
        return new Template(apiRoot + PresenceEntitiesResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createPresenceEntityId(PresenceEntityContext ctx) {
        Template t = new Template(apiRoot + PresenceEntityResource.PATH);
        Map<String,String> values = createHubValues(ctx.getHubContext());
        values.put(JSONAttributes.ENTITY_ID, ctx.getEntityId());
        return t.format(values);
    }

    @Override
    public String createRepositoryId(HubContext ctx, String uri) {
        try {
            Template t = new Template(apiRoot + HubRemoteRepositoryResource.PATH);
            Map<String,String> values = createHubValues(ctx);
            values.put(JSONAttributes.REPOSITORY_ID, URLEncoder.encode(uri, "UTF8"));
            return t.format(values);
        } catch (UnsupportedEncodingException e) {
            throw new HobsonRuntimeException("UTF8 is not supported on this platform", e);
        }
    }

    @Override
    public String createSendTestEmailId(HubContext ctx) {
        return new Template(apiRoot + HubSendTestEmailResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createRepositoriesId(HubContext ctx) {
        return new Template(apiRoot + HubRemoteRepositoriesResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createPresenceLocationsId(HubContext ctx) {
        return new Template(apiRoot + PresenceLocationsResource.PATH).format(createHubValues(ctx));
    }

    @Override
    public String createLocalPluginReloadId(PluginContext ctx) {
        return new Template(apiRoot + LocalPluginReloadResource.PATH).format(createPluginValues(ctx));
    }

    @Override
    public DeviceContext createDeviceContext(String deviceId) {
        Template t = new Template(apiRoot + DeviceResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(deviceId, vars);
        return DeviceContext.create(HubContext.create((String)vars.get(JSONAttributes.HUB_ID)), (String)vars.get(JSONAttributes.PLUGIN_ID), (String)vars.get(JSONAttributes.DEVICE_ID));
    }

    @Override
    public DeviceContext createDeviceContextWithHub(HubContext ctx, String deviceId) {
        Template t = new Template(apiRoot + DeviceResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(deviceId, vars);
        return DeviceContext.create(ctx, (String)vars.get(JSONAttributes.PLUGIN_ID), (String)vars.get(JSONAttributes.DEVICE_ID));
    }

    @Override
    public String createDeviceActionClassId(DeviceContext ctx, String actionClassId) {
        Template t = new Template(apiRoot + DeviceActionClassResource.PATH);
        Map<String,String> map = createDeviceValues(ctx);
        map.put(JSONAttributes.ACTION_CLASS_ID, actionClassId);
        return t.format(map);
    }

    @Override
    public String createDeviceActionClassesId(DeviceContext ctx) {
        return new Template(apiRoot + DeviceActionClassesResource.PATH).format(createDeviceValues(ctx));
    }

    @Override
    public PresenceEntityContext createPresenceEntityContext(String presenceEntityId) {
        Template t = new Template(apiRoot + PresenceEntityResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(presenceEntityId, vars);
        return PresenceEntityContext.create(HubContext.create((String)vars.get(JSONAttributes.HUB_ID)), (String)vars.get(JSONAttributes.ENTITY_ID));
    }

    @Override
    public PresenceLocationContext createPresenceLocationContext(String presenceLocationId) {
        Template t = new Template(apiRoot + PresenceLocationResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(presenceLocationId, vars);
        return PresenceLocationContext.create(HubContext.create((String)vars.get(JSONAttributes.HUB_ID)), (String)vars.get(JSONAttributes.LOCATION_ID));
    }

    private Map<String,String> createHubValues(HubContext ctx) {
        Map<String,String> values = new HashMap<>();
        values.put(JSONAttributes.HUB_ID, ctx.getHubId());
        return values;
    }

    private Map<String,String> createPluginValues(PluginContext ctx) {
        Map<String,String> values = createHubValues(ctx.getHubContext());
        values.put(JSONAttributes.PLUGIN_ID, ctx.getPluginId());
        return values;
    }

    private Map<String,String> createDeviceValues(DeviceContext ctx) {
        Map<String,String> values = createPluginValues(ctx.getPluginContext());
        values.put(JSONAttributes.DEVICE_ID, ctx.getDeviceId());
        return values;
    }
}
