/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.resource.task;

import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.property.PropertyContainerClass;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassContext;
import com.whizzosoftware.hobson.api.property.PropertyContainerClassProvider;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.security.AccessManager;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.ExpansionFields;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.context.DTOBuildContextFactory;
import com.whizzosoftware.hobson.dto.task.HobsonTaskDTO;
import com.whizzosoftware.hobson.dto.ItemListDTO;
import com.whizzosoftware.hobson.json.JSONAttributes;
import com.whizzosoftware.hobson.rest.HobsonRestContext;
import com.whizzosoftware.hobson.rest.HobsonRestUser;
import com.whizzosoftware.hobson.api.security.AuthorizationAction;
import com.whizzosoftware.hobson.rest.util.PathUtil;
import com.whizzosoftware.hobson.rest.v1.util.DTOMapper;
import com.whizzosoftware.hobson.rest.v1.util.JSONHelper;
import com.whizzosoftware.hobson.rest.v1.util.MediaTypeHelper;
import org.restlet.data.Status;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import java.util.Collection;

/**
 * A REST resource for retrieving a list of all tasks.
 *
 * @author Dan Noguerol
 */
public class TasksResource extends SelfInjectingServerResource {
    public static final String PATH = "/hubs/{hubId}/tasks";
    public static final String TEMPLATE = "/hubs/{hubId}/{entity}";

    @Inject
    AccessManager accessManager;
    @Inject
    HubManager hubManager;
    @Inject
    TaskManager taskManager;
    @Inject
    DTOBuildContextFactory dtoBuildContextFactory;
    @Inject
    IdProvider idProvider;

    @Override
    protected Representation get() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());
        final ExpansionFields expansions = new ExpansionFields(getQueryValue("expand"));
        final DTOBuildContext bctx = dtoBuildContextFactory.createContext(ctx.getApiRoot(), expansions);

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.TASK_READ, PathUtil.convertPath(ctx.getApiRoot(), null));

        ItemListDTO dto = new ItemListDTO(bctx, idProvider.createTasksId(ctx.getHubContext()));
        boolean showDetails = expansions.has("item");

        Collection<HobsonTask> tasks = taskManager.getTasks(ctx.getHubContext());

        if (tasks != null) {
            expansions.pushContext(JSONAttributes.ITEM);
            for (HobsonTask task : tasks) {
                if (task != null) {
                    dto.add(new HobsonTaskDTO.Builder(
                        bctx,
                        task,
                        showDetails
                    ).build());
                }
            }
            expansions.popContext();
        }

        dto.addContext(JSONAttributes.AIDT, bctx.getIdTemplateMap());

        JsonRepresentation jr = new JsonRepresentation(dto.toJSON());
        jr.setMediaType(MediaTypeHelper.createMediaType(getRequest(), dto));
        return jr;
    }

    @Override
    protected Representation post(Representation entity) {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.TASK_CREATE, PathUtil.convertPath(ctx.getApiRoot(), getRequest().getResourceRef().getPath()));

        HobsonTaskDTO dto = new HobsonTaskDTO.Builder(JSONHelper.createJSONFromRepresentation(entity)).build();
        dto.validate();

        PropertyContainerClassProvider pccp = new PropertyContainerClassProvider() {
            @Override
            public PropertyContainerClass getPropertyContainerClass(PropertyContainerClassContext ctx) {
                return hubManager.getContainerClass(ctx);
            }
        };

        taskManager.createTask(
            ctx.getHubContext(),
            dto.getName(),
            dto.getDescription(),
            DTOMapper.mapPropertyContainerDTOList(dto.getConditions(), pccp, idProvider),
            DTOMapper.mapPropertyContainerSetDTO(dto.getActionSet(), pccp, idProvider)
        );

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }

    @Override
    protected Representation delete() {
        final HobsonRestContext ctx = HobsonRestContext.createContext(getApplication(), getRequest().getClientInfo(), getRequest().getResourceRef().getPath());

        accessManager.authorize(((HobsonRestUser)getClientInfo().getUser()).getUser(), AuthorizationAction.TASK_DELETE, null);

        for (HobsonTask task : taskManager.getTasks(ctx.getHubContext())) {
            taskManager.deleteTask(task.getContext());
        }

        getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        return new EmptyRepresentation();
    }
}
