package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.action.ActionClass;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.dto.action.ActionClassDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;
import java.util.Collection;

public class LocalPluginActionClassesResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/actionClasses";

    @Inject
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));

        Collection<ActionClass> actionClasses = pluginManager.getLocalPlugin(pctx).getActionClasses();
        ItemListDTO results = new ItemListDTO(idProvider.createActionClassesId(ctx.getHubContext()));
        for (ActionClass ac : actionClasses) {
            results.add(new ActionClassDTO.Builder(idProvider.createActionClassId(ac.getContext()), DTOMapper.mapTypedPropertyList(ac.getSupportedProperties())).build());
        }
        return new JsonRepresentation(results.toJSON());
    }
}
