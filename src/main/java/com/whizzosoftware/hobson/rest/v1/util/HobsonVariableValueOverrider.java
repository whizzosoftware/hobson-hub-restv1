/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.variable.HobsonVariable;

/**
 * An implementation of HobsonVariable that is used to override the value of an existing HobsonVariable.
 *
 * @author Dan Noguerol
 */
public class HobsonVariableValueOverrider implements HobsonVariable {
    private HobsonVariable variable;
    private Object value;

    public HobsonVariableValueOverrider(HobsonVariable variable, Object value) {
        this.variable = variable;
        this.value = value;
    }

    @Override
    public String getPluginId() {
        return variable.getPluginId();
    }

    @Override
    public String getDeviceId() {
        return variable.getDeviceId();
    }

    @Override
    public String getName() {
        return variable.getName();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Mask getMask() {
        return variable.getMask();
    }

    @Override
    public Long getLastUpdate() {
        return variable.getLastUpdate();
    }

    @Override
    public boolean isGlobal() {
        return variable.isGlobal();
    }
}
