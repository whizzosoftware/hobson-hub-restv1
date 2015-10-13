/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HobsonHub;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.hub.PasswordChange;
import com.whizzosoftware.hobson.api.plugin.*;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.api.task.HobsonTask;
import com.whizzosoftware.hobson.api.task.TaskManager;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.dto.device.HobsonDeviceDTO;
import com.whizzosoftware.hobson.dto.hub.HobsonHubDTO;
import com.whizzosoftware.hobson.dto.hub.HubLogDTO;
import com.whizzosoftware.hobson.dto.image.ImageDTO;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerSetDTO;
import com.whizzosoftware.hobson.dto.property.TypedPropertyDTO;
import com.whizzosoftware.hobson.dto.task.HobsonTaskDTO;
import com.whizzosoftware.hobson.json.TypedPropertyValueSerializer;
import com.whizzosoftware.hobson.rest.ExpansionFields;
import com.whizzosoftware.hobson.rest.v1.resource.device.DeviceConfigurationClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubConfigurationClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.LocalPluginConfigurationClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskActionClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskConditionClassResource;
import org.json.JSONException;
import org.restlet.routing.Template;

import java.util.*;

/**
 * Helper class for mapping DTOs to/from model objects. This is done manually to avoid the overhead of using
 * auto-mapping libraries.
 *
 * @author Dan Noguerol
 */
public class DTOMapper {
    private static Template actionClassesTemplate;
    private static Template conditionClassesTemplate;
    private static Template hubConfigClassesTemplate;
    private static Template pluginConfigClassesTemplate;
    private static Template deviceConfigClassesTemplate;

    static {
        actionClassesTemplate = new Template(LinkProvider.API_ROOT + TaskActionClassResource.PATH);
        conditionClassesTemplate = new Template(LinkProvider.API_ROOT + TaskConditionClassResource.PATH);
        hubConfigClassesTemplate = new Template(LinkProvider.API_ROOT + HubConfigurationClassResource.PATH);
        pluginConfigClassesTemplate = new Template(LinkProvider.API_ROOT + LocalPluginConfigurationClassResource.PATH);
        deviceConfigClassesTemplate = new Template(LinkProvider.API_ROOT + DeviceConfigurationClassResource.PATH);
    }

    static public PropertyContainerClassContext createPropertyContainerClassContext(PropertyContainerClassType type, String id) {
        Map<String,Object> vars = new HashMap<>();

        String containerName = null;

        switch (type) {
            case CONDITION:
                conditionClassesTemplate.parse(id, vars);
                containerName = "conditionClassId";
                break;
            case ACTION:
                actionClassesTemplate.parse(id, vars);
                containerName = "actionClassId";
                break;
            case HUB_CONFIG:
                hubConfigClassesTemplate.parse(id, vars);
                containerName = "configurationClass";
                break;
            case PLUGIN_CONFIG:
                pluginConfigClassesTemplate.parse(id, vars);
                containerName = "configurationClass";
                break;
            case DEVICE_CONFIG:
                deviceConfigClassesTemplate.parse(id, vars);
                containerName = "configurationClass";
                break;
        }

        if (containerName != null) {
            return PropertyContainerClassContext.create((String)vars.get("userId"), (String)vars.get("hubId"), (String)vars.get("pluginId"), (String)vars.get("deviceId"), (String)vars.get(containerName));
        } else {
            return null;
        }
    }

    static public PropertyContainerClassType createPropertyContainerClassType(String id) {
        if (conditionClassesTemplate.match(id) > -1) {
            return PropertyContainerClassType.CONDITION;
        } else if (actionClassesTemplate.match(id) > -1) {
            return PropertyContainerClassType.ACTION;
        } else if (hubConfigClassesTemplate.match(id) > -1) {
            return PropertyContainerClassType.HUB_CONFIG;
        } else if (pluginConfigClassesTemplate.match(id) > -1) {
            return PropertyContainerClassType.PLUGIN_CONFIG;
        } else if (deviceConfigClassesTemplate.match(id) > -1) {
            return PropertyContainerClassType.DEVICE_CONFIG;
        } else {
            return null;
        }
    }

