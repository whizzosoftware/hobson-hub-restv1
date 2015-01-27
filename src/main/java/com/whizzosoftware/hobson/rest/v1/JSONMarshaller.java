/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.action.HobsonAction;
import com.whizzosoftware.hobson.api.action.meta.ActionMetaData;
import com.whizzosoftware.hobson.api.action.meta.ActionMetaDataEnumValue;
import com.whizzosoftware.hobson.api.config.Configuration;
import com.whizzosoftware.hobson.api.config.ConfigurationEnumValue;
import com.whizzosoftware.hobson.api.config.ConfigurationProperty;
import com.whizzosoftware.hobson.api.config.ConfigurationPropertyMetaData;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.hub.EmailConfiguration;
import com.whizzosoftware.hobson.api.hub.HubLocation;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.hub.PasswordChange;
import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.PluginType;
import com.whizzosoftware.hobson.api.presence.PresenceEntity;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.util.VersionUtil;
import com.whizzosoftware.hobson.api.variable.DeviceVariableNotFoundException;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.rest.v1.resource.LogResource;
import com.whizzosoftware.hobson.rest.v1.resource.ShutdownResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionResource;
import com.whizzosoftware.hobson.rest.v1.resource.action.ActionsResource;
import com.whizzosoftware.hobson.rest.v1.resource.config.HubConfigurationResource;
import com.whizzosoftware.hobson.rest.v1.resource.config.HubPasswordResource;
import com.whizzosoftware.hobson.rest.v1.resource.device.*;
import com.whizzosoftware.hobson.rest.v1.resource.image.HubImageResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.*;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntitiesResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceEntityResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TasksResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariableResource;
import com.whizzosoftware.hobson.rest.v1.resource.variable.GlobalVariablesResource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.representation.Representation;
import org.restlet.routing.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Convenience methods for marshalling objects to/from JSON. Since Hobson needs to run on low resource devices,
 * this is done manually to avoid the overhead of bringing in a library like Jackson.
 *
 * @author Dan Noguerol
 */
public class JSONMarshaller {
    private static final Logger logger = LoggerFactory.getLogger(JSONMarshaller.class);

    static public JSONObject createHubInfoJSON(HobsonRestContext ctx, String version, boolean isSetupWizardComplete) {
        JSONObject json = new JSONObject();
        json.put("version", version);
        json.put("setupComplete", isSetupWizardComplete);

        String apiRoot = ctx.getApiRoot();

        Map<String,Object> emptyMap = createEmptyMap(ctx);

        JSONObject links = new JSONObject();
        links.put(ActionsResource.REL, apiRoot + new Template(ActionsResource.PATH).format(emptyMap));
        links.put(DevicesResource.REL, apiRoot + new Template(DevicesResource.PATH).format(emptyMap));
        links.put(GlobalVariablesResource.REL, apiRoot + new Template(GlobalVariablesResource.PATH).format(emptyMap));
        links.put(HubConfigurationResource.REL, apiRoot + new Template(HubConfigurationResource.PATH).format(emptyMap));
        links.put(HubImageResource.REL, apiRoot + new Template(HubImageResource.PATH).format(emptyMap));
        links.put(HubPasswordResource.REL, apiRoot + new Template(HubPasswordResource.PATH).format(emptyMap));
        links.put(LogResource.REL, apiRoot + new Template(LogResource.PATH).format(emptyMap));
        links.put(PluginsResource.REL, apiRoot + new Template(PluginsResource.PATH).format(emptyMap));
        links.put(PresenceEntitiesResource.REL, apiRoot + new Template(PresenceEntitiesResource.PATH).format(emptyMap));
        links.put(ShutdownResource.REL, apiRoot + new Template(ShutdownResource.PATH).format(emptyMap));
        links.put(TasksResource.REL, apiRoot + new Template(TasksResource.PATH).format(emptyMap));
        json.put("links", links);

        return json;
    }

