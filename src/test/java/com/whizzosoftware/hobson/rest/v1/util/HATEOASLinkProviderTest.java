package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class HATEOASLinkProviderTest {
    @Test
    public void testCreatePluginLink() {
        HATEOASLinkProvider provider = new HATEOASLinkProvider();
        assertEquals("/api/v1/users/local/hubs/local/plugins/plugin1", provider.createPluginLink(PluginContext.createLocal("plugin1")));
    }

    @Test
    public void testCreatePluginContext() {
        HATEOASLinkProvider provider = new HATEOASLinkProvider();
        PluginContext ctx = provider.createPluginContext("/api/v1/users/local/hubs/hub1/plugins/plugin1");
        assertNotNull(ctx);
        assertEquals("local", ctx.getUserId());
        assertEquals("hub1", ctx.getHubId());
        assertEquals("plugin1", ctx.getPluginId());
    }

    @Test
    public void testCreateTaskConditionClassContext() {
        HATEOASLinkProvider provider = new HATEOASLinkProvider();
        PropertyContainerClassContext ctx = provider.createTaskConditionClassContext("/api/v1/users/user1/hubs/hub1/plugins/plugin1/conditionClasses/conditionclass1");
        assertEquals("user1", ctx.getUserId());
        assertEquals("hub1", ctx.getHubId());
        assertEquals("plugin1", ctx.getPluginId());
        assertEquals("conditionclass1", ctx.getContainerClassId());
    }

    @Test
    public void testCreateTaskConditionClassLink() {
        HATEOASLinkProvider provider = new HATEOASLinkProvider();
        assertEquals("/api/v1/users/user1/hubs/hub1/plugins/plugin1/conditionClasses/conditionclass1", provider.createTaskConditionClassLink(PropertyContainerClassContext.create("user1", "hub1", "plugin1", "conditionclass1")));
    }

    @Test
    public void testCreateTaskActionClassContext() {
        HATEOASLinkProvider provider = new HATEOASLinkProvider();
        PropertyContainerClassContext ctx = provider.createTaskActionClassContext("/api/v1/users/user1/hubs/hub1/plugins/plugin1/actionClasses/actionclass1");
        assertEquals("user1", ctx.getUserId());
        assertEquals("hub1", ctx.getHubId());
        assertEquals("plugin1", ctx.getPluginId());
        assertEquals("actionclass1", ctx.getContainerClassId());
    }

    @Test
    public void testCreateTaskActionClassLink() {
        HATEOASLinkProvider provider = new HATEOASLinkProvider();
        assertEquals("/api/v1/users/user1/hubs/hub1/plugins/plugin1/actionClasses/actionclass1", provider.createTaskActionClassLink(PropertyContainerClassContext.create("user1", "hub1", "plugin1", "actionclass1")));
    }
}