    static public HobsonHubDTO mapHub(HobsonHub hub, ExpansionFields expansions, LinkProvider linkProvider, HubManager hubManager, PluginManager pluginManager, TaskManager taskManager) {
        // create the response DTO
        HobsonHubDTO.Builder builder = new HobsonHubDTO.Builder(linkProvider.createHubLink(hub.getContext()))
                .name(hub.getName())
                .version(hub.getVersion());

        // add action classes
        ItemListDTO ildto = new ItemListDTO(linkProvider.createTaskActionClassesLink(hub.getContext()));
        if (expansions.has("actionClasses")) {
            for (PropertyContainerClass tac : taskManager.getAllActionClasses(hub.getContext(), false)) {
                ildto.add(new PropertyContainerClassDTO.Builder(linkProvider.createTaskActionClassLink(tac.getContext())).build());
            }
            ildto.updateNumberOfItems();
        }
        builder.actionClasses(ildto);

        // add configuration class attribute
        if (expansions.has("configurationClass")) {
            builder.configurationClass(
                    new PropertyContainerClassDTO.Builder(linkProvider.createHubConfigurationClassLink(hub.getContext()))
                            .name(hub.getConfigurationClass().getName())
                            .supportedProperties(DTOMapper.mapTypedPropertyList(hub.getConfigurationClass().getSupportedProperties()))
                            .build()
            );
        } else {
            builder.configurationClass(new PropertyContainerClassDTO.Builder(linkProvider.createHubConfigurationClassLink(hub.getContext())).build());
        }

        // add configuration attribute
        if (expansions.has("configuration")) {
            PropertyContainer hubConfig = hubManager.getConfiguration(hub.getContext());
            builder.configuration(
                    new PropertyContainerDTO.Builder(linkProvider.createHubConfigurationLink(hub.getContext()))
                            .name(hubConfig.getName())
                            .containerClass(
                                    new PropertyContainerClassDTO.Builder(linkProvider.createHubConfigurationClassLink(hub.getContext())).build()
                            )
                            .values(hubConfig.getPropertyValues())
                            .build()
            );
        } else {
            builder.configuration(new PropertyContainerDTO.Builder(linkProvider.createHubConfigurationLink(hub.getContext())).build());
        }

        // add condition classes
        ildto = new ItemListDTO(linkProvider.createTaskConditionClassesLink(hub.getContext()));
        if (expansions.has("conditionClasses")) {
            for (PropertyContainerClass tcc : taskManager.getAllConditionClasses(hub.getContext(), null, false)) {
                ildto.add(new PropertyContainerClassDTO.Builder(linkProvider.createTaskConditionClassLink(tcc.getContext())).build());
            }
            ildto.updateNumberOfItems();
        }
        builder.conditionClasses(ildto);

        // add devices attribute
        ildto = new ItemListDTO(linkProvider.createDevicesLink(hub.getContext()));
        builder.devices(ildto);

        // add log attribute
        builder.log(new HubLogDTO(linkProvider.createHubLogLink(hub.getContext())));

        // add local plugins attribute
        ildto = new ItemListDTO(linkProvider.createLocalPluginsLink(hub.getContext()));
        builder.localPlugins(ildto);
        if (expansions.has("localPlugins")) {
            for (PluginDescriptor pd : pluginManager.getLocalPluginDescriptors(hub.getContext())) {
                PluginContext pctx = PluginContext.create(hub.getContext(), pd.getId());
                HobsonPluginDTO dto = DTOMapper.mapPlugin(
                    new PluginDescriptorAdaptor(pd, null),
                    pd.getDescription(),
                    pd.isConfigurable() ? pluginManager.getLocalPluginConfiguration(pctx) : null,
                    null,
                    false,
                    expansions,
                    false,
                    linkProvider
                );
                ildto.add(dto);
            }
            ildto.updateNumberOfItems();
        }

        // add remote plugins attribute
        ildto = new ItemListDTO(linkProvider.createRemotePluginsLink(hub.getContext()));
        builder.remotePlugins(ildto);
        if (expansions.has("remotePlugins")) {
            for (PluginDescriptor pd : pluginManager.getRemotePluginDescriptors(hub.getContext())) {
                PluginContext pctx = PluginContext.create(hub.getContext(), pd.getId());
                HobsonPluginDTO dto = DTOMapper.mapPlugin(new PluginDescriptorAdaptor(pd, null), pd.getDescription(), null, null, false, expansions, true, linkProvider);
                dto.addLink("install", linkProvider.createRemotePluginInstallLink(pctx, pd.getVersionString()));
                ildto.add(dto);
            }
            ildto.updateNumberOfItems();
        }

        // add tasks
        ildto = new ItemListDTO(linkProvider.createTasksLink(hub.getContext()));
        if (expansions.has("tasks")) {
            for (HobsonTask task : taskManager.getAllTasks(hub.getContext())) {
                HobsonTaskDTO.Builder builder2 = new HobsonTaskDTO.Builder(linkProvider.createTaskLink(task.getContext()));
                builder2.name(task.getName())
                    .conditions(Collections.singletonList(new PropertyContainerDTO.Builder("").build())) // TODO
                    .actionSet(new PropertyContainerSetDTO.Builder("").build()) // TODO
                    .properties(task.getProperties());
                ildto.add(builder2.build());
            }
            ildto.updateNumberOfItems();
        }
        builder.tasks(ildto);

        return builder.build();
    }

