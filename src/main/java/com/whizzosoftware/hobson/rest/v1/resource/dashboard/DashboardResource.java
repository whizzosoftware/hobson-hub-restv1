/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.dashboard;

import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.presence.PresenceEntity;
import com.whizzosoftware.hobson.api.presence.PresenceManager;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.dto.presence.PresenceEntityDTO;
import com.whizzosoftware.hobson.rest.Authorizer;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.LinkProvider;
import org.json.JSONObject;
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

public class DashboardResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/dashboard";

    @Inject
    Authorizer authorizer;
    @Inject
    DeviceManager deviceManager;
    @Inject
    VariableManager variableManager;
    @Inject
    PresenceManager presenceManager;
    @Inject
    LinkProvider linkProvider;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/dashboard Get dashboard data
     * @apiVersion 0.1.3
     * @apiName GetDashboardData
     * @apiDescription Retrieves data needed for the user's dashboard.
     * @apiGroup Dashboard
     * @apiParam (Query Parameters) {String} expand A comma-separated list of attributes to expand (supported values are "item", "configurationClass", "configuration", "preferredVariable", "variables").
     * @apiSuccessExample {json} Success Response:
     * {
     *   "devices": {
     *     "numberOfItems": 2,
     *     "itemListElement": [
     *       {
     *         "item": {
     *           "@id": "/api/v1/users/local/hubs/local/com.whizzosoftware.hobson.hub.hobson-hub-foscam/devices/camera1",
     *         }
     *       },
     *       {
     *         "item": {
     *           "@id": "/api/v1/users/local/hubs/local/plugins/com.whizzosoftware.hobson.hub.hobson-hub-radiora/devices/device1",
     *         }
     *       }
     *     ]
     *   },
     *   "presenceEntities": {
     *     "numberOfItems": 2,
     *     "itemListElement": [
     *       {
     *         "item": {
     *           "@id": "/api/v1/users/local/hubs/local/presence/entities/8e0e0a27-85c8-4f1b-bfa5-59b4d309f259",
     *         }
     *       },
     *       {
     *         "item": {
     *           "@id": "/api/v1/users/local/hubs/local/presence/entities/baef1152-db85-4714-91b5-2b359fb79e50",
     *         }
     *       }
     *     ]
     *   }
     * }
     */
    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        authorizer.authorizeHub(ctx.getHubContext());

        JSONObject result = new JSONObject();
        TreeMap<String, Long> etagMap = new TreeMap<>();

        // build device list
        ItemListDTO deviceItemList = new ItemListDTO(linkProvider.createDevicesLink(ctx.getHubContext()));
        Collection<HobsonDevice> devices = deviceManager.getAllDevices(ctx.getHubContext());
        if (devices != null) {
            for (HobsonDevice device : devices) {
                HobsonDeviceDTO dto = DTOMapper.mapDevice(ctx, device, deviceManager, variableManager, expansions, linkProvider);
                deviceItemList.add(dto);
                etagMap.put(device.getContext().toString(), device.isAvailable() && dto.getLastVariableUpdate() != null ? dto.getLastVariableUpdate() : -1);
            }
            result.put("devices", deviceItemList.toJSON());
        }

        // build presence entity list
        ItemListDTO entityItemList = new ItemListDTO(linkProvider.createPresenceEntitiesLink(ctx.getHubContext()));
        Collection<PresenceEntity> entities = presenceManager.getAllEntities(ctx.getHubContext());
        if (entities != null) {
            for (PresenceEntity entity : entities) {
                PresenceEntityDTO dto = DTOMapper.mapPresenceEntity(entity, presenceManager, expansions, linkProvider);
                entityItemList.add(dto);
                etagMap.put(entity.getContext().toString(), entity.getLastUpdate() != null ? entity.getLastUpdate() : -1);
            }
            result.put("presenceEntities", entityItemList.toJSON());
        }

        // the ETag is a CRC calculated from all contexts and last update times
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
            r = new JsonRepresentation(result);
        } else {
            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
            r = new EmptyRepresentation();
        }

        r.setTag(etag);
        return r;
    }
}
