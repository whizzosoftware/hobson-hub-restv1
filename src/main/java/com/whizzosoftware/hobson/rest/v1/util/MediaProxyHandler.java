/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableDescriptor;
import com.whizzosoftware.hobson.api.variable.DeviceVariableState;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.representation.Representation;

/**
 * An interface for classes that can handle proxying Hobson variables such as image URLs.
 *
 * @author Dan Noguerol
 */
public interface MediaProxyHandler {
    Representation createRepresentation(HubContext hctx, DeviceVariableDescriptor v, DeviceVariableState s, Form query, Response response);
}