    /**
     * Converts a model value object into one appropriate for use in a DTO.
     *
     * @param value the model value object
     * @param linkProvider a link provider
     *
     * @return a DTO value object
     */
    static public Object mapDTOValueObject(Object value, LinkProvider linkProvider) {
        if (value instanceof DeviceContext) {
            DeviceContext dctx = (DeviceContext) value;
            return new HobsonDeviceDTO.Builder(linkProvider.createDeviceLink(dctx)).build();
        } else if (value instanceof List) {
            List<Object> mappedList = new ArrayList<>();
            for (Object o : ((List)value)) {
                mappedList.add(mapDTOValueObject(o, linkProvider));
            }
            return mappedList;
        } else {
            return value;
        }
    }

    static public PasswordChange mapPasswordChangeDTO(PasswordChangeDTO dto) {
        try {
            return new PasswordChange(dto.getCurrentPassword(), dto.getNewPassword());
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }

    /**
     * Maps a HobsonPlugin object to a HobsonPluginDTO object.
     *
     * @param plugin the plugin to map
     * @param description the plugin description (or null)
     * @param config the plugin configuration (or null)
     * @param ccProvider a PropertyContainerClassProvider (or null)
     * @param includeDetails indicates whether to include plugin detail attributes
     * @param expansions any desired expansion fields (or null)
     * @param isRemote indicates whether this is a remote plugin (which will effect the ID that is generated)
     * @param linkProvider a link provider
     *
     * @return a HobsonPluginDTO instance
     */
    static public HobsonPluginDTO mapPlugin(HobsonPlugin plugin, String description, PropertyContainer config, PropertyContainerClassProvider ccProvider, boolean includeDetails, ExpansionFields expansions, boolean isRemote, LinkProvider linkProvider) {
        String id;

        if (isRemote) {
            id = linkProvider.createRemotePluginLink(plugin.getContext(), plugin.getVersion());
        } else {
            id = linkProvider.createLocalPluginLink(plugin.getContext());
        }

        HobsonPluginDTO.Builder b = new HobsonPluginDTO.Builder(id);

        // include details if necessary
        if (includeDetails) {
            ImageDTO imageDTO = null;

            if (!isRemote) {
                imageDTO = new ImageDTO.Builder(linkProvider.createLocalPluginIconLink(plugin.getContext())).build();
                b.addLink("reload", linkProvider.createLocalPluginReloadLink(plugin.getContext()));
            }

            b.name(plugin.getName()).
                    pluginId(plugin.getContext().getPluginId()). // pluginId is needed for the update check
                    description(description).
                    version(plugin.getVersion()).
                    type(plugin.getType()).
                    configurable(plugin.isConfigurable()).
                    status(plugin.getStatus()).
                    image(imageDTO).
                    configurationClass(mapPropertyContainerClass(plugin.getConfigurationClass(), expansions != null && expansions.has("configurationClass"), linkProvider)).
                    configuration(mapPropertyContainer(config, ccProvider, expansions != null && expansions.has("configuration"), linkProvider));
        }

        return b.build();
    }

    /**
     * Creates a PropertyContainerDTO from a PropertyContainer. This will also take care of creating appropriately
     * typed property values if a PropertyContainerClass can be obtained through the specified
     * PropertyContainerClassProvider.
     *
     * @param container the property container to map
     * @param ccProvider a PropertyContainerClassProvider
     * @param includeDetails indicates whether to include detail properties
     * @param links a link provider
     *
     * @return a PropertyContainerDTO instance
     */
    static public PropertyContainerDTO mapPropertyContainer(PropertyContainer container, PropertyContainerClassProvider ccProvider, boolean includeDetails, LinkProvider links) {
        PropertyContainerDTO dto = null;
        if (container != null) {
            PropertyContainerClass pcc = ccProvider.getPropertyContainerClass(container.getContainerClassContext());
            if (pcc != null) {
                // create DTO builder
                PropertyContainerDTO.Builder builder = new PropertyContainerDTO.Builder(links.createPropertyContainerLink(pcc));

                if (includeDetails) {
                    builder.name(container.getName()).containerClass(mapPropertyContainerClass(pcc, false, links));

                    // copy property values
                    if (container.getPropertyValues() != null) {
                        Map<String, Object> values = new HashMap<>();
                        for (String name : container.getPropertyValues().keySet()) {
                            values.put(name, mapDTOValueObject(container.getPropertyValue(name), links));
                        }
                        builder.values(values);
                    }
                }

                // create DTO
                dto = builder.build();
            }
        }
        return dto;
    }

    /**
     * Creates a PropertyContainer from a PropertyContainerDTO. This will also take care of creating appropriately typed
     * property values if a PropertyContainerClass can be obtained through the specified PropertyContainerClassProvider.
     *
     * @param dto the dto to map
     * @param ccProvider a provider for property container class lookup
     * @param links a link provider
     *
     * @return a PropertyContainer object
     */
    static public PropertyContainer mapPropertyContainerDTO(PropertyContainerDTO dto, PropertyContainerClassProvider ccProvider, final LinkProvider links) {
        PropertyContainer pc = null;
        if (dto != null) {
            pc = new PropertyContainer();
            pc.setId(dto.getId());
            pc.setName(dto.getName());

            // map the container class
            PropertyContainerClass pcc = mapPropertyContainerClassDTO(dto.getContainerClass());
            if (pcc != null) {
                // at a minimum, we have the property container class id; however, if we can get the full property
                // container class definition from the container class provider, use that instead
                PropertyContainerClass npcc;
                if (ccProvider != null) {
                    npcc = ccProvider.getPropertyContainerClass(pcc.getContext());
                    if (npcc != null) {
                        pcc = npcc;
                    }
                }
                pc.setContainerClassContext(pcc.getContext());
            }

            // copy property values
            if (dto.hasPropertyValues()) {
                for (String id : dto.getValues().keySet()) {
                    Object value = dto.getValues().get(id);
                    if (pcc != null) {
                        TypedProperty tp = pcc.getSupportedProperty(id);
                        if (tp != null) {
                            value = TypedPropertyValueSerializer.createValueObject(tp.getType(), value, links != null ? new TypedPropertyValueSerializer.DeviceContextProvider() {
                                @Override
                                public DeviceContext createDeviceContext(String id) {
                                    return links.createDeviceContext(id);
                                }
                            } : null);
                        }
                    }
                    pc.setPropertyValue(id, value);
                }
            }
        }
        return pc;
    }

    static public PropertyContainerClassDTO mapPropertyContainerClass(PropertyContainerClass pcc, boolean includeDetails, LinkProvider links) {
        if (pcc != null) {
            PropertyContainerClassDTO.Builder b = new PropertyContainerClassDTO.Builder(links.createPropertyContainerClassLink(pcc.getContext(), pcc.getType()));

            if (includeDetails) {
                b.name(pcc.getName()).
                        descriptionTemplate(pcc.getDescriptionTemplate()).
                        supportedProperties(mapTypedPropertyList(pcc.getSupportedProperties()));
            }

            return b.build();
        } else {
            return null;
        }
    }

    static public PropertyContainerClass mapPropertyContainerClassDTO(PropertyContainerClassDTO dto) {
        PropertyContainerClass pcc = null;
        if (dto != null) {
            PropertyContainerClassType type = createPropertyContainerClassType(dto.getId());
            if (type != null) {
                pcc = new PropertyContainerClass(createPropertyContainerClassContext(type, dto.getId()), type);
                pcc.setName(dto.getName());
                pcc.setSupportedProperties(mapTypedPropertyDTOList(dto.getSupportedProperties()));
            }
        }
        return pcc;
    }

    /**
     * Maps a list of PropertyContainer objects to a list of PropertyContainerDTO objects.
     *
     * @param containers the PropertyContainers to map
     * @param ccProvider a PropertyContainerClass provider
     * @param links a link provider
     *
     * @return a List of PropertyContainerDTO objects
     */
    static public List<PropertyContainerDTO> mapPropertyContainerList(List<PropertyContainer> containers, PropertyContainerClassProvider ccProvider, LinkProvider links) {
        List<PropertyContainerDTO> results = new ArrayList<>();
        if (containers != null) {
            for (PropertyContainer c : containers) {
                results.add(mapPropertyContainer(c, ccProvider, false, links));
            }
        }
        return results;
    }

    /**
     * Maps a list of PropertyContainerDTO objects to a list of PropertyContainer objects.
     *
     * @param dtos the DTOs to map
     * @param ccProvider a PropertyContainerClass provider
     * @param links a link provider
     *
     * @return a List of PropertyContainer objects
     */
    static public List<PropertyContainer> mapPropertyContainerDTOList(List<PropertyContainerDTO> dtos, PropertyContainerClassProvider ccProvider, LinkProvider links) {
        List<PropertyContainer> results = null;
        if (dtos != null) {
            results = new ArrayList<>();
            for (PropertyContainerDTO dto : dtos) {
                results.add(mapPropertyContainerDTO(dto, ccProvider, links));
            }
        }
        return results;
    }

    static public PropertyContainerSetDTO mapPropertyContainerSet(PropertyContainerSet pcs, PropertyContainerClassProvider ccProvider, LinkProvider links) {
        return new PropertyContainerSetDTO.Builder(pcs.getId())
            .containers(mapPropertyContainerList(pcs.getProperties(), ccProvider, links))
            .build();
    }

    static public PropertyContainerSet mapPropertyContainerSetDTO(PropertyContainerSetDTO dto, PropertyContainerClassProvider ccProvider, LinkProvider links) {
        try {
            if (dto != null) {
                PropertyContainerSet pcs = new PropertyContainerSet();
                pcs.setId(dto.getId());
                pcs.setName(dto.getName());
                pcs.setProperties(mapPropertyContainerDTOList(dto.getContainers(), ccProvider, links));
                return pcs;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new HobsonRuntimeException("Unable to map", e);
        }
    }

    static public TypedPropertyDTO mapTypedProperty(TypedProperty tp) {
        return new TypedPropertyDTO.Builder(tp.getId()).name(tp.getName()).description(tp.getDescription()).type(tp.getType()).constraints(tp.getConstraints()).build();
    }

    static public TypedProperty mapTypedPropertyDTO(TypedPropertyDTO dto) {
        return new TypedProperty.Builder(dto.getId(), dto.getName(), dto.getDescription(), TypedProperty.Type.valueOf(dto.getMediaType())).build();
    }

    static public List<TypedPropertyDTO> mapTypedPropertyList(List<TypedProperty> props) {
        List<TypedPropertyDTO> results = null;
        if (props != null) {
            results = new ArrayList<>();
            for (TypedProperty tp : props) {
                results.add(mapTypedProperty(tp));
            }
        }
        return results;
    }

    static public List<TypedProperty> mapTypedPropertyDTOList(List<TypedPropertyDTO> dtos) {
        List<TypedProperty> results = null;
        if (dtos != null) {
            results = new ArrayList<>();
            for (TypedPropertyDTO dto : dtos) {
                results.add(mapTypedPropertyDTO(dto));
            }
        }
        return results;
    }
}
