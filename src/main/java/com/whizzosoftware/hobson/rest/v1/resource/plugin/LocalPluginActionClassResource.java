package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.google.inject.Inject;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.action.ActionClass;
import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.action.job.AsyncJobHandle;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginContext;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.dto.action.ActionClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

public class LocalPluginActionClassResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/actionClasses/{actionClassId}";

    @Inject
    ActionManager actionManager;
    @Inject
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        final PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        final String actionClassId = getAttribute("actionClassId");

        ActionClass ac = actionManager.getActionClass(PropertyContainerClassContext.create(pctx, actionClassId));
        return new JsonRepresentation(new ActionClassDTO.Builder(idProvider.createActionClassId(ac.getContext()), DTOMapper.mapTypedPropertyList(ac.getSupportedProperties())).build().toJSON());
    }

    @Override
    protected Representation post(Representation entity) {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
        final PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
        final String actionClassId = getAttribute("actionClassId");

        PropertyContainerDTO dto = new PropertyContainerDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return pluginManager.getLocalPlugin(pctx).getActionClass(actionClassId);
            }
        };
        AsyncJobHandle result = actionManager.executeAction(DTOMapper.mapPropertyContainerDTO(dto, pccp, idProvider));
        try {
            result.getStartFuture().sync();
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            getResponse().setLocationRef(idProvider.createJobId(ctx.getHubContext(), result.getJobId()));
            return new EmptyRepresentation();
        } catch (InterruptedException e) {
            throw new HobsonRuntimeException("Unable to execute action");
        }
    }
}
