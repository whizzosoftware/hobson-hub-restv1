/*
 *******************************************************************************
 * Copyright (c) 2017 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathUtilTest {
    @Test
    public void testRemovePrefix() {
        assertNull(PathUtil.convertPath(null, null));
        assertEquals("path", PathUtil.convertPath(null, "path"));
        assertEquals("hubs:local", PathUtil.convertPath("/api/v1", "/api/v1/hubs/local"));
    }
}
