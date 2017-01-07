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
import com.whizzosoftware.hobson.api.persist.TemplatedId;
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
import com.whizzosoftware.hobson.rest.v1.resource.data.DataStreamFieldResource;
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
import com.whizzosoftware.hobson.rest.v1.resource.user.UsersResource;
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
    public TemplatedId createShutdownId(HubContext ctx) {
        String s = apiRoot + ShutdownResource.PATH;
        return new TemplatedId(new Template(s).format(createHubValues(ctx)), s);
    }

    @Override
    public TemplatedId createTaskId(TaskContext ctx) {
        String s = apiRoot + TaskResource.PATH;
        Map<String,String> values = createHubValues(ctx.getHubContext());
        values.put(JSONAttributes.TASK_ID, ctx.getTaskId());
        return new TemplatedId(new Template(s).format(values), s);
    }

    @Override
    public TemplatedId createTaskPropertiesId(TaskContext ctx) {
        return new TemplatedId(null, null);
    }

    @Override
    public TemplatedId createTaskConditionClassesId(HubContext ctx) {
        String s = apiRoot + TaskConditionClassesResource.PATH;
        return new TemplatedId(
            new Template(s).format(createHubValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createTaskConditionClassId(PropertyContainerClassContext ctx) {
        String s = apiRoot + TaskConditionClassResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = createPluginValues(ctx.getPluginContext());
        values.put(JSONAttributes.CONDITION_CLASS_ID, ctx.getContainerClassId());
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createTaskConditionId(TaskContext ctx, String propertyContainerId) {
        return new TemplatedId(null, null);
    }

    @Override
    public TemplatedId createTaskConditionPropertiesId(TaskContext ctx, String propertyContainerId) {
        return new TemplatedId(null, null);
    }

    @Override
    public TemplatedId createTaskConditionsId(TaskContext ctx) {
        return new TemplatedId(null, null);
    }

    @Override
    public TemplatedId createActionClassesId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + ActionClassesResource.PATH).format(createHubValues(ctx)),
            apiRoot + ActionClassesResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createActionClassId(PropertyContainerClassContext ctx) {
        String s;
        Template t;
        Map<String,String> values;
        if (ctx.hasDeviceContext()) {
            s = apiRoot + DeviceActionClassResource.PATH;
            t = new Template(s);
            values = createDeviceValues(DeviceContext.create(ctx.getPluginContext(), ctx.getDeviceId()));
        } else {
            s = apiRoot + LocalPluginActionClassResource.PATH;
            t = new Template(s);
            values = createPluginValues(ctx.getPluginContext());
        }
        values.put(JSONAttributes.ACTION_CLASS_ID, ctx.getContainerClassId());
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createTaskActionSetId(HubContext ctx, String actionSetId) {
        String s = apiRoot + ActionSetResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.ACTION_SET_ID, actionSetId);
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createPersonId(String userId) {
        String s = apiRoot + UserResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = new HashMap<>();
        values.put(JSONAttributes.USER_ID, userId);
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public PluginContext createPluginContext(String pluginId) {
        Template t = new Template(apiRoot + LocalPluginResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(pluginId, vars);
        return PluginContext.create(HubContext.create((String)vars.get(JSONAttributes.HUB_ID)), (String)vars.get(JSONAttributes.PLUGIN_ID));
    }

    @Override
    public TemplatedId createHubId(HubContext ctx) {
        String s = apiRoot + HubResource.PATH;
        return new TemplatedId(
            new Template(s).format(createHubValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createUserHubsId(String userId) {
        Map<String,String> values = new HashMap<>();
        values.put(JSONAttributes.USER_ID, userId);
        return new TemplatedId(new Template(apiRoot + HubsResource.PATH).format(values), apiRoot + HubsResource.TEMPLATE);
    }

    @Override
    public TemplatedId createUsersId() {
        String s = apiRoot + UsersResource.PATH;
        return new TemplatedId(s, s);
    }

    @Override
    public DeviceVariableContext createDeviceVariableContext(String variableId) {
        Template t = new Template(apiRoot + DeviceVariableResource.PATH);
        Map<String,Object> vars = new HashMap<>();
        t.parse(variableId, vars);
        return DeviceVariableContext.create(HubContext.create((String)vars.get(JSONAttributes.HUB_ID)), (String)vars.get(JSONAttributes.PLUGIN_ID), (String)vars.get(JSONAttributes.DEVICE_ID), (String)vars.get(JSONAttributes.VARIABLE_NAME));
    }

    @Override
    public TemplatedId createJobId(HubContext ctx, String jobId) {
        String s = apiRoot + JobResource.PATH;
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.JOB_ID, jobId);
        return new TemplatedId(
            new Template(s).format(values),
            s
        );
    }

    @Override
    public TemplatedId createHubConfigurationClassId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + HubConfigurationClassResource.PATH).format(createHubValues(ctx)),
            apiRoot + HubConfigurationClassResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createHubConfigurationId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + HubConfigurationResource.PATH).format(createHubValues(ctx)),
            apiRoot + HubConfigurationResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createPresenceLocationId(PresenceLocationContext ctx) {
        String s = apiRoot + PresenceLocationResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = createHubValues(ctx.getHubContext());
        values.put(JSONAttributes.LOCATION_ID, ctx.getLocationId());
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createPropertyContainerId(String id, PropertyContainerClass pcc) {
        switch (pcc.getType()) {
            case CONDITION: {
                return new TemplatedId(id, "{conditionId}");
            }
            case ACTION: {
                return new TemplatedId(id, "{actionId}");
            }
            case HUB_CONFIG: {
                return new TemplatedId(createHubConfigurationId(pcc.getContext().getHubContext()).getId(), apiRoot + HubConfigurationResource.TEMPLATE);
            }
            case PLUGIN_CONFIG: {
                return new TemplatedId(createLocalPluginConfigurationId(pcc.getContext().getPluginContext()).getId(), apiRoot + LocalPluginConfigurationResource.PATH);
            }
            case DEVICE_CONFIG: {
                return new TemplatedId(createDeviceConfigurationId(DeviceContext.create(pcc.getContext().getHubContext(), pcc.getContext().getPluginId(), pcc.getContext().getDeviceId())).getId(), apiRoot + DeviceConfigurationResource.TEMPLATE);
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public TemplatedId createPropertyContainerClassesId(PluginContext pctx) {
        return new TemplatedId(null, null);
    }

    @Override
    public TemplatedId createPropertyContainerClassId(PropertyContainerClassContext pccc, PropertyContainerClassType type) {
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
    public TemplatedId createTasksId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + TasksResource.PATH).format(createHubValues(ctx)),
            apiRoot + TasksResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createUserId(String userId) {
        return new TemplatedId(null, null);
    }

    @Override
    public TemplatedId createDevicesId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + DevicesResource.PATH).format(createHubValues(ctx)),
            apiRoot + DevicesResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createLocalPluginId(PluginContext ctx) {
        String s = apiRoot + LocalPluginResource.PATH;
        return new TemplatedId(
            new Template(s).format(createPluginValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createLocalPluginsId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + LocalPluginsResource.PATH).format(createHubValues(ctx)),
            apiRoot + LocalPluginsResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createRemotePluginsId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + RemotePluginsResource.PATH).format(createHubValues(ctx)),
            apiRoot + RemotePluginsResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createRemotePluginId(HubContext ctx, String pluginId, String version) {
        String s = apiRoot + RemotePluginResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.PLUGIN_ID, pluginId);
        values.put(JSONAttributes.PLUGIN_VERSION, version);
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createDeviceId(DeviceContext ctx) {
        String t = apiRoot + DeviceResource.PATH;
        return new TemplatedId(
            new Template(t).format(createDeviceValues(ctx)),
            t
        );
    }

    @Override
    public TemplatedId createDeviceConfigurationId(DeviceContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + DeviceConfigurationResource.PATH).format(createDeviceValues(ctx)),
            apiRoot + DeviceConfigurationResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createDeviceNameId(DeviceContext ctx) {
        String s = apiRoot + DeviceNameResource.PATH;
        return new TemplatedId(
            new Template(s).format(createDeviceValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createDeviceVariableDescriptionId(DeviceVariableContext vctx) {
        return null;
    }

    @Override
    public TemplatedId createDeviceVariableId(DeviceVariableContext vctx) {
        String ts = apiRoot + DeviceVariableResource.PATH;
        Template t = new Template(ts);
        Map<String,String> values = createDeviceValues(vctx.getDeviceContext());
        values.put(JSONAttributes.VARIABLE_NAME, vctx.getName());
        return new TemplatedId(
            t.format(values),
            ts
        );
    }

    @Override
    public TemplatedId createDeviceVariablesId(DeviceContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + DeviceVariablesResource.PATH).format(createDeviceValues(ctx)),
            apiRoot + DeviceVariablesResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createDeviceConfigurationClassId(DeviceContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + DeviceConfigurationClassResource.PATH).format(createDeviceValues(ctx)),
            apiRoot + DeviceConfigurationClassResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createDeviceTagsId(DeviceContext ctx) {
        return null;
    }

    @Override
    public TemplatedId createLocalPluginActionClassesId(PluginContext ctx) {
        String s = apiRoot + LocalPluginActionClassesResource.PATH;
        return new TemplatedId(
            new Template(s).format(createPluginValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createPluginDeviceConfigurationClassesId(PluginContext ctx) {
        String s = apiRoot + LocalPluginDeviceConfigurationClassesResource.PATH;
        return new TemplatedId(
            new Template(s).format(createPluginValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createPluginDeviceConfigurationClassId(PluginContext ctx, String name) {
        String s = apiRoot + LocalPluginDeviceConfigurationClassResource.PATH;
        Template t = new Template(s);
        Map<String,String> map = createPluginValues(ctx);
        map.put(JSONAttributes.NAME, name);
        return new TemplatedId(t.format(map), s);
    }

    @Override
    public TemplatedId createLocalPluginConfigurationId(PluginContext ctx) {
        String s = apiRoot + LocalPluginConfigurationResource.PATH;
        return new TemplatedId(
            new Template(s).format(createPluginValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createLocalPluginConfigurationClassId(PluginContext ctx) {
        String s = apiRoot + LocalPluginConfigurationClassResource.PATH;
        return new TemplatedId(new Template(s).format(createPluginValues(ctx)), s);
    }

    @Override
    public TemplatedId createRemotePluginInstallId(HubContext ctx, String pluginId, String version) {
        String s = apiRoot + RemotePluginInstallResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.PLUGIN_ID, pluginId);
        values.put(JSONAttributes.PLUGIN_VERSION, version);
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createTaskActionSetsId(HubContext ctx) {
        String s = apiRoot + ActionSetsResource.PATH;
        return new TemplatedId(
            new Template(s).format(createHubValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createHubLogId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + HubLogResource.PATH).format(createHubValues(ctx)),
            apiRoot + HubLogResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createHubPasswordId(HubContext ctx) {
        String s = apiRoot + HubPasswordResource.PATH;
        return new TemplatedId(
            new Template(s).format(createHubValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createHubSerialPortsId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + HubSerialPortsResource.PATH).format(createHubValues(ctx)),
            apiRoot + HubSerialPortsResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createHubSerialPortId(HubContext ctx, String name) {
        return new TemplatedId(name, null);
    }

    @Override
    public TemplatedId createLocalPluginIconId(PluginContext ctx) {
        String s = apiRoot + LocalPluginImageResource.PATH;
        return new TemplatedId(
            new Template(s).format(createPluginValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createGlobalVariablesId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + GlobalVariablesResource.PATH).format(createHubValues(ctx)),
            apiRoot + GlobalVariablesResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createGlobalVariableId(GlobalVariableContext gvctx) {
        String s = apiRoot + GlobalVariableResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = createPluginValues(gvctx.getPluginContext());
        values.put(JSONAttributes.NAME, gvctx.getName());
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createActionId(HubContext ctx, String actionId) {
        return null;
    }

    @Override
    public TemplatedId createActionSetId(HubContext ctx, String actionSetId) {
        return null;
    }

    @Override
    public TemplatedId createActionSetActionsId(HubContext ctx, String actionSetId) {
        return null;
    }

    @Override
    public TemplatedId createActionSetsId(HubContext ctx) {
        return null;
    }

    @Override
    public TemplatedId createActionPropertiesId(HubContext ctx, String actionId) {
        return null;
    }

    @Override
    public TemplatedId createActivityLogId(HubContext ctx) {
        String s = apiRoot + ActivityLogResource.PATH;
        return new TemplatedId(
            new Template(s).format(createHubValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createDataStreamsId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + DataStreamsResource.PATH).format(createHubValues(ctx)),
            apiRoot + DataStreamsResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createDataStreamId(HubContext ctx, String dataStreamId) {
        return new TemplatedId(
            createDataStreamPathId(ctx, DataStreamResource.PATH, dataStreamId),
            apiRoot + DataStreamResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createDataStreamDataId(HubContext ctx, String dataStreamId) {
        return new TemplatedId(
            createDataStreamPathId(ctx, DataStreamDataResource.PATH, dataStreamId),
            apiRoot + DataStreamDataResource.TEMPLATE
        );
    }

    private String createDataStreamPathId(HubContext ctx, String path, String dataStreamId) {
        Template t = new Template(apiRoot + path);
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.DATA_STREAM_ID, dataStreamId);
        return t.format(values);
    }

    @Override
    public TemplatedId createDataStreamFieldsId(HubContext ctx, String dataStreamId) {
        return null;
    }

    @Override
    public TemplatedId createDataStreamTagsId(HubContext ctx, String dataStreamId) {
        return null;
    }

    @Override
    public TemplatedId createDataStreamFieldId(HubContext ctx, String dataStreamId, String fieldId) {
        String s = apiRoot + DataStreamFieldResource.PATH;
        Map<String,String> values = createHubValues(ctx);
        values.put(JSONAttributes.DATA_STREAM_ID, dataStreamId);
        values.put(JSONAttributes.FIELD_ID, fieldId);
        return new TemplatedId(
            new Template(s).format(values),
            s
        );
    }

    @Override
    public TemplatedId createPluginDevicesId(PluginContext ctx) {
        String s = apiRoot + PluginDevicesResource.PATH;
        return new TemplatedId(
            new Template(s).format(createPluginValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createPresenceEntitiesId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + PresenceEntitiesResource.PATH).format(createHubValues(ctx)),
            apiRoot + PresenceEntitiesResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createPresenceEntityId(PresenceEntityContext ctx) {
        String s = apiRoot + PresenceEntityResource.PATH;
        Template t = new Template(s);
        Map<String,String> values = createHubValues(ctx.getHubContext());
        values.put(JSONAttributes.ENTITY_ID, ctx.getEntityId());
        return new TemplatedId(t.format(values), s);
    }

    @Override
    public TemplatedId createRepositoryId(HubContext ctx, String uri) {
        try {
            String s = apiRoot + HubRemoteRepositoryResource.PATH;
            Template t = new Template(s);
            Map<String,String> values = createHubValues(ctx);
            values.put(JSONAttributes.REPOSITORY_ID, URLEncoder.encode(uri, "UTF8"));
            return new TemplatedId(t.format(values), s);
        } catch (UnsupportedEncodingException e) {
            throw new HobsonRuntimeException("UTF8 is not supported on this platform", e);
        }
    }

    @Override
    public TemplatedId createSendTestEmailId(HubContext ctx) {
        String s = apiRoot + HubSendTestEmailResource.PATH;
        return new TemplatedId(
            new Template(s).format(createHubValues(ctx)),
            s
        );
    }

    @Override
    public TemplatedId createRepositoriesId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + HubRemoteRepositoriesResource.PATH).format(createHubValues(ctx)),
            apiRoot + HubRemoteRepositoriesResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createPresenceLocationsId(HubContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + PresenceLocationsResource.PATH).format(createHubValues(ctx)),
            apiRoot + PresenceLocationsResource.TEMPLATE
        );
    }

    @Override
    public TemplatedId createLocalPluginReloadId(PluginContext ctx) {
        String s = apiRoot + LocalPluginReloadResource.PATH;
        return new TemplatedId(
            new Template(s).format(createPluginValues(ctx)),
            s
        );
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
    public TemplatedId createDeviceActionClassId(DeviceContext ctx, String actionClassId) {
        String s = apiRoot + DeviceActionClassResource.PATH;
        Template t = new Template(s);
        Map<String,String> map = createDeviceValues(ctx);
        map.put(JSONAttributes.ACTION_CLASS_ID, actionClassId);
        return new TemplatedId(t.format(map), s);
    }

    @Override
    public TemplatedId createDeviceActionClassesId(DeviceContext ctx) {
        return new TemplatedId(
            new Template(apiRoot + DeviceActionClassesResource.PATH).format(createDeviceValues(ctx)),
            apiRoot + DeviceActionClassesResource.TEMPLATE
        );
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
