/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.image.ImageInputStream;
import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A REST resource for setting/retrieving a hub image.
 *
 * @author Dan Noguerol
 */
public class HubImageResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/image";

    @Inject
    AccessManager accessManager;
    @Inject
    ImageManager imageManager;

    @Override
    protected Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        ImageInputStream iis = imageManager.getHubImage(ctx.getHubContext());
        return new InputRepresentation(iis.getInputStream(), MediaType.valueOf(iis.getMediaType()));
    }

    @Override
    protected Representation put(Representation entity) throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_CONFIGURE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        if (MediaType.APPLICATION_JSON.equals(entity.getMediaType(), true)) {
            JSONObject json = JSONHelper.createJSONFromRepresentation(entity);
            if (json.has("imageLibRef")) {
                String path = json.getString("imageLibRef");
                String imageId = path.substring(path.lastIndexOf('/') + 1, path.length());
                ImageInputStream iis = imageManager.getImageLibraryImage(ctx.getHubContext(), imageId);
                try {
                    imageManager.setHubImage(ctx.getHubContext(), new ImageInputStream(MediaType.IMAGE_PNG.toString(), iis.getInputStream()));
                    getResponse().setStatus(Status.SUCCESS_ACCEPTED);
                } finally {
                    iis.close();
                }
            } else if (json.has("image")) {
                JSONObject image = json.getJSONObject("image");
                ImageInputStream iis = new ImageInputStream(image.getString("mediaType"), new ByteArrayInputStream(Base64.decodeBase64(image.getString("data"))));
                try {
                    imageManager.setHubImage(ctx.getHubContext(), iis);
                    getResponse().setStatus(Status.SUCCESS_ACCEPTED);
                } finally {
                    iis.close();
                }
            }
        } else if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
            try {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1000240);
                RestletFileUpload upload = new RestletFileUpload(factory);
                FileItemIterator fileIt = upload.getItemIterator(entity);
                boolean found = false;
                while (fileIt.hasNext() && !found) {
                    FileItemStream fi = fileIt.next();
                    if (fi.getFieldName().equals("file")) {
                        found = true;
                        ImageInputStream iis = new ImageInputStream(fi.getContentType(), fi.openStream());
                        try {
                            imageManager.setHubImage(ctx.getHubContext(), iis);
                            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
                        } finally {
                            iis.close();
                        }
                    }
                }
            } catch (IOException | FileUploadException e) {
                e.printStackTrace();
            }
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return new EmptyRepresentation();
    }
}
