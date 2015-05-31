package com.whizzosoftware.hobson.rest.v1.resource.login;

import com.whizzosoftware.hobson.ExpansionFields;
import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.api.user.UserStore;
import com.whizzosoftware.hobson.dto.AuthResultDTO;
import com.whizzosoftware.hobson.rest.TokenHelper;
import com.whizzosoftware.hobson.rest.v1.util.HATEOASLinkProvider;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class LoginResource extends SelfInjectingServerResource {
    public static final String PATH = "/login";

    @Inject
    UserStore userManager;
    @Inject
    TokenHelper tokenHelper;
    @Inject
    HATEOASLinkProvider linkHelper;

    /**
     * @api {post} /api/v1/login Login user
     * @apiVersion 0.5.0
     * @apiName UserLogin
     * @apiDescription Authenticates a user, providing a bearer token for making further API calls. Note that for a local hub, the username is always "local".
     * @apiGroup User
     * @apiParam (Query Parameters) {String} expand A comma-separated list of fields to expand in the response. Valid field values are "user".
     * @apiParam (Request Body) {String} username The name of the user to authenticate
     * @apiParam (Request Body) {String} password The password of the user to authenticate
     * @apiParamExample {json} Example Request:
     * {
     *   "username": "local",
     *   "password": "password"
     * }
     * @apiSuccess (Success 200) {String} token a bearer token for making further API calls
     * @apiSuccess (Success 200) {application/vnd.hobson.v1.user} user the user that was authenticated
     * @apiSuccessExample {json} Success Response:
     * {
     *   "token": "eyJraWQiOiJSU0EiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJIb2Jzb24iLCJzdWIiOiJsb2NhbCIsImV4cCI6MTQzMjY4NjQ4MX0.mTravshTbaAuEtKp8aezgmNy5mUHqH3W85vutyR7zI-Q_AC_pQ1DT9wayIvYcpkXR8p54pBc5Ju7LAwfPuQzzFhKnmKTSYTfZlE0gQsELK4pfH3FWzxfb8qdYTAy_f4q7gPiAMUPCORCd4d30H0PIGEgEv5-PjcF5UBQpa6JT1NCkeBczLDCFrxq7XfJCBUosHpO9OQTLzBVaJh83wSia4CRP-cfBphQDSVy-hFQ9Qo6YUUVj4l_f0znlEXQSN7VWepDB81ixU5IomEdU3mH50g5uSFu3CHnenkz-BgVokXa2Ge8OUl7MaMDHkWwdnfahcgFmrcZHdiyXyLf8qulGA",
     *   "user": {
     *     "_links": {
     *       "self": {
     *         "href": "/api/v1/users/local"
     *       }
     *     }
     *   }
     * }
     */
    @Override
    protected Representation post(Representation entity) {
        ExpansionFields expansion = new ExpansionFields(getQueryValue("expand"));
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        if (json.has("username") && json.has("password")) {
            HobsonUser user = userManager.authenticate(json.getString("username"), json.getString("password"));

            AuthResultDTO dto = new AuthResultDTO(
                tokenHelper.createToken(user.getId()),
                user,
                expansion,
                linkHelper
            );

            JsonRepresentation jr = new JsonRepresentation(dto.toJSON(linkHelper));
            jr.setMediaType(new MediaType(dto.getMediaType() + "+json"));
            return jr;
        } else {
            throw new HobsonAuthenticationException("Username and password are required properties");
        }
    }

    @Override
    protected Representation options() {
        JSONObject json = new JSONObject();
        if (userManager.hasDefaultUser()) {
            json.put("defaultUser", userManager.getDefaultUser());
        }
        return new JsonRepresentation(json);
    }
}