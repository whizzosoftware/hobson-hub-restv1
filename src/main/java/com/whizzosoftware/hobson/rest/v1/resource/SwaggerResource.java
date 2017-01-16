/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource;

import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

/**
 * Resource that serves up the Swagger API definition file.
 *
 * @author Dan Noguerol
 */
public class SwaggerResource extends SelfInjectingServerResource {
    public static final String PATH = "/swagger.json";

    @Override
    protected Representation get() throws ResourceException {
        return new InputRepresentation(getClass().getClassLoader().getResourceAsStream("swagger.json"));
    }
}
