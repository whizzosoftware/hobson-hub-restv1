package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.image.ImageInputStream;
import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class ImageLibraryImageResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/imageLibrary/images/{imageId}";

    @Inject
    AccessManager accessManager;
    @Inject
    ImageManager imageManager;

    @Override
    public Representation get() throws ResourceException {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.HUB_READ, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        ImageInputStream iis = imageManager.getImageLibraryImage(
            ctx.getHubContext(), getAttribute("imageId"));

        return new InputRepresentation(iis.getInputStream(), MediaType.valueOf(iis.getMediaType()));
    }
}
