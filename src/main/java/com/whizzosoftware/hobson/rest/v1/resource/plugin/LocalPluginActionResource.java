package com.whizzosoftware.hobson.rest.v1.resource.plugin;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.plugin.PluginManager;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class LocalPluginActionResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/plugins/local/{pluginId}/actions/{actionId}";

    @Inject
    PluginManager pluginManager;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() throws ResourceException {
//        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
//        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
//
//        ActionInfo info = pluginManager.getLocalPlugin(pctx).getAction(getAttribute("actionId")).getInfo();
//        JSONObject json = new JSONObject();
//        json.put("status", info.getStatus().toString());
//        JSONArray a = new JSONArray();
//        for (String m : info.getMessages()) {
//            a.put(m);
//        }
//        json.put("messages", a);
//        return new JsonRepresentation(json);
        return new EmptyRepresentation();
    }

    @Override
    protected Representation delete() throws ResourceException {
//        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);
//        PluginContext pctx = PluginContext.create(ctx.getHubContext(), getAttribute("pluginId"));
//
//        pluginManager.getLocalPlugin(pctx).deleteAction(getAttribute("actionId"));
//
//        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
//        return new EmptyRepresentation();
        return new EmptyRepresentation();
    }
}
