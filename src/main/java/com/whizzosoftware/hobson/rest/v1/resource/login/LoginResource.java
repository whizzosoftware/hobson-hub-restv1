/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.login;

import com.whizzosoftware.hobson.api.HobsonAuthenticationException;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.user.HobsonUser;
import com.whizzosoftware.hobson.api.user.UserStore;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRole;
import com.whizzosoftware.hobson.rest.TokenHelper;
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
    HubManager hubManager;
    @Inject
    TokenHelper tokenHelper;
    @Inject
    IdProvider idProvider;

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
     *     "@id": "/api/v1/users/local"
     *   }
     * }
     */
    @Override
    protected Representation post(Representation entity) {
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        if (json.has("username") && json.has("password")) {
            HobsonUser user = userManager.authenticate(json.getString("username"), json.getString("password"));
            boolean showDetails = expansions.has(JSONAttributes.USER);

            AuthResultDTO dto = new AuthResultDTO(
                tokenHelper.createToken(user.getId(), HobsonRole.USER.value()),
                new HobsonUserDTO.Builder(
                    new DTOBuildContext.Builder().
                        expansionFields(expansions.pushContext(JSONAttributes.USER)).
                        idProvider(idProvider).
                        hubManager(hubManager).
                        build(),
                    user,
                    showDetails
                ).build()
            );
            expansions.popContext();

            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
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
        HubContext hctx = HubContext.createLocal();

        // set the hub name
        PropertyContainer config = hubManager.getConfiguration(hctx);
        String hubName;
        if (config != null) {
            hubName = config.hasPropertyValue("name") ? config.getPropertyValue("name").toString() : "Unnamed";
        } else {
            hubName = "Unnamed";
        }
        json.put("hubName", hubName);

        // set the hub version
        json.put("hubVersion", hubManager.getVersion(hctx));

        return new JsonRepresentation(json);
    }
}
