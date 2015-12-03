/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableProxyType;
import com.whizzosoftware.hobson.api.variable.VariableProxyValueProvider;
import com.whizzosoftware.hobson.rest.v1.resource.device.MediaProxyResource;
import org.restlet.routing.Template;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that replaces device image and video URLs with a Hobson REST API proxy URL. This is done so that authentication
 * with those device media URLs is handled through the Hobson runtime.
 *
 * @author Dan Noguerol
 */
public class MediaVariableProxyProvider implements VariableProxyValueProvider {
    private String apiRoot;

    public MediaVariableProxyProvider(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    @Override
    public VariableProxyType getProxyType() {
        return VariableProxyType.MEDIA;
    }

    public Object getProxyValue(HubContext hubContext, HobsonVariable v) {
        Object value = null;

        if (v != null) {
            if (VariableConstants.IMAGE_STATUS_URL.equals(v.getName()) || VariableConstants.VIDEO_STATUS_URL.equals(v.getName())) {
                value = apiRoot + new Template(MediaProxyResource.PATH).format(
                    createParamMap(hubContext.getUserId(), hubContext.getHubId(), v.getPluginId(), v.getDeviceId(), v.getName())
                );
            } else {
                value = v.getValue();
            }
        }

        return value;
    }

    private Map<String,String> createParamMap(String userId, String hubId, String pluginId, String deviceId, String mediaId) {
        Map<String,String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("hubId", hubId);
        map.put("pluginId", pluginId);
        map.put("deviceId", deviceId);
        map.put("mediaId", mediaId);
        return map;
    }
}
