package com.whizzosoftware.hobson.rest.v1;

import org.junit.Before;
import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BaseTest {
    public final String uriPrefix = "http://localhost:8182";
    public final String apiPrefix = "/api/v1/users/local";

    @Before
    public void setUp() {
    }

    protected String createAbsolutePath(String path) {
        return apiPrefix + path;
    }

    protected String createUri(String path) {
        return uriPrefix + apiPrefix + path;
    }

    protected String getUsername() {
        return "local";
    }

    protected String getPassword() {
        return "local";
    }
}
