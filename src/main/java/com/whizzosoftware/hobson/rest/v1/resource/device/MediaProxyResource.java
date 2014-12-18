/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StreamRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A REST resource that proxies images and video.
 *
 * @author Dan Noguerol
 */
public class MediaProxyResource extends SelfInjectingServerResource {
    private static final Logger logger = LoggerFactory.getLogger(MediaProxyResource.class);

    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/media/{mediaId}";

    @Inject
    VariableManager variableManager;

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    @Override
    public Representation get() {
        try {
            HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
            HobsonVariable hvar = variableManager.getDeviceVariable(ctx.getUserId(), ctx.getHubId(), getAttribute("pluginId"), getAttribute("deviceId"), getAttribute("mediaId"));
            if (hvar != null && hvar.getValue() != null) {
                String imageUri = hvar.getValue().toString();
                HttpGet get = new HttpGet(imageUri);
                final CloseableHttpResponse response = httpClient.execute(get);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String contentType = null;
                    HttpEntity entity = response.getEntity();
                    Header header = entity.getContentType();
                    if (header != null) {
                        contentType = header.getValue();
                    }
                    if (contentType != null) {
                        final InputStream inputStream = entity.getContent();
                        return new StreamRepresentation(new MediaType(contentType)) {
                            @Override
                            public InputStream getStream() throws IOException {
                                return inputStream;
                            }

                            @Override
                            public void write(OutputStream output) throws IOException {
                                try {
                                    // stream the camera image to the response stream
                                    byte[] buf = new byte[8192];
                                    int read;
                                    while ((read = inputStream.read(buf)) > -1) {
                                        output.write(buf, 0, read);
                                    }
                                    output.close();
                                } catch (IOException ioe) {
                                    logger.debug("IOException occurred while streaming media", ioe);
                                } finally {
                                    response.close();
                                }
                            }
                        };
                    } else {
                        response.close();
                        throw new HobsonRuntimeException("Unable to determine proxy content type");
                    }
                } else if (statusCode == 401) {
                    response.close();
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                    return new EmptyRepresentation();
                } else {
                    response.close();
                    throw new HobsonRuntimeException("Received " + statusCode + " response while retrieving image from camera");
                }
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return new EmptyRepresentation();
            }
        } catch (Exception e) {
            logger.error("Error obtaining image", e);
            throw new HobsonRuntimeException(e.getLocalizedMessage(), e);
        }
    }
}
