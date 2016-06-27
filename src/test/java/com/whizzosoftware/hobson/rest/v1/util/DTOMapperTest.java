/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubConfigurationClass;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.plugin.*;
import com.whizzosoftware.hobson.api.presence.PresenceLocation;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.dto.presence.PresenceLocationDTO;
import com.whizzosoftware.hobson.dto.property.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class DTOMapperTest {
    @Test
    public void testMapPropertyContainerSetDTO() {
        PropertyContainerSetDTO dto = new PropertyContainerSetDTO.Builder(new JSONObject(new JSONTokener("{\"trigger\":{\"cclass\":{\"@id\":\"/api/v1/users/user1/hubs/hub1/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule\"},\"values\":{\"date\":\"20150428\",\"time\":\"100000Z\"}}}")), new PropertyContainerMappingContext() {
            @Override
            public String getContainersName() {
                return "conditions";
            }
        }).build();
        PropertyContainerSet tcs = DTOMapper.mapPropertyContainerSetDTO(dto, null, null);
        assertNotNull(tcs);
    }

    @Test
    public void testMapPropertyContainerDTOWithNullContainerClass() {
        // test dto with no values
        PropertyContainerDTO dto = new PropertyContainerDTO.Builder("pcid").build();
        PropertyContainerClassProvider ccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return null;
            }
        };
        PropertyContainer pc = DTOMapper.mapPropertyContainerDTO(dto, ccp, new RestResourceIdProvider());
        assertEquals("pcid", pc.getId());
        assertNull(pc.getContainerClassContext());
        assertFalse(pc.hasPropertyValues());

        // test dto with values
        dto = new PropertyContainerDTO.Builder("pcid").values(Collections.singletonMap("foo", (Object)"bar")).build();
        pc = DTOMapper.mapPropertyContainerDTO(dto, ccp, new RestResourceIdProvider());
        assertEquals("pcid", pc.getId());
        assertNull(pc.getContainerClassContext());
        assertTrue(pc.hasPropertyValues());
        assertEquals("bar", pc.getStringPropertyValue("foo"));
    }

    @Test
    public void testMapPropertyContainerDTOWithConditionContainerClass() {
        // define the properties the condition class will support
        final List<TypedProperty> properties = new ArrayList<>();
        properties.add(new TypedProperty.Builder("name", "name", "Name", TypedProperty.Type.STRING).build());
        properties.add(new TypedProperty.Builder("device", "device", "Device", TypedProperty.Type.DEVICE).build());

        // create the property values
        Map<String,Object> values = new HashMap<>();
        values.put("name", "Hello");
        JSONObject deviceJson = new JSONObject();
        deviceJson.put("@id", "/api/v1/hubs/local/plugins/plugin2/devices/device1");
        values.put("device", deviceJson);

        // create the DTO to map
        PropertyContainerDTO dto = new PropertyContainerDTO.Builder("pcid").
            containerClass(
                new PropertyContainerClassDTO.Builder("/api/v1/hubs/local/plugins/plugin/conditionClasses/ccid").
                    build()
            ).
            values(values).
            build();

        // create the container class provider
        PropertyContainerClassProvider ccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return new PropertyContainerClass(
                    PropertyContainerClassContext.create(HubContext.createLocal(), "ccid"),
                    "name",
                    PropertyContainerClassType.CONDITION,
                    "",
                    properties
                );
            }
        };

        // perform mapping
        PropertyContainer pc = DTOMapper.mapPropertyContainerDTO(dto, ccp, new RestResourceIdProvider());

        assertEquals("pcid", pc.getId());
        assertNotNull(pc.getContainerClassContext());
        assertEquals("local", pc.getContainerClassContext().getHubId());
        assertEquals("ccid", pc.getContainerClassContext().getContainerClassId());
        assertTrue(pc.hasPropertyValues());
        assertEquals("Hello", pc.getStringPropertyValue("name"));
        assertTrue(pc.getPropertyValue("device") instanceof DeviceContext);
        assertEquals("local", ((DeviceContext)pc.getPropertyValue("device")).getHubId());
        assertEquals("plugin2", ((DeviceContext)pc.getPropertyValue("device")).getPluginId());
        assertEquals("device1", ((DeviceContext)pc.getPropertyValue("device")).getDeviceId());
    }

    @Test
    public void testMapPropertyContainerDTOWithActionContainerClass() {
        // define the properties the condition class will support
        final List<TypedProperty> properties = new ArrayList<>();
        properties.add(new TypedProperty.Builder("name", "name", "Name", TypedProperty.Type.STRING).build());
        properties.add(new TypedProperty.Builder("device", "device", "Device", TypedProperty.Type.DEVICE).build());

        // create the property values
        Map<String,Object> values = new HashMap<>();
        values.put("name", "Hello");
        JSONObject deviceJson = new JSONObject();
        deviceJson.put("@id", "/api/v1/hubs/local/plugins/plugin1/devices/device2");
        values.put("device", deviceJson);

        // create the DTO to map
        PropertyContainerDTO dto = new PropertyContainerDTO.Builder("pcid").
            containerClass(new PropertyContainerClassDTO.Builder("/api/v1/hubs/local/plugins/plugin/actionClasses/ccid").build()).
            values(values).
            build();

        // create the container class provider
        PropertyContainerClassProvider ccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return new PropertyContainerClass(
                    PropertyContainerClassContext.create(HubContext.createLocal(), "ccid"),
                    "name",
                    PropertyContainerClassType.ACTION,
                    "",
                    properties
                );
            }
        };

        // perform mapping
        PropertyContainer pc = DTOMapper.mapPropertyContainerDTO(dto, ccp, new RestResourceIdProvider());

        assertEquals("pcid", pc.getId());
        assertNotNull(pc.getContainerClassContext());
        assertEquals("local", pc.getContainerClassContext().getHubId());
        assertEquals("ccid", pc.getContainerClassContext().getContainerClassId());
        assertTrue(pc.hasPropertyValues());
        assertEquals("Hello", pc.getStringPropertyValue("name"));
        assertTrue(pc.getPropertyValue("device") instanceof DeviceContext);
        assertEquals("local", ((DeviceContext)pc.getPropertyValue("device")).getHubId());
        assertEquals("plugin1", ((DeviceContext)pc.getPropertyValue("device")).getPluginId());
        assertEquals("device2", ((DeviceContext)pc.getPropertyValue("device")).getDeviceId());
    }

    @Test
    public void testMapPropertyContainerDTOWithHubConfigContainerClass() {
        // define the properties the condition class will support
        final List<TypedProperty> properties = new ArrayList<>();
        properties.add(new TypedProperty.Builder("name", "name", "Name", TypedProperty.Type.STRING).build());

        // create the property values
        Map<String,Object> values = new HashMap<>();
        values.put("name", "Home");

        // create the DTO to map
        PropertyContainerDTO dto = new PropertyContainerDTO.Builder("pcid").
            containerClass(new PropertyContainerClassDTO.Builder("/api/v1/users/local/hubs/local/configurationClass").build()).
            values(values).
            build();

        // create the container class provider
        PropertyContainerClassProvider ccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return new HubConfigurationClass();
            }
        };

        // perform mapping
        PropertyContainer pc = DTOMapper.mapPropertyContainerDTO(dto, ccp, new RestResourceIdProvider());

        assertEquals("pcid", pc.getId());
        assertNotNull(pc.getContainerClassContext());
        assertEquals("local", pc.getContainerClassContext().getHubId());
        assertEquals("configuration", pc.getContainerClassContext().getContainerClassId());
        assertTrue(pc.hasPropertyValues());
        assertEquals("Home", pc.getStringPropertyValue("name"));
    }

    @Test
    public void testMapPropertyContainerDTOWithPluginConfigContainerClass() {
        // define the properties the condition class will support
        final List<TypedProperty> properties = new ArrayList<>();
        properties.add(new TypedProperty.Builder("name", "name", "Name", TypedProperty.Type.STRING).build());
        properties.add(new TypedProperty.Builder("device", "device", "Device", TypedProperty.Type.DEVICE).build());

        // create the property values
        Map<String,Object> values = new HashMap<>();
        values.put("name", "Hello");
        JSONObject deviceJson = new JSONObject();
        deviceJson.put("@id", "/api/v1/hubs/local/plugins/plugin2/devices/device1");
        values.put("device", deviceJson);

        // create the DTO to map
        PropertyContainerDTO dto = new PropertyContainerDTO.Builder("pcid").
            containerClass(
                new PropertyContainerClassDTO.Builder("/api/v1/hubs/local/plugins/local/plugin1/configurationClass").
                    build()
            ).
            values(values).
            build();

        // create the container class provider
        PropertyContainerClassProvider ccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
            return new PropertyContainerClass(
                PropertyContainerClassContext.create(HubContext.createLocal(), "ccid"),
                "name",
                PropertyContainerClassType.PLUGIN_CONFIG,
                "",
                properties
            );
            }
        };

        // perform mapping
        PropertyContainer pc = DTOMapper.mapPropertyContainerDTO(dto, ccp, new RestResourceIdProvider());

        assertEquals("pcid", pc.getId());
        assertNotNull(pc.getContainerClassContext());
        assertEquals("local", pc.getContainerClassContext().getHubId());
        assertEquals("ccid", pc.getContainerClassContext().getContainerClassId());
        assertTrue(pc.hasPropertyValues());
        assertEquals("Hello", pc.getStringPropertyValue("name"));
        assertTrue(pc.getPropertyValue("device") instanceof DeviceContext);
        assertEquals("local", ((DeviceContext)pc.getPropertyValue("device")).getHubId());
        assertEquals("plugin2", ((DeviceContext)pc.getPropertyValue("device")).getPluginId());
        assertEquals("device1", ((DeviceContext)pc.getPropertyValue("device")).getDeviceId());
    }

    @Test
    public void testMapPropertyContainerDTOList() {
        // create DTOs
        Map<String,Object> values = new HashMap<>();
        values.put("date", "2015-09-09");
        values.put("time", "10:00:00");
        JSONArray jda = new JSONArray();
        JSONObject jd = new JSONObject();
        jd.put("@id", "/api/v1/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/bulb");
        jda.put(jd);
        values.put("devices", jda);
        List<PropertyContainerDTO> dtos = new ArrayList<>();
        dtos.add(
            new PropertyContainerDTO.Builder()
                .containerClass(new PropertyContainerClassDTO.Builder("/api/v1/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule")
                    .build()
                )
                .values(values)
                .build()
        );

        // create container class provider
        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                List<TypedProperty> props = new ArrayList<>();
                props.add(new TypedProperty.Builder("date", "date", "date", TypedProperty.Type.DATE).build());
                props.add(new TypedProperty.Builder("time", "time", "time", TypedProperty.Type.TIME).build());
                props.add(new TypedProperty.Builder("recurrence", "recurrence", "recurrence", TypedProperty.Type.RECURRENCE).build());
                props.add(new TypedProperty.Builder("devices", "devices", "devices", TypedProperty.Type.DEVICES).build());
                return new PropertyContainerClass(
                    PropertyContainerClassContext.create(PluginContext.createLocal("com.whizzosoftware.hobson.hub.hobson-hub-scheduler"), "schedule"),
                    "schedule",
                    PropertyContainerClassType.CONDITION,
                    "",
                    props
                );
            }
        };

        // perform mapping
        List<PropertyContainer> pcs = DTOMapper.mapPropertyContainerDTOList(dtos, pccp, new RestResourceIdProvider());

        // verify results
        assertEquals(1, pcs.size());
        PropertyContainer pc = pcs.get(0);
        PropertyContainerClassContext pcc = pc.getContainerClassContext();
        assertEquals("local", pcc.getHubId());
        assertEquals("com.whizzosoftware.hobson.hub.hobson-hub-scheduler", pcc.getPluginId());
        assertEquals("schedule", pcc.getContainerClassId());
        assertEquals("2015-09-09", pc.getPropertyValue("date"));
        assertEquals("10:00:00", pc.getPropertyValue("time"));
        assertTrue(pc.getPropertyValue("devices") instanceof List);
        List l = (List)pc.getPropertyValue("devices");
        assertEquals(1, l.size());
        DeviceContext dc = (DeviceContext)l.get(0);
        assertEquals("local", dc.getHubId());
        assertEquals("com.whizzosoftware.hobson.hub.hobson-hub-sample", dc.getPluginId());
        assertEquals("bulb", dc.getDeviceId());

        // perform reverse mapping
        List<PropertyContainerDTO> pdtos = DTOMapper.mapPropertyContainerList(pcs, PropertyContainerClassType.DEVICE_CONFIG, false, pccp, new RestResourceIdProvider());

        // verify results
        assertEquals(1, pdtos.size());
        PropertyContainerDTO pdto = pdtos.get(0);
        assertNull(pdto.getValues());
    }

    @Test
    public void testMapPresenceLocationDTO() {
        // test map location
        PresenceLocation loc = DTOMapper.mapPresenceLocationDTO(new PresenceLocationDTO.Builder("foo").name("name").latitude(1.0).longitude(2.0).radius(3.0).build());
        assertEquals("name", loc.getName());
        assertEquals(1.0, loc.getLatitude(), 0.0);
        assertEquals(2.0, loc.getLongitude(), 0.0);
        assertEquals(3.0, loc.getRadius(), 0.0);

        // test beacon location
        loc = DTOMapper.mapPresenceLocationDTO(new PresenceLocationDTO.Builder("foo").name("name").beaconMajor(1).beaconMinor(2).build());
        assertEquals("name", loc.getName());
        assertEquals((Integer)1, loc.getBeaconMajor());
        assertEquals((Integer)2, loc.getBeaconMinor());

        // test empty location
        assertNull(DTOMapper.mapPresenceLocationDTO(new PresenceLocationDTO.Builder((String)null).build()));
    }
}

