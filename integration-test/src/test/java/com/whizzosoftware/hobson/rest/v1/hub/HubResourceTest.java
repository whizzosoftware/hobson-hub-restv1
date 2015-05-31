package com.whizzosoftware.hobson.rest.v1.hub;

import com.whizzosoftware.hobson.rest.v1.BaseTest;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubsResource;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class HubResourceTest extends BaseTest {
    @Test
    public void testGet() {
        // clear out any lingering configuration
        given().auth().basic(getUsername(), getPassword()).delete(createUri("/hubs/local")).then().statusCode(202);

        // verify hub default details
        given().auth().basic(getUsername(), getPassword()).get(createUri("/hubs/local")).then().assertThat().
            body("name", equalTo("Unnamed")).
            body("version", notNullValue()).
            body("setupComplete", equalTo(false)).
            body("logLevel", equalTo("INFO")).
            body("email", nullValue()).
            body("location", nullValue()).
            body("links.self", equalTo(createAbsolutePath("/hubs/local"))).
            body("links.actions", equalTo(createAbsolutePath("/hubs/local/actions"))).
            body("links.devices", equalTo(createAbsolutePath("/hubs/local/devices"))).
            body("links.globalVariables", equalTo(createAbsolutePath("/hubs/local/globalVariables"))).
            body("links.image", equalTo(createAbsolutePath("/hubs/local/image"))).
            body("links.imageLibrary", equalTo(createAbsolutePath("/hubs/local/imageLibrary"))).
            body("links.log", equalTo(createAbsolutePath("/hubs/local/log"))).
            body("links.password", equalTo(createAbsolutePath("/hubs/local/password"))).
            body("links.plugins", equalTo(createAbsolutePath("/hubs/local/plugins"))).
            body("links.presenceEntities", equalTo(createAbsolutePath("/hubs/local/presence/entities"))).
            body("links.tasks", equalTo(createAbsolutePath("/hubs/local/tasks"))).
            body("links.shutdown", equalTo(createAbsolutePath("/hubs/local/shutdown")));
    }

    @Test
    public void testGetWithInvalidHubId() {
        given().auth().basic(getUsername(), getPassword()).get(createUri(HubsResource.PATH + "/invalid1")).then().
                statusCode(404);
    }

    @Test
    public void testPut() {
        // clear out any lingering configuration
        given().auth().basic(getUsername(), getPassword()).delete(createUri("/hubs/local")).then().statusCode(202);

        // put a new configuration
        given().auth().basic(getUsername(), getPassword()).body("{\"name\":\"Test\",\"location\":{\"text\":\"555 Some St, New York, NY 10021\",\"latitude\":0.1234,\"longitude\":-0.1234},\"setupComplete\":true,\"logLevel\":\"DEBUG\",\"email\":{\"server\":\"smtp.gmail.com\",\"secure\":true,\"senderAddress\":\"foo@bar.com\",\"username\":\"user\",\"password\":\"password\"}}").put(createUri("/hubs/local")).then().statusCode(202);

        // verify the hub details have changed
        given().auth().basic(getUsername(), getPassword()).get(createUri("/hubs/local")).then().assertThat().
                body("name", equalTo("Test")).
                body("location.text", equalTo("555 Some St, New York, NY 10021")).
                body("location.latitude", equalTo(0.1234f)).
                body("location.longitude", equalTo(-0.1234f)).
                body("setupComplete", equalTo(true)).
                body("logLevel", equalTo("DEBUG")).
                body("email.server", equalTo("smtp.gmail.com")).
                body("email.secure", equalTo(true)).
                body("email.senderAddress", equalTo("foo@bar.com")).
                body("email.username", equalTo("user"));

        // clear out configuration
        given().auth().basic(getUsername(), getPassword()).delete(createUri("/hubs/local")).then().statusCode(202);
    }

    @Test
    public void testPutWithMalformedJSON() {
        given().auth().basic(getUsername(), getPassword()).body("{name\":\"Test\",\"location\":{\"text\":\"555 Some St, New York, NY 10021\",\"latitude\":0.1234,\"longitude\":-0.1234},\"setupComplete\":true,\"logLevel\":\"DEBUG\",\"email\":{\"server\":\"smtp.gmail.com\",\"secure\":true,\"senderAddress\":\"foo@bar.com\",\"username\":\"user\",\"password\":\"password\"}}").put(createUri("/hubs/local")).then().statusCode(400);
    }
}
