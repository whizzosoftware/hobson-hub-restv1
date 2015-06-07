package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.property.PropertyContainerSet;
import com.whizzosoftware.hobson.dto.PropertyContainerMappingContext;
import com.whizzosoftware.hobson.dto.PropertyContainerSetDTO;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import static org.junit.Assert.*;

public class DTOHelperTest {
    @Test
    public void testMapPropertyContainerSetDTO() {
        PropertyContainerSetDTO dto = new PropertyContainerSetDTO(new JSONObject(new JSONTokener("{\"trigger\":{\"cclass\":{\"@id\":\"/api/v1/users/user1/hubs/hub1/plugins/com.whizzosoftware.hobson.hub.hobson-hub-scheduler/conditionClasses/schedule\"},\"values\":{\"date\":\"20150428\",\"time\":\"100000Z\"}}}")), new PropertyContainerMappingContext() {
            @Override
            public String getPrimaryContainerName() {
                return "trigger";
            }

            @Override
            public String getContainersName() {
                return "conditions";
            }
        });
        PropertyContainerSet tcs = DTOHelper.mapPropertyContainerSetDTO(dto, null, null);
        assertNotNull(tcs);
        assertTrue(tcs.hasPrimaryProperty());
        assertNotNull(tcs.getPrimaryProperty().getContainerClassContext());
        assertEquals("user1", tcs.getPrimaryProperty().getContainerClassContext().getUserId());
        assertEquals("hub1", tcs.getPrimaryProperty().getContainerClassContext().getHubId());
        System.out.println(tcs);
    }
}

