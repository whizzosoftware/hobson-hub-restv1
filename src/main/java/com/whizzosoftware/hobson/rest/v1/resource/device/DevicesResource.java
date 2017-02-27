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

import com.whizzosoftware.hobson.api.device.HobsonDeviceDescriptor;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.CRC32;

/**
 * A REST resource that returns device information.
 *
 * @author Dan Noguerol
 */
public class DevicesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/devices";
    public static final String TEMPLATE = "/hubs/{hubId}/{entity}";

    @Inject
    DeviceManager deviceManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        String varFilter = getQueryValue("var");
        String typeFilter = getQueryValue("type");
        String tag = getQueryValue("tag");

        ItemListDTO dto = new ItemListDTO(bctx, bctx.getIdProvider().createDevicesId(ctx.getHubContext()));

        Collection<HobsonDeviceDescriptor> devices;

        if (tag != null) {
            devices = deviceManager.getDevices(ctx.getHubContext(), tag);
        } else {
            devices = deviceManager.getDevices(ctx.getHubContext());
        }

        TreeMap<String, Long> etagMap = new TreeMap<>();

        if (devices != null) {
            // TODO: refactor so the JSON isn't built if the ETag matches
            boolean itemExpand = expansions.has(JSONAttributes.ITEM);

            expansions.pushContext(JSONAttributes.ITEM);

            for (HobsonDeviceDescriptor device : devices) {
                if ((varFilter == null || device.hasVariable(varFilter)) && (typeFilter == null || device.getType().toString().equals(typeFilter))) {
                    dto.add(new HobsonDeviceDTO.Builder(
                        bctx,
                        device.getContext(),
                        itemExpand
                    ).build());
                }
            }

            dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

            expansions.popContext();
        }

        // the ETag is a CRC calculated from all devices' contexts and last variable updates
        CRC32 crc = new CRC32();
        for (String dctx : etagMap.keySet()) {
            String s = dctx + Long.toString(etagMap.get(dctx));
            crc.update(s.getBytes());
        }
        Tag etag = new Tag(Long.toString(crc.getValue()));

        // check if ETag matches request
        List<Tag> requestTags = getRequest().getConditions().getNoneMatch();
        Representation r;
        if (requestTags.size() == 0 || !requestTags.get(0).equals(etag)) {
            r = new JsonRepresentation(dto.toJSON());
        } else {
            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
            r = new EmptyRepresentation();
        }

        r.setTag(etag);
        r.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return r;
    }
}
