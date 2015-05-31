package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.property.PropertyContainerSet;
import com.whizzosoftware.hobson.dto.PropertyContainerSetDTO;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import static org.junit.Assert.*;

public class DTOMapperTest {
    @Test
    public void testMapPropertyContainerSetDTO() {
        PropertyContainerSetDTO dto = new PropertyContainerSetDTO(new JSONObject(new JSONTokener("{\"trigger\":{\"class\":{\"@id\":\"/api/v1/users/user1/hubs/hub1/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule\"},\"values\":{\"date\":\"20150428\",\"time\":\"100000Z\"}}}")), "trigger", "conditions");
        PropertyContainerSet tcs = DTOMapper.mapPropertyContainerSetDTO(dto, null);
        assertNotNull(tcs);
        assertTrue(tcs.hasPrimaryProperty());
        assertNotNull(tcs.getPrimaryProperty().getContainerClassContext());
        assertEquals("user1", tcs.getPrimaryProperty().getContainerClassContext().getUserId());
        assertEquals("hub1", tcs.getPrimaryProperty().getContainerClassContext().getHubId());
        System.out.println(tcs);
    }
}

