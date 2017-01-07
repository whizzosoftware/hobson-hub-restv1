/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.hub;

import com.whizzosoftware.hobson.api.HobsonAuthorizationException;
import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.user.HobsonRole;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.hub.RepositoryDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class HubRemoteRepositoryResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/repositories/{repositoryId}";

    @Inject
    PluginManager pluginManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;

    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));

        try {
            String repositoryId = URLDecoder.decode(getAttribute("repositoryId"), "UTF-8");
            String repositoryUrl = null;
            for (String url : pluginManager.getRemoteRepositories()) {
                if (repositoryId.equals(URLEncoder.encode(url, "UTF8"))) {
                    repositoryUrl = url;
                    break;
                }
            }

            if (repositoryUrl != null) {
                DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);
                RepositoryDTO dto = new RepositoryDTO(bctx, bctx.getIdProvider().createRepositoryId(ctx.getHubContext(), repositoryUrl), repositoryUrl);
                dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());
                JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
                jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
                return jr;
            } else {
                throw new HobsonNotFoundException("No repository found");
            }
        } catch (UnsupportedEncodingException e) {
            throw new HobsonRuntimeException("UTF8 encoding is not supported", e);
        }
    }

    @Override
    protected Representation delete() throws ResourceException {
        if (!isInRole(HobsonRole.administrator.name()) && !isInRole(HobsonRole.userWrite.name())) {
            throw new HobsonAuthorizationException("Forbidden");
        }
        if (pluginManager != null) {
            try {
                pluginManager.removeRemoteRepository(URLDecoder.decode(getAttribute("repositoryId"), "UTF-8"));
                getResponse().setStatus(Status.SUCCESS_ACCEPTED);
                return new EmptyRepresentation();
            } catch (UnsupportedEncodingException e) {
                throw new HobsonRuntimeException("URL decoding failed", e);
            }
        } else {
            throw new HobsonRuntimeException("No plugin manager found");
        }
    }
}
