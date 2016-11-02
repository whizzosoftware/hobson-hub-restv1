/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.dto.context;

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
    public DeviceVariableState getDeviceVariableState(DeviceVariableContext vctx) {
        DeviceVariableState s = super.getDeviceVariableState(vctx);
        return createStubVariableIfNecessary(vctx.getHubContext(), deviceManager.getDevice(vctx.getDeviceContext()).getVariable(vctx.getName()), s);
    }

    private DeviceVariableState createStubVariableIfNecessary(HubContext hctx, DeviceVariableDescriptor v, DeviceVariableState s) {
        if (v != null && v.hasMediaType()) {
            return new DeviceVariableState(v.getContext(), getProxyValue(hctx, v, s), s.getLastUpdate());
        } else {
            return s;
        }
    }

    private Object getProxyValue(HubContext hctx, DeviceVariableDescriptor v, DeviceVariableState s) {
        Object value = null;

        if (v != null && s != null) {
            value = s.getValue();
            if (v.hasMediaType()) {
                value = apiRoot + new Template(MediaProxyResource.PATH).format(
                    createParamMap(hctx, v.getContext().getPluginId(), v.getContext().getDeviceId(), v.getContext().getName())
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
