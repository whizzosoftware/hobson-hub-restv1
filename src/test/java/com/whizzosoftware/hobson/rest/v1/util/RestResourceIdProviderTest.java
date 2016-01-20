package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassType;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class RestResourceIdProviderTest {
    @Test
    public void testCreateLocalPluginId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/users/local/hubs/local/plugins/local/plugin1", provider.createLocalPluginId(PluginContext.createLocal("plugin1")));
    }

    @Test
    public void testCreateTaskConditionClassId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/users/user1/hubs/hub1/plugins/plugin1/conditionClasses/conditionclass1", provider.createTaskConditionClassId(PropertyContainerClassContext.create("user1", "hub1", "plugin1", null, "conditionclass1")));
    }

    @Test
    public void testCreateTaskActionClassId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/users/user1/hubs/hub1/plugins/plugin1/actionClasses/actionclass1", provider.createTaskActionClassId(PropertyContainerClassContext.create("user1", "hub1", "plugin1", null, "actionclass1")));
    }

    @Test
    public void testCreatePropertyContainerId() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        assertEquals("/api/v1/users/local/hubs/local/configuration", provider.createPropertyContainerId(
            new PropertyContainerClass(PropertyContainerClassContext.create(PluginContext.createLocal("plugin1"), "configuration"), PropertyContainerClassType.HUB_CONFIG)
        ));
    }

    @Test
    public void testCreateDeviceContext() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        DeviceContext dc = provider.createDeviceContext("/api/v1/users/user1/hubs/hub1/plugins/plugin1/devices/device1");
        assertEquals("user1", dc.getUserId());
        assertEquals("hub1", dc.getHubId());
        assertEquals("plugin1", dc.getPluginId());
        assertEquals("device1", dc.getDeviceId());
    }

    @Test
    public void testCreateVariableContext() {
        RestResourceIdProvider provider = new RestResourceIdProvider();
        VariableContext c = provider.createVariableContext("/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/wstation/variables/outTempF");
        assertEquals("local", c.getHubId());
        assertEquals("local", c.getUserId());
        assertEquals("com.whizzosoftware.hobson.hub.hobson-hub-sample", c.getPluginId());
        assertEquals("wstation", c.getDeviceId());
        assertEquals("outTempF", c.getName());
    }
}
