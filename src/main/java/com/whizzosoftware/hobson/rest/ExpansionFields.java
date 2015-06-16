/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Indicates the fields that should be expanded when marshaling a DTO.
 *
 * @author Dan Noguerol
 */
public class ExpansionFields {
    private String[] expansionFields;

    /**
     * Constructor.
     *
     * @param expansions a comma-separated list of expansion fields
     */
    public ExpansionFields(String expansions) {
        expansionFields = StringUtils.split(expansions, ',');
    }

    /**
     * Indicates whether an expansion field is present.
     *
     * @param fieldName the name of the field to check
     *
     * @return a boolean
     */
    public boolean has(String fieldName) {
        return ArrayUtils.contains(expansionFields, fieldName);
    }
}