    static public JSONObject createPluginDescriptorJSON(HobsonRestContext ctx, PluginDescriptor pd, Boolean details) {
        if (pd != null && pd.getId() != null) {
            // attempt to URL encode the plugin ID
            String encodedId;
            try {
                encodedId = URLEncoder.encode(pd.getId(), "UTF8");
            } catch (UnsupportedEncodingException e) {
                encodedId = pd.getId();
            }

            JSONObject json = new JSONObject();
            json.put("id", pd.getId());
            json.put("name", pd.getName());

            JSONObject status = new JSONObject();
            status.put("status", pd.getStatus().getStatus().toString());
            if (details != null && details && pd.getStatus().getMessage() != null) {
                status.put("message", pd.getStatus().getMessage());
            }
            json.put("status", status);

            json.put("type", pd.getType().toString());

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

                boolean hasCurrentVersion = (currentVersionString != null);
                boolean hasNewerVersion = (VersionUtil.versionCompare(latestVersionString, currentVersionString) == 1);

                if (pd.getDescription() != null) {
                    json.put("description", pd.getDescription());
                }

                if (hasCurrentVersion) {
                    json.put("currentVersion", currentVersionString);
                }
                if (hasNewerVersion) {
                    json.put("latestVersion", latestVersionString);
                }

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
                    links.put("reload", ctx.getApiRoot() + new Template(PluginReloadResource.PATH).format(createSingleEntryMap(ctx, "pluginId", encodedId)));
                }
                if (pd.getStatus().getStatus() != PluginStatus.Status.NOT_INSTALLED && pd.isConfigurable()) {
                    links.put("configuration", ctx.getApiRoot() + new Template(PluginConfigurationResource.PATH).format(createSingleEntryMap(ctx, "pluginId", encodedId)));
                }
            }

