package com.whizzosoftware.hobson.rest.v1.hub;

import com.whizzosoftware.hobson.rest.v1.BaseTest;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class HubsResourceTest extends BaseTest {
    @Test
    public void testGet() {
        // clear out any lingering configuration
        given().auth().basic(getUsername(), getPassword()).delete(createUri("/hubs/local")).then().statusCode(202);

        given().auth().basic(getUsername(), getPassword()).get(createUri("/hubs")).then().assertThat().
            body("size()", equalTo(1)).
            body("get(0).name", equalTo("Unnamed")).
            body("get(0).links.self", equalTo(createAbsolutePath("/hubs/local")));
    }
}
