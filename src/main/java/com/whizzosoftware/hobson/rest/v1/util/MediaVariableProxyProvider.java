/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableProxyValueProvider;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
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
    private HobsonRestContext context;

    public MediaVariableProxyProvider(HobsonRestContext context) {
        this.context = context;
    }

    public Object getProxyValue(HobsonVariable v) {
        Object value = null;

        if (v != null) {
            if (VariableConstants.IMAGE_STATUS_URL.equals(v.getName()) || VariableConstants.VIDEO_STATUS_URL.equals(v.getName())) {
                value = context.getApiRoot() + new Template(MediaProxyResource.PATH).format(createParamMap(v.getPluginId(), v.getDeviceId(), v.getName()));
            } else {
                value = v.getValue();
            }
        }

        return value;
    }

    private Map<String,String> createParamMap(String pluginId, String deviceId, String mediaId) {
        Map<String,String> map = new HashMap<>();
        map.put("userId", context.getHubContext().getUserId());
        map.put("hubId", context.getHubContext().getHubId());
        map.put("pluginId", pluginId);
        map.put("deviceId", deviceId);
        map.put("mediaId", mediaId);
        return map;
    }
}
