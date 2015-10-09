/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.plugin.*;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;

/**
 * Class to adapt a PluginDescriptor to a HobsonPlugin.
 *
 * @author Dan Noguerol
 */
public class PluginDescriptorAdaptor implements HobsonPlugin {
    private PluginDescriptor descriptor;
    private HobsonPlugin plugin;

    public PluginDescriptorAdaptor(PluginDescriptor descriptor, HobsonPlugin plugin) {
        this.descriptor = descriptor;
        this.plugin = plugin;
    }

    @Override
    public PluginContext getContext() {
        if (plugin != null) {
            return plugin.getContext();
        } else {
            return PluginContext.createLocal(descriptor.getId());
        }
    }

    @Override
    public String getName() {
        if (plugin != null) {
            return plugin.getName();
        } else {
            return descriptor.getName();
        }
    }

    @Override
    public PropertyContainerClass getConfigurationClass() {
        return plugin != null ? plugin.getConfigurationClass() : null;
    }

    @Override
    public HobsonPluginRuntime getRuntime() {
        return plugin != null ? plugin.getRuntime() : null;
    }

    @Override
    public PluginStatus getStatus() {
        if (plugin != null) {
            return plugin.getStatus();
        } else {
            return descriptor.getStatus();
        }
    }

    @Override
    public PluginType getType() {
        if (plugin != null) {
            return plugin.getType();
        } else {
            return descriptor.getType();
        }
    }

    @Override
    public String getVersion() {
        if (plugin != null && plugin.getVersion() != null) {
            return plugin.getVersion();
        } else {
            return descriptor.getVersionString();
        }
    }

    @Override
    public boolean isConfigurable() {
        if (plugin != null) {
            return plugin.isConfigurable();
        } else {
            return descriptor.isConfigurable();
        }
    }
}
