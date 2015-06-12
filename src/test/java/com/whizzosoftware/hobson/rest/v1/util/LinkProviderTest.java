package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class LinkProviderTest {
    @Test
    public void testCreateLocalPluginLink() {
        LinkProvider provider = new LinkProvider();
        assertEquals("/api/v1/users/local/hubs/local/plugins/local/plugin1", provider.createLocalPluginLink(PluginContext.createLocal("plugin1")));
    }

    @Test
    public void testCreateTaskConditionClassContext() {
        LinkProvider provider = new LinkProvider();
        PropertyContainerClassContext ctx = provider.createTaskConditionClassContext("/api/v1/users/user1/hubs/hub1/plugins/plugin1/conditionClasses/conditionclass1");
        assertEquals("user1", ctx.getUserId());
        assertEquals("hub1", ctx.getHubId());
        assertEquals("plugin1", ctx.getPluginId());
        assertEquals("conditionclass1", ctx.getContainerClassId());
    }

    @Test
    public void testCreateTaskConditionClassLink() {
        LinkProvider provider = new LinkProvider();
        assertEquals("/api/v1/users/user1/hubs/hub1/plugins/plugin1/conditionClasses/conditionclass1", provider.createTaskConditionClassLink(PropertyContainerClassContext.create("user1", "hub1", "plugin1", "conditionclass1")));
    }

    @Test
    public void testCreateTaskActionClassContext() {
        LinkProvider provider = new LinkProvider();
        PropertyContainerClassContext ctx = provider.createTaskActionClassContext("/api/v1/users/user1/hubs/hub1/plugins/plugin1/actionClasses/actionclass1");
        assertEquals("user1", ctx.getUserId());
        assertEquals("hub1", ctx.getHubId());
        assertEquals("plugin1", ctx.getPluginId());
        assertEquals("actionclass1", ctx.getContainerClassId());
    }

    @Test
    public void testCreateTaskActionClassLink() {
        LinkProvider provider = new LinkProvider();
        assertEquals("/api/v1/users/user1/hubs/hub1/plugins/plugin1/actionClasses/actionclass1", provider.createTaskActionClassLink(PropertyContainerClassContext.create("user1", "hub1", "plugin1", "actionclass1")));
    }

    @Test
    public void testCreatePropertyContainerLink() {
        LinkProvider provider = new LinkProvider();
        assertEquals("/api/v1/users/local/hubs/local/configuration", provider.createPropertyContainerLink(HubContext.createLocal(), LinkProvider.HUB_CONFIG_CONTAINER));
    }

    @Test
    public void testCreateDeviceContext() {
        LinkProvider provider = new LinkProvider();
        DeviceContext dc = provider.createDeviceContext("/api/v1/users/user1/hubs/hub1/plugins/plugin1/devices/device1");
        assertEquals("user1", dc.getUserId());
        assertEquals("hub1", dc.getHubId());
        assertEquals("plugin1", dc.getPluginId());
        assertEquals("device1", dc.getDeviceId());
    }
}
