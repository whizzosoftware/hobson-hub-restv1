/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.event.EventManager;
import com.whizzosoftware.hobson.api.event.device.DeviceVariablesUpdateRequestEvent;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.api.variable.DeviceVariableDescriptor;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A REST resource that retrieves device variable information.
 *
 * @author Dan Noguerol
 */
public class DeviceVariablesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/variables";
    public static final String TEMPLATE = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/{entity}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    EventManager eventManager;
    @Inject
    DTOBuildContextFactory contextFactory;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        DTOBuildContext dbctx = contextFactory.createContext(ctx.getApiRoot(), expansions);

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        ItemListDTO dto = new ItemListDTO(dbctx, idProvider.createDeviceVariablesId(dctx));

        Collection<DeviceVariableDescriptor> variables = deviceManager.getDevice(dctx).getVariables();
        if (variables != null) {
            boolean showDetails = expansions.has(JSONAttributes.ITEM);
            expansions.pushContext(JSONAttributes.ITEM);
            for (DeviceVariableDescriptor v : variables) {
                dto.add(new HobsonVariableDTO.Builder(
                    dbctx,
                    dbctx.getIdProvider().createDeviceVariableId(v.getContext()),
                    v,
                    deviceManager.getDeviceVariable(v.getContext()),
                    showDetails
                ).build());
            }
            expansions.popContext();

            dto.addContext(JSONAttributes.AIDT, dbctx.getIdTemplateMap());

            JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
            jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
            return jr;
        } else {
            throw new HobsonNotFoundException("Unable to find variables for device");
        }
    }

    @Override
    protected Representation put(Representation entity) {
        if (!isInRole(HobsonRole.administrator.name()) && !isInRole(HobsonRole.userWrite.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        Response response = getResponse();
        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId"));
        eventManager.postEvent(ctx.getHubContext(), new DeviceVariablesUpdateRequestEvent(System.currentTimeMillis(), dctx, createDeviceVariableValues(JSONHelper.createJSONFromRepresentation(entity))));
        response.setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    private Map<String,Object> createDeviceVariableValues(JSONObject json) {
        try {
            Map<String,Object> map = new HashMap<>();
            JSONObject values = json.getJSONObject("values");
            for (Object o : values.keySet()) {
                String key = (String)o;
                map.put(key, values.get(key));
            }
            return map;
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }
}
