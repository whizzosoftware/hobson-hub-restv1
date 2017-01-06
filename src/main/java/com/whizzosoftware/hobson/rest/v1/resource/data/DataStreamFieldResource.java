/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.data;

import org.restlet.ext.guice.SelfInjectingServerResource;

public class DataStreamFieldResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/dataStreams/{dataStreamId}/data/{fieldId}";
}
