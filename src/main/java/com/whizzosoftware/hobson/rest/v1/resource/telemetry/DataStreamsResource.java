/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.telemetry;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.telemetry.DataStream;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.telemetry.DataStreamDTO;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class DataStreamsResource extends SelfInjectingServerResource {
    public static final String PATH = "/dataStreams";

    @Inject
    TelemetryManager telemetryManager;
    @Inject
    IdProvider idProvider;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    /**
     * @api {get} /api/v1/users/:userId/dataStreams/:dataStreamId Get data streams
     * @apiVersion 0.8.0
     * @apiName GetDataStreams
     * @apiDescription Retrieve all data streams.
     * @apiGroup DataStream
     * @apiSuccessExample {json} Success Response:
     * {
     *   "@id": "/api/v1/users/local/hubs/local/dataStreams",
     *   "numberOfItems": 2,
     *   "itemListElement": [
     *     {
     *       "item": {
     *         "@id": "31c68ded-2364-4fb0-9bee-9d96b388476a"
     *       }
     *     },
     *     {
     *       "item": {
     *         "@id": "c141a552-07d7-4928-8d75-54db66cb86b9"
     *       }
     *     }
     *   ]
     * }
     */
    @Override
    protected Representation get() {
        if (telemetryManager != null && !telemetryManager.isStub()) {
            HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
            ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
            ItemListDTO results = new ItemListDTO(idProvider.createDataStreamsId());
            DTOBuildContext bc = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);
            for (DataStream ds : telemetryManager.getDataStreams("local")) { // TODO
                results.add(new DataStreamDTO.Builder(bc, ds, expansions.has(JSONAttributes.ITEM)).build());
            }
            JsonRepresentation jr = new JsonRepresentation(results.toJSON());
            jr.setMediaType(new MediaType(results.getJSONMediaType()));
            return jr;
        } else {
            throw new HobsonInvalidRequestException("No telemetry manager is available");
        }
    }

    /**
     * @api {post} /api/v1/users/:userId/hubs/:hubId/dataStreams/:dataStreamId Create data stream
     * @apiVersion 0.8.0
     * @apiName CreateDataStream
     * @apiDescription Create a new data stream.
     * @apiGroup DataStream
     * @apiSuccess {String} name The name of the data set.
     * @apiSuccess {Object} variables The variables in the set.
     * @apiSuccessExample {json} Success Response:
     * {
     *   "name": "House Temperature",
     *   "variables": [
     *      {
     *          "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/wstation/variables/outTempF"
     *      },
     *      {
     *          "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-sample/devices/thermostat/variables/inTempF"
     *      }
     *   ]
     * }
     */
    @Override
    protected Representation post(Representation entity) {
        if (telemetryManager != null && !telemetryManager.isStub()) {
            HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
            DataStreamDTO dto = new DataStreamDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();

            List<VariableContext> vars = new ArrayList<>();
            for (HobsonVariableDTO v : dto.getVariables()) {
                vars.add(idProvider.createVariableContext(v.getId()));
            }

            telemetryManager.createDataStream("local", dto.getName(), vars); // TODO

            getResponse().setStatus(Status.SUCCESS_CREATED);
            return new EmptyRepresentation();
        } else {
            throw new HobsonInvalidRequestException("No telemetry manager is available");
        }
    }
}