            return json;
        } else {
            return null;
        }
    }

    static public JSONObject createPluginConfigPropertiesJSON(HobsonRestContext ctx, String pluginId, Configuration config) {
        JSONObject results = new JSONObject();
        JSONObject configProps = new JSONObject();
        results.put("properties", configProps);
        for (ConfigurationProperty pcp : config.getProperties()) {
            JSONObject j = createConfigurationPropertyJSON(pcp);
            configProps.put(pcp.getId(), j);
        }
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(PluginConfigurationResource.PATH).format(createSingleEntryMap(ctx, "pluginId", pluginId)));
        results.put("links", links);
        return results;
    }

    static public JSONObject createHubConfigurationJSON(HobsonRestContext ctx, HubManager hubManager) {
        JSONObject json = new JSONObject();

        EmailConfiguration email = hubManager.getHubEmailConfiguration(ctx.getUserId(), ctx.getHubId());
        if (email != null && email.getMailServer() != null) {
            JSONObject jmail = new JSONObject();
            jmail.put("server", email.getMailServer());
            jmail.put("secure", email.isSMTPS());
            jmail.put("senderAddress", email.getSenderAddress());
            jmail.put("username", email.getUsername());
            json.put("email", jmail);
        }

        HubLocation loc = hubManager.getHubLocation(ctx.getUserId(), ctx.getHubId());
        if (loc != null && loc.getText() != null) {
            JSONObject jloc = new JSONObject();
            jloc.put("text", loc.getText());
            if (loc.hasLatitude()) {
                jloc.put("latitude", loc.getLatitude());
            }
            if (loc.hasLongitude()) {
                jloc.put("longitude", loc.getLongitude());
            }
            json.put("location", jloc);
        }

        json.put("logLevel", hubManager.getLogLevel(ctx.getUserId(), ctx.getHubId()));
        json.put("name", hubManager.getHubName(ctx.getUserId(), ctx.getHubId()));
        json.put("setupComplete", hubManager.isSetupWizardComplete(ctx.getUserId(), ctx.getHubId()));

        return json;
    }

    static public JSONObject createDeviceJSON(HobsonRestContext ctx, VariableManager variableManager, HobsonDevice device, Boolean telemetryEnabled, boolean details, boolean variables) {
        JSONObject json = new JSONObject();
        json.put("id", device.getId());
        json.put("name", device.getName());
        json.put("pluginId", device.getPluginId());
        if (device.getType() != null) {
            json.put("type", device.getType().toString());
        }

        // set the preferred variable if specified
        if (details) {
            if (telemetryEnabled != null) {
                json.put("telemetryEnabled", telemetryEnabled);
            }
            if (device.getPreferredVariableName() != null) {
                String pvName = device.getPreferredVariableName();
                try {
                    HobsonVariable var = variableManager.getDeviceVariable(ctx.getUserId(), ctx.getHubId(), device.getPluginId(), device.getId(), pvName);
                    JSONObject vjson = createDeviceVariableJSON(ctx, device.getPluginId(), device.getId(), var, false);
                    vjson.put("name", pvName);
                    json.put("preferredVariable", vjson);
                } catch (DeviceVariableNotFoundException e) {
                    logger.error("Error obtaining preferred variable for " + device.getPluginId() + "." + device.getId(), e);
                }
            }
        }

        // set all variables if specified
        if (variables) {
            JSONObject vars = new JSONObject();
            for (HobsonVariable v : variableManager.getDeviceVariables(ctx.getUserId(), ctx.getHubId(), device.getPluginId(), device.getId())) {
                vars.put(v.getName(), JSONMarshaller.createDeviceVariableJSON(ctx, device.getPluginId(), device.getId(), v, false));
            }
            json.put("variables", vars);
        }

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

    static public JSONArray createDeviceListJSON(HobsonRestContext ctx, VariableManager variableManager, Collection<HobsonDevice> devices, boolean details) {
        JSONArray results = new JSONArray();
        for (HobsonDevice device : devices) {
            results.put(createDeviceJSON(ctx, variableManager, device, null, details, false));
        }
        return results;
    }

    static public PasswordChange createPasswordChange(JSONObject json) {
        return new PasswordChange(json.getString("currentPassword"), json.getString("newPassword"));
    }

    static public JSONObject createGlobalVariableJSON(HobsonRestContext ctx, HobsonVariable v) {
        JSONObject json = new JSONObject();
        json.put("name", v.getName());
        json.put("value", v.getValue());
        json.put("mask", v.getMask());
        json.put("lastUpdate", v.getLastUpdate());
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(GlobalVariableResource.PATH).format(createSingleEntryMap(ctx, "name", v.getName())));
        json.put("links", links);

        return json;
    }

    static public JSONObject createGlobalVariablesListJSON(HobsonRestContext ctx, Collection<HobsonVariable> vars) {
        JSONObject results = new JSONObject();
        for (HobsonVariable v : vars) {
            results.put(v.getName(), createGlobalVariableJSON(ctx, v));
        }
        return results;
    }

    static public JSONObject createDeviceVariableJSON(HobsonRestContext ctx, String pluginId, String deviceId, HobsonVariable v, boolean details) {
        Object value = v.getValue();

        if (VariableConstants.IMAGE_STATUS_URL.equals(v.getName()) || VariableConstants.VIDEO_STATUS_URL.equals(v.getName())) {
            value = ctx.getApiRoot() + new Template(MediaProxyResource.PATH).format(createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "mediaId", v.getName()));
        }

        JSONObject o = new JSONObject();
        o.put("value", value);

        if (details) {
            o.put("mask", v.getMask());
            o.put("lastUpdate", v.getLastUpdate());
        }

        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(DeviceVariableResource.PATH).format(createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "variableName", v.getName())));
        o.put("links", links);
        return o;
    }

    public static JSONObject createDeviceVariableListJSON(HobsonRestContext ctx, String pluginId, String deviceId, Collection<HobsonVariable> deviceVariables) {
        JSONObject json = new JSONObject();
        for (HobsonVariable v : deviceVariables) {
            json.put(v.getName(), createDeviceVariableJSON(ctx, pluginId, deviceId, v, false));
        }
        return json;
    }

    public static Object createDeviceVariableValue(JSONObject json) {
        return json.get("value");
    }

    static public Configuration createConfigurationFromConfigJSON(JSONObject json) {
        Configuration config = new Configuration();
        JSONObject jsonProps = json.getJSONObject("properties");
        for (Object o : jsonProps.keySet()) {
            String configKey = o.toString();
            JSONObject configJson = jsonProps.getJSONObject(configKey);
            config.addProperty(new ConfigurationProperty(new ConfigurationPropertyMetaData(configKey), configJson.get("value")));
        }
        return config;
    }

    public static JSONObject createJSONFromRepresentation(Representation r) {
        try {
            return new JSONObject(new JSONTokener(r.getStream()));
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error reading JSON", e);
        }
    }

    public static EmailConfiguration createEmailConfiguration(JSONObject json) {
        return new EmailConfiguration(json.getString("server"), json.getBoolean("secure"), json.getString("username"), json.getString("password"), json.getString("senderAddress"));
    }

    public static HubLocation createHubLocation(JSONObject json) {
        Double latitude = null, longitude = null;
        if (json.has("latitude")) {
            latitude = json.getDouble("latitude");
        }
        if (json.has("longitude")) {
            longitude = json.getDouble("longitude");
        }
        return new HubLocation(json.getString("text"), latitude, longitude);
    }

    public static JSONObject createCurrentVersionJSON(String currentVersion) {
        JSONObject json = new JSONObject();
        json.put("currentVersion", currentVersion);
        return json;
    }

    public static JSONArray createPluginDescriptorListJSON(HobsonRestContext ctx, List<PluginDescriptor> plugins, boolean details) {
        JSONArray results = new JSONArray();
        for (PluginDescriptor pd : plugins) {
            JSONObject json = JSONMarshaller.createPluginDescriptorJSON(ctx, pd, details);
            if (json != null) {
                results.put(json);
            }
        }
        return results;
    }

    public static JSONObject createActionJSON(HobsonRestContext ctx, HobsonAction action, boolean details) {
        JSONObject json = new JSONObject();

        // add summary data
        json.put("name", action.getName());
        json.put("pluginId", action.getPluginId());

        // add detail data
        if (details) {
            JSONObject metas = new JSONObject();
            JSONArray metaOrder = new JSONArray();
            for (ActionMetaData ham : action.getMetaData()) {
                JSONObject meta = new JSONObject();
                metaOrder.put(ham.getId());
                meta.put("name", ham.getName());
                meta.put("description", ham.getDescription());
                meta.put("type", ham.getType());
                if (ham.getType() == ActionMetaData.Type.ENUMERATION) {
                    JSONObject enumValues = new JSONObject();
                    for (ActionMetaDataEnumValue eval : ham.getEnumValues()) {
                        JSONObject enumValue = new JSONObject();
                        enumValue.put("name", eval.getName());
                        if (eval.getParam() != null) {
                            JSONObject param = new JSONObject();
                            param.put("name", eval.getParam().getName());
                            param.put("description", eval.getParam().getDescription());
                            param.put("type", eval.getParam().getType());
                            enumValue.put("param", param);
                        }
                        if (eval.getRequiredDeviceVariable() != null) {
                            enumValue.put("requiredDeviceVariable", eval.getRequiredDeviceVariable());
                        }
                        enumValues.put(eval.getId(), enumValue);
                    }
                    meta.put("enumValues", enumValues);
                }
                metas.put(ham.getId(), meta);
            }
            json.put("meta", metas);
            json.put("metaOrder", metaOrder);
        }

        JSONObject links = new JSONObject();
        json.put("links", links);

        // add summary links
        links.put("self", ctx.getApiRoot() + new Template(ActionResource.PATH).format(createDoubleEntryMap(ctx, "pluginId", action.getPluginId(), "actionId", action.getId())));

        return json;
    }

    public static JSONObject createActionListJSON(HobsonRestContext ctx, Collection<HobsonAction> actions) {
        JSONObject json = new JSONObject();
        for (HobsonAction action : actions) {
            json.put(action.getId(), createActionJSON(ctx, action, false));
        }
        return json;
    }

    public static JSONObject createDeviceConfigurationJSON(HobsonRestContext ctx, Configuration config) {
        JSONObject json = new JSONObject();
        for (ConfigurationProperty cp : config.getProperties()) {
            json.put(cp.getId(), createConfigurationPropertyJSON(cp));
        }
        return json;
    }

    public static Map<String,Object> createConfigurationPropertyMap(JSONObject json) {
        Map<String,Object> results = new HashMap<>();
        for (Object o : json.keySet()) {
            String name = o.toString();
            JSONObject vo = json.getJSONObject(name);
            results.put(name, vo.get("value"));
        }
        return results;
    }

    public static JSONObject createConfigurationPropertyJSON(ConfigurationProperty cp) {
        JSONObject json = new JSONObject();
        json.put("name", cp.getName());
        json.put("description", cp.getDescription());
        json.put("type", cp.getType());
        if (cp.hasEnumValues()) {
            JSONObject vals = new JSONObject();
            for (ConfigurationEnumValue cev : cp.getEnumValues()) {
                JSONObject jcev = new JSONObject();
                jcev.put("name", cev.getName());
                vals.put(cev.getId(), jcev);
            }
            json.put("enumValues", vals);
        }
        if (cp.hasValue()) {
            json.put("value", cp.getValue());
        }
        return json;
    }

    public static JSONObject createTaskJSON(HobsonRestContext ctx, HobsonTask task, boolean details, boolean properties) {
        JSONObject json = new JSONObject();
        json.put("name", task.getName());
        json.put("type", task.getType().toString());
        JSONObject links = new JSONObject();
        links.put("self", ctx.getApiRoot() + new Template(TaskResource.PATH).format(createDoubleEntryMap(ctx, "providerId", task.getProviderId(), "taskId", task.getId())));
        json.put("links", links);

        if (details) {
            json.put("provider", task.getProviderId());
            json.put("conditions", task.getConditions());
            json.put("actions", task.getActions());
        }

        if (properties) {
            Properties p = task.getProperties();
            if (p != null && p.size() > 0) {
                JSONObject props = new JSONObject();
                for (Object o : p.keySet()) {
                    String key = o.toString();
                    props.put(o.toString(), p.get(key));
                }
                json.put("properties", props);
            }
        }

        return json;
    }

    public static JSONArray createTaskListJSON(HobsonRestContext ctx, Collection<HobsonTask> tasks, boolean properties) {
        JSONArray results = new JSONArray();
        for (HobsonTask t : tasks) {
            results.put(createTaskJSON(ctx, t, false, properties));
        }
        return results;
    }

    public static JSONObject createPresenceEntityJSON(HobsonRestContext ctx, PresenceEntity entity, boolean details) {
        JSONObject json = new JSONObject();
        json.put("name", entity.getName());
        json.put("location", entity.getLocation());
        if (details) {
            json.put("lastUpdate", entity.getLastUpdate());
        }
        JSONObject links = new JSONObject();
        json.put("links", links);
        links.put("self", ctx.getApiRoot() + new Template(PresenceEntityResource.PATH).format(createSingleEntryMap(ctx, "entityId", entity.getId())));
        return json;
    }

    public static JSONArray createPresenceEntitiesListJSON(HobsonRestContext ctx, Collection<PresenceEntity> entities) {
        JSONArray results = new JSONArray();
        for (PresenceEntity e : entities) {
            results.put(createPresenceEntityJSON(ctx, e, false));
        }
        return results;
    }

    public static JSONArray createVariableEventIdJSON(Collection<String> deviceVariableEventIds) {
        JSONArray json = new JSONArray();
        for (String eventId : deviceVariableEventIds) {
            json.put(eventId);
        }
        return json;
    }

    public static JSONObject createErrorJSON(Throwable t) {
        return createErrorJSON(t.getLocalizedMessage());
    }

    public static JSONObject createErrorJSON(String message) {
        JSONObject json = new JSONObject();
        json.put("message", message);
        return json;
    }

    public static Map<String,Object> createEmptyMap(HobsonRestContext ctx) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        return map;
    }

    public static Map<String,Object> createSingleEntryMap(HobsonRestContext ctx, String key, Object value) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        map.put(key, value);
        return map;
    }

    public static Map<String,Object> createDoubleEntryMap(HobsonRestContext ctx, String key1, String value1, String key2, String value2) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    public static Map<String,Object> createTripleEntryMap(HobsonRestContext ctx, String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId", ctx.getUserId());
        map.put("hubId", ctx.getHubId());
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }
}
