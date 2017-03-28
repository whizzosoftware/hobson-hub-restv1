/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.hub.PasswordChange;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.presence.*;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.dto.context.DTOBuildContext;
import com.whizzosoftware.hobson.dto.presence.PresenceLocationDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerSetDTO;
import com.whizzosoftware.hobson.dto.property.TypedPropertyDTO;
import com.whizzosoftware.hobson.json.TypedPropertyValueSerializer;
import com.whizzosoftware.hobson.rest.v1.resource.device.DeviceConfigurationClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.hub.HubConfigurationClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.LocalPluginActionClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.plugin.LocalPluginConfigurationClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.presence.PresenceLocationResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskConditionClassResource;
import org.json.JSONException;
import org.restlet.routing.Template;

import java.util.*;

/**
 * Helper class for mapping DTOs to model objects. This is done manually to avoid the overhead of a full-blown
 * marshalling library.
 *
 * @author Dan Noguerol
 */
public class DTOMapper {
    private static Template actionClassesTemplate;
    private static Template conditionClassesTemplate;
    private static Template hubConfigClassesTemplate;
    private static Template pluginConfigClassesTemplate;
    private static Template deviceConfigClassesTemplate;
    private static Template presenceLocationTemplate;

    static {
        actionClassesTemplate = new Template(RestResourceIdProvider.API_ROOT + LocalPluginActionClassResource.PATH);
        conditionClassesTemplate = new Template(RestResourceIdProvider.API_ROOT + TaskConditionClassResource.PATH);
        hubConfigClassesTemplate = new Template(RestResourceIdProvider.API_ROOT + HubConfigurationClassResource.PATH);
        pluginConfigClassesTemplate = new Template(RestResourceIdProvider.API_ROOT + LocalPluginConfigurationClassResource.PATH);
        deviceConfigClassesTemplate = new Template(RestResourceIdProvider.API_ROOT + DeviceConfigurationClassResource.PATH);
        presenceLocationTemplate = new Template(RestResourceIdProvider.API_ROOT + PresenceLocationResource.PATH);
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
            return PropertyContainerClassContext.create((String)vars.get("hubId"), (String)vars.get("pluginId"), (String)vars.get("deviceId"), (String)vars.get(containerName));
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

    static public PresenceLocationContext createPresenceLocationContext(String id) {
        Map<String,Object> vars = new HashMap<>();
        presenceLocationTemplate.parse(id, vars);
        return PresenceLocationContext.create(HubContext.create((String)vars.get("hubId")), (String)vars.get("locationId"));
    }

    static public PasswordChange mapPasswordChangeDTO(PasswordChangeDTO dto) {
        try {
            return new PasswordChange(dto.getCurrentPassword(), dto.getNewPassword());
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }

    static public PresenceLocation mapPresenceLocationDTO(PresenceLocationDTO dto) {
        PresenceLocation pl = null;
        if (dto.getId() != null) {
            pl = new PresenceLocation(createPresenceLocationContext(dto.getId()), dto.getName(), dto.getLatitude(), dto.getLongitude(), dto.getRadius(), dto.getBeaconMajor(), dto.getBeaconMinor());
        }
        return pl;
    }

    /**
     * Creates a PropertyContainer from a PropertyContainerDTO. This will also take care of creating appropriately typed
     * property values if a PropertyContainerClass can be obtained through the specified PropertyContainerClassProvider.
     *
     * @param dto the dto to map
     * @param ccProvider a provider for property container class lookup
     * @param idProvider a link provider
     *
     * @return a PropertyContainer object
     */
    static public PropertyContainer mapPropertyContainerDTO(PropertyContainerDTO dto, PropertyContainerClassProvider ccProvider, final IdProvider idProvider) {
        PropertyContainer pc = null;
        if (dto != null) {
            pc = new PropertyContainer();
            pc.setId(dto.getId());
            pc.setName(dto.getName());

            // at a minimum, we have the property container class id; however, if we can get the full property
            // container class definition from the container class provider, use that instead
            PropertyContainerClass pcc = mapPropertyContainerClassDTO(dto.getContainerClass());
            PropertyContainerClass npcc = ccProvider != null ? ccProvider.getPropertyContainerClass(pcc != null ? pcc.getContext() : null) : null;
            if (npcc != null) {
                pcc = npcc;
            }
            if (pcc != null) {
                pc.setContainerClassContext(pcc.getContext());
            }

            // copy property values
            if (dto.hasPropertyValues()) {
                for (String id : dto.getValues().keySet()) {
                    Object value = dto.getValues().get(id);
                    if (pcc != null) {
                        TypedProperty tp = pcc.getSupportedProperty(id);
                        if (tp != null) {
                            value = TypedPropertyValueSerializer.createValueObject(tp.getType(), value, idProvider != null ? new TypedPropertyValueSerializer.PropertyContextProvider() {
                                @Override
                                public DeviceContext createDeviceContext(String id) {
                                    return idProvider.createDeviceContext(id);
                                }

                                @Override
                                public PresenceEntityContext createPresenceEntityContext(String id) {
                                    return idProvider.createPresenceEntityContext(id);
                                }

                                @Override
                                public PresenceLocationContext createPresenceLocationContext(String id) {
                                    return idProvider.createPresenceLocationContext(id);
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

    static public PropertyContainerClass mapPropertyContainerClassDTO(PropertyContainerClassDTO dto) {
        PropertyContainerClass pcc = null;
        if (dto != null) {
            PropertyContainerClassType type = createPropertyContainerClassType(dto.getId());
            if (type != null) {
                pcc = new PropertyContainerClass(createPropertyContainerClassContext(type, dto.getId()), type);
//                pcc.setName(dto.getName());
                pcc.setSupportedProperties(mapTypedPropertyDTOList(dto.getSupportedProperties()));
            }
        }
        return pcc;
    }

    /**
     * Maps a list of PropertyContainer objects to a list of PropertyContainerDTO objects.
     *
     * @param ctx the build context
     * @param containers the PropertyContainers to map
     * @param type the PropertyContainerClassType
     * @param showDetails whether to include details for the items in the list
     * @param ccProvider a PropertyContainerClass provider
     *
     * @return a List of PropertyContainerDTO objects
     */
    static public List<PropertyContainerDTO> mapPropertyContainerList(DTOBuildContext ctx, List<PropertyContainer> containers, PropertyContainerClassType type, boolean showDetails, PropertyContainerClassProvider ccProvider) {
        List<PropertyContainerDTO> results = new ArrayList<>();
        if (containers != null) {
            for (PropertyContainer c : containers) {
                results.add(new PropertyContainerDTO.Builder(
                    ctx,
                    c,
                    ccProvider,
                    type,
                    showDetails
                ).build());
            }
        }
        return results;
    }

    /**
     * Maps a list of PropertyContainerDTO objects to a list of PropertyContainer objects.
     *
     * @param dtos the DTOs to map
     * @param ccProvider a PropertyContainerClass provider
     * @param idProvider a link provider
     *
     * @return a List of PropertyContainer objects
     */
    static public List<PropertyContainer> mapPropertyContainerDTOList(List<PropertyContainerDTO> dtos, PropertyContainerClassProvider ccProvider, IdProvider idProvider) {
        List<PropertyContainer> results = null;
        if (dtos != null) {
            results = new ArrayList<>();
            for (PropertyContainerDTO dto : dtos) {
                results.add(mapPropertyContainerDTO(dto, ccProvider, idProvider));
            }
        }
        return results;
    }

    static public PropertyContainerSet mapPropertyContainerSetDTO(PropertyContainerSetDTO dto, PropertyContainerClassProvider ccProvider, IdProvider idProvider) {
        try {
            if (dto != null) {
                PropertyContainerSet pcs = new PropertyContainerSet();
                pcs.setId(dto.getId());
                pcs.setName(dto.getName());
                pcs.setProperties(mapPropertyContainerDTOList(dto.getContainers(), ccProvider, idProvider));
                return pcs;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new HobsonRuntimeException("Unable to map", e);
        }
    }

    static public TypedProperty mapTypedPropertyDTO(TypedPropertyDTO dto) {
//        return TypedProperty.Builder(dto.getId(), dto.getName(), dto.getDescription(), TypedProperty.Type.valueOf(dto.getMediaType())).build();
        return null;
    }

    static public List<TypedPropertyDTO> mapTypedPropertyList(List<TypedProperty> props) {
        List<TypedPropertyDTO> results = null;
        if (props != null) {
            results = new ArrayList<>();
            for (TypedProperty tp : props) {
                results.add(new TypedPropertyDTO.Builder(tp).build());
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
