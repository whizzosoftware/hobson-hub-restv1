/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.dto.context;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.variable.*;
import com.whizzosoftware.hobson.rest.v1.resource.device.MediaProxyResource;
import org.restlet.routing.Template;

import java.util.*;

/**
 * An implementation of DTOBuildContext that uses Hobson manager objects for data and replaces any variable
 * media URL values with a local proxy URL.
 *
 * @author Dan Noguerol
 */
public class MediaProxyDTOBuildContext extends ManagerDTOBuildContext {
    private String apiRoot;

    private MediaProxyDTOBuildContext(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public Collection<DeviceVariable> getDeviceVariables(DeviceContext dctx) {
        List<DeviceVariable> results = new ArrayList<>();
        for (DeviceVariable v : deviceManager.getDeviceVariables(dctx)) {
            results.add(createStubVariableIfNecessary(dctx.getHubContext(), v));
        }
        return results;
    }

    @Override
    public DeviceVariable getDeviceVariable(DeviceVariableContext vctx) {
        return createStubVariableIfNecessary(vctx.getHubContext(), deviceManager.getDeviceVariable(vctx));
    }

    private DeviceVariable createStubVariableIfNecessary(HubContext hctx, DeviceVariable v) {
        if (v != null && v.getDescription().hasMediaType()) {
            return new DeviceVariable(v.getDescription(), getProxyValue(hctx, v), v.getLastUpdate());
        } else {
            return v;
        }
    }

    private Object getProxyValue(HubContext hctx, DeviceVariable v) {
        Object value = null;

        if (v != null) {
            value = v.getValue();
            DeviceVariableDescription vd = v.getDescription();
            if (vd != null && vd.hasMediaType()) {
                value = apiRoot + new Template(MediaProxyResource.PATH).format(
                    createParamMap(hctx, vd.getContext().getPluginId(), vd.getContext().getDeviceId(), vd.getName())
                );
            }
        }
        return value;
    }

    private Map<String,String> createParamMap(HubContext hctx, String pluginId, String deviceId, String mediaId) {
        Map<String,String> map = new HashMap<>();
        map.put("hubId", hctx.getHubId());
        map.put("pluginId", pluginId);
        map.put("deviceId", deviceId);
        map.put("mediaId", mediaId);
        return map;
    }

    public static final class Builder extends ManagerDTOBuildContext.Builder {
        public Builder(String apiRoot) {
            ctx = new MediaProxyDTOBuildContext(apiRoot);
        }
    }
}
