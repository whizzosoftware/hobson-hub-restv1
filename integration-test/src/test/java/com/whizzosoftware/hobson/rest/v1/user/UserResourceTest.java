package com.whizzosoftware.hobson.rest.v1.user;

import com.whizzosoftware.hobson.rest.v1.BaseTest;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubsResource;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UserResourceTest extends BaseTest {
    @Test
    public void testGet() {
        given().auth().basic(getUsername(), getPassword()).get(createUri("")).then().assertThat().
            body("lastName", equalTo("User")).
            body("firstName", equalTo("Local")).
            body("links.self", equalTo(createAbsolutePath(""))).
            body("links.hubs", equalTo(createAbsolutePath(HubsResource.PATH)));
    }
}
