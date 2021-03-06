package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassType;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class RestResourceIdProviderTest {
    @Test
    public void testCreateLocalPluginId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/hubs/local/plugins/local/plugin1", provider.createLocalPluginId(PluginContext.createLocal("plugin1")).getId());
    }

    @Test
    public void testCreateTaskConditionClassId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/hubs/hub1/plugins/local/plugin1/conditionClasses/conditionclass1", provider.createTaskConditionClassId(PropertyContainerClassContext.create("hub1", "plugin1", null, "conditionclass1")).getId());
    }

    @Test
    public void testCreateTaskActionClassId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/hubs/hub1/plugins/local/plugin1/actionClasses/actionclass1", provider.createActionClassId(PropertyContainerClassContext.create("hub1", "plugin1", null, "actionclass1")).getId());
    }

    @Test
    public void testCreatePropertyContainerId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/hubs/local/configuration", provider.createPropertyContainerId(
            "configuration",
            new PropertyContainerClass(PropertyContainerClassContext.create(PluginContext.createLocal("plugin1"), "configurationClass"), PropertyContainerClassType.HUB_CONFIG)
        ).getId());
        assertEquals("/api/v1/hubs/local/plugins/local/plugin1/configuration", provider.createPropertyContainerId(
            "configuration",
            new PropertyContainerClass(PropertyContainerClassContext.create(PluginContext.createLocal("plugin1"), "configurationClass"), PropertyContainerClassType.PLUGIN_CONFIG)
        ).getId());
        assertEquals("myCondition", provider.createPropertyContainerId(
            "myCondition",
            new PropertyContainerClass(PropertyContainerClassContext.create(PluginContext.createLocal("plugin1"), "turnsOn"), PropertyContainerClassType.CONDITION)
        ).getId());
        assertEquals("myAction", provider.createPropertyContainerId(
            "myAction",
            new PropertyContainerClass(PropertyContainerClassContext.create(PluginContext.createLocal("plugin1"), "turnOn"), PropertyContainerClassType.CONDITION)
        ).getId());
    }

    @Test
    public void testCreateDeviceContext() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        DeviceContext dc = provider.createDeviceContext("/api/v1/hubs/hub1/plugins/local/plugin1/devices/device1");
        assertEquals("hub1", dc.getHubId());
        assertEquals("plugin1", dc.getPluginId());
        assertEquals("device1", dc.getDeviceId());
    }

    @Test
    public void testCreateDeviceVariableContext() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        DeviceVariableContext c = provider.createDeviceVariableContext("/api/v1/hubs/local/plugins/local/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/wstation/variables/outTempF");
        assertEquals("local", c.getHubId());
        assertEquals("com.whizzosoftware.hobson.hub.hobson-hub-sample", c.getPluginId());
        assertEquals("wstation", c.getDeviceId());
        assertEquals("outTempF", c.getName());
    }

    @Test
    public void testCreateDataStreamsId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/hubs/local/dataStreams", provider.createDataStreamsId(HubContext.createLocal()).getId());
    }
}
