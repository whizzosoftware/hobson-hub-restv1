package com.whizzosoftware.hobson.rest.v1.resource.image;

import com.whizzosoftware.hobson.api.image.ImageInputStream;
import com.whizzosoftware.hobson.api.image.ImageManager;
import com.whizzosoftware.hobson.rest.v1.HobsonRestContext;
import org.restlet.data.MediaType;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class ImageLibraryImageResource extends SelfInjectingServerResource {
    public static final String PATH = "/users/{userId}/hubs/{hubId}/imageLibrary/images/{imageId}";

    @Inject
    ImageManager imageManager;

    /**
     * @api {get} /api/v1/users/:userId/hubs/:hubId/imageLibrary/images/:imageId Get library image
     * @apiVersion 0.4.4
     * @apiName GetImageLibraryImage
     * @apiDescription Retrieves binary data for an image library image.
     * @apiGroup Images
     * @apiSuccessExample {json} Success Response:
     *   HTTP/1.1 200 OK
     *   Content-Type: image/png
     *   ...
     */
    @Override
    public Representation get() throws ResourceException {
        HobsonRestContext ctx = HobsonRestContext.createContext(this, getRequest());
        ImageInputStream iis = imageManager.getImageLibraryImage(
            ctx.getUserId(), ctx.getHubId(), getAttribute("imageId"));
        return new InputRepresentation(iis.getInputStream(), MediaType.valueOf(iis.getMediaType()));
    }
}
