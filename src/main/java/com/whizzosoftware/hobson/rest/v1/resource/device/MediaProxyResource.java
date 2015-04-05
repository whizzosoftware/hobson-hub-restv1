/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.resource.device;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableManager;
import com.whizzosoftware.hobson.rest.v1.Authorizer;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.URIInfo;
import com.whizzosoftware.hobson.rest.v1.util.URLVariableParser;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;

/**
 * A REST resource that proxies images and video.
 *
 * @author Dan Noguerol
 */
public class MediaProxyResource extends SelfInjectingServerResource {
    private static final Logger logger = LoggerFactory.getLogger(MediaProxyResource.class);

    public static final String PATH = "/users/{userId}/hubs/{hubId}/plugins/{pluginId}/devices/{deviceId}/media/{mediaId}";

    private static final int PROXY_BUF_SIZE = 8192;
    private static final int DEFAULT_REALM_PORT = 80;

    @Inject
    Authorizer authorizer;
    @Inject
    VariableManager variableManager;

    @Override
    public Representation head() {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        authorizer.authorizeHub(ctx.getHubContext());
        HobsonVariable hvar = variableManager.getDeviceVariable(DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId")), getAttribute("mediaId"));
        if (hvar != null && hvar.getValue() != null) {
            try {
                final HttpProps httpProps = createHttpGet(hvar.getValue().toString());

                try {
                    final CloseableHttpResponse response = httpProps.client.execute(httpProps.httpGet);
                    getResponse().setStatus(new Status(response.getStatusLine().getStatusCode()));
                    response.close();
                    return new EmptyRepresentation();
                } catch (IOException e) {
                    throw new HobsonRuntimeException(e.getLocalizedMessage(), e);
                } finally {
                    try {
                        httpProps.client.close();
                    } catch (IOException e) {
                        logger.warn("Error closing HttpClient", e);
                    }
                }
            } catch (ParseException|URISyntaxException e) {
                logger.error("Error obtaining media stream from device", e);
                throw new HobsonRuntimeException(e.getLocalizedMessage(), e);
            }
        }

        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return new EmptyRepresentation();
    }

    @Override
    public Representation get() {
        try {
            HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
            authorizer.authorizeHub(ctx.getHubContext());
            HobsonVariable hvar = variableManager.getDeviceVariable(DeviceContext.create(ctx.getHubContext(), getAttribute("pluginId"), getAttribute("deviceId")), getAttribute("mediaId"));
            if (hvar != null && hvar.getValue() != null) {
                final HttpProps httpProps = createHttpGet(hvar.getValue().toString());

                try {
                    final CloseableHttpResponse response = httpProps.client.execute(httpProps.httpGet);

                    // make sure we got a valid 2xx response
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        String contentType = null;
                        HttpEntity entity = response.getEntity();
                        Header header = entity.getContentType();
                        if (header != null) {
                            contentType = header.getValue();
                        }
                        // The Content-Type response header is required or we won't know the type of content being proxied
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
                                        byte[] buf = new byte[PROXY_BUF_SIZE];
                                        int read;
                                        while ((read = inputStream.read(buf)) > -1) {
                                            output.write(buf, 0, read);
                                        }
                                        output.close();
                                    } catch (IOException ioe) {
                                        logger.debug("IOException occurred while streaming media", ioe);
                                    } finally {
                                        response.close();
                                        httpProps.client.close();
                                    }
                                }
                            };
                        } else {
                            response.close();
                            httpProps.client.close();
                            throw new HobsonRuntimeException("Unable to determine proxy content type");
                        }
                    // explicitly handle the 401 code
                    } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                        getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                        response.close();
                        httpProps.client.close();
                        return new EmptyRepresentation();
                    // otherwise, its a general failure
                    } else {
                        response.close();
                        httpProps.client.close();
                        throw new HobsonRuntimeException("Received " + statusCode + " response while retrieving image from camera");
                    }
                } catch (IOException e) {
                    try {
                        httpProps.client.close();
                    } catch (IOException ioe) {
                        logger.warn("Error closing HttpClient", ioe);
                    }
                    throw new HobsonRuntimeException(e.getLocalizedMessage(), e);
                }
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return new EmptyRepresentation();
            }
        } catch (ParseException|URISyntaxException e) {
            logger.error("Error obtaining media stream from device", e);
            throw new HobsonRuntimeException(e.getLocalizedMessage(), e);
        }
    }

    protected HttpProps createHttpGet(String varValue) throws ParseException, URISyntaxException {
        URIInfo uriInfo = URLVariableParser.parse(varValue);
        HttpGet get = new HttpGet(uriInfo.getURI());

        // populate the GET request with headers if specified
        if (uriInfo.hasHeaders()) {
            Map<String,String> headers = uriInfo.getHeaders();
            for (String name : headers.keySet()) {
                uriInfo.addHeader(name, headers.get(name));
            }
        }

        CloseableHttpClient httpClient;

        // populate the GET request with auth information if specified
        if (uriInfo.hasAuthInfo()) {
            URI uri = uriInfo.getURI();
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(
                        uri.getHost(),
                        (uri.getPort() > 0) ? uri.getPort() : DEFAULT_REALM_PORT
                    ),
                    new UsernamePasswordCredentials(
                        uriInfo.getAuthInfo().getUsername(),
                        uriInfo.getAuthInfo().getPassword()
                    )
            );
            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        } else {
            httpClient = HttpClients.createDefault();
        }

        return new HttpProps(httpClient, get);
    }

    private class HttpProps {
        public CloseableHttpClient client;
        public HttpGet httpGet;

        public HttpProps(CloseableHttpClient client, HttpGet httpGet) {
            this.client = client;
            this.httpGet = httpGet;
        }
    }
}
