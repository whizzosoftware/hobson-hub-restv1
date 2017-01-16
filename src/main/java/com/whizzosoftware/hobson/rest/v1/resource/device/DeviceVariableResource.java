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
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.event.EventManager;
import com.whizzosoftware.hobson.api.event.device.DeviceVariablesUpdateRequestEvent;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.api.variable.DeviceVariableDescriptor;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.variable.HobsonVariableDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MapUtil;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.routing.Template;

import javax.inject.Inject;

/**
 * A REST resource that manages a device variable.
 *
 * @author Dan Noguerol
 */
public class DeviceVariableResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/devices/{deviceId}/variables/{variableName}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    EventManager eventManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute(JSONAttributes.PLUGIN_ID), getAttribute(JSONAttributes.DEVICE_ID));
        DeviceVariableDescriptor var = deviceManager.getDevice(dctx).getVariable(getAttribute(JSONAttributes.VARIABLE_NAME));

        HobsonVariableDTO dto = new HobsonVariableDTO.Builder(
            bctx,
            bctx.getIdProvider().createDeviceVariableId(var.getContext()),
            var,
            deviceManager.getDeviceVariable(var.getContext()),
            true
        ).build();

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation put(Representation entity) {
        if (!isInRole(HobsonRole.administrator.name()) && !isInRole(HobsonRole.userWrite.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }

        final HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        DeviceContext dctx = DeviceContext.create(ctx.getHubContext(), getAttribute(JSONAttributes.PLUGIN_ID), getAttribute(JSONAttributes.DEVICE_ID));

        Object value = createDeviceVariableValue(JSONHelper.createJSONFromRepresentation(entity));
        final String pluginId = getAttribute("pluginId");
        final String deviceId = getAttribute("deviceId");
        final String variableName = getAttribute("variableName");

        Response response = getResponse();

        eventManager.postEvent(ctx.getHubContext(), new DeviceVariablesUpdateRequestEvent(System.currentTimeMillis(), dctx, variableName, value));
        response.setStatus(Status.SUCCESS_ACCEPTED);
        // TODO: is there a better way to do this? The Restlet request reference scheme is always HTTP for some reason...
        Reference requestRef = getRequest().getResourceRef();
        if (Boolean.getBoolean(System.getProperty("useSSL"))) {
            response.setLocationRef(new Reference("https", requestRef.getHostDomain(), requestRef.getHostPort(), ctx.getApiRoot() + new Template(DeviceVariableResource.PATH).format(MapUtil.createTripleEntryMap(ctx, "pluginId", pluginId, "deviceId", deviceId, "variableName", variableName)), null, null));
        } else {
            response.setLocationRef(requestRef);
        }

        return new EmptyRepresentation();
    }

    private Object createDeviceVariableValue(JSONObject json) {
        try {
            return json.get("value");
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }
}
