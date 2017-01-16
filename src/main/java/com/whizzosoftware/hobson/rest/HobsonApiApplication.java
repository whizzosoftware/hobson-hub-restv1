/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest;

/**
 * An interface for all Hobson API application classes.
 *
 * @author Dan Noguerol
 */
public interface HobsonApiApplication {
    /**
     * Returns the root path of this application (e.g. /api/v1).
     *
     * @return the root of the application
     */
    String getApiRoot();
}
