package com.whizzosoftware.hobson.rest.v1.resource.job;

import com.whizzosoftware.hobson.api.action.ActionManager;
import com.whizzosoftware.hobson.api.action.job.JobInfo;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.rest.HobsonAuthorizer;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.inject.Inject;

public class JobResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/jobs/{jobId}";

    @Inject
    ActionManager actionManager;
    @Inject
    IdProvider idProvider;


    @Override
    protected Representation get() throws ResourceException {
        HobsonRestContext ctx = (HobsonRestContext)getRequest().getAttributes().get(HobsonAuthorizer.HUB_CONTEXT);

        JobInfo info = actionManager.getJobInfo(ctx.getHubContext(), getAttribute("jobId"));
        JSONObject json = new JSONObject();
        json.put("status", info.getStatus().toString());
        JSONArray a = new JSONArray();
        for (String m : info.getStatusMessages()) {
            a.put(m);
        }
        json.put("messages", a);
        return new JsonRepresentation(json);
    }


}
