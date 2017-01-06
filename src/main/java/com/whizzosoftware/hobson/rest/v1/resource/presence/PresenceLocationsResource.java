/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.presence;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.presence.PresenceLocation;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.presence.PresenceLocationDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;

public class PresenceLocationsResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/presence/locations";
    public static final String TEMPLATE = "/hubs/{hubId}/presence/{presenceType}";

    @Inject
    PresenceManager presenceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        ItemListDTO dto = new ItemListDTO(bctx, idProvider.createPresenceLocationsId(ctx.getHubContext()), true);
        for (PresenceLocation location : presenceManager.getAllPresenceLocations(ctx.getHubContext())) {
            dto.add(new PresenceLocationDTO.Builder(bctx, location, idProvider, expansions.has(JSONAttributes.ITEM)).build());
        }

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation post(Representation entity) {
        if (!isInRole(HobsonRole.administrator.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        JSONObject json = JSONHelper.createJSONFromRepresentation(entity);

        Double latitude = null;
        Double longitude = null;
        Double radius = null;
        Integer beaconMajor = null;
        Integer beaconMinor = null;

        if (json.has(JSONAttributes.LATITUDE)) {
            latitude = json.getDouble(JSONAttributes.LATITUDE);
        }
        if (json.has(JSONAttributes.LONGITUDE)) {
            longitude = json.getDouble(JSONAttributes.LONGITUDE);
        }
        if (json.has(JSONAttributes.RADIUS)) {
            radius = json.getDouble(JSONAttributes.RADIUS);
        }
        if (json.has(JSONAttributes.BEACON_MAJOR)) {
            beaconMajor = json.getInt(JSONAttributes.BEACON_MAJOR);
        }
        if (json.has(JSONAttributes.BEACON_MAJOR)) {
            beaconMinor = json.getInt(JSONAttributes.BEACON_MINOR);
        }

        presenceManager.addPresenceLocation(ctx.getHubContext(), json.getString(JSONAttributes.NAME), latitude, longitude, radius, beaconMajor, beaconMinor);

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    @Override
    protected Representation delete() {
        if (!isInRole(HobsonRole.administrator.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        for (PresenceLocation pl : presenceManager.getAllPresenceLocations(ctx.getHubContext())) {
            presenceManager.deletePresenceLocation(pl.getContext());
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}
