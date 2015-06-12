package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.HobsonInvalidRequestException;
import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.hub.HubManager;
import com.whizzosoftware.hobson.api.hub.PasswordChange;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginDescriptor;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.PluginType;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.dto.image.ImageDTO;
import com.whizzosoftware.hobson.dto.plugin.HobsonPluginDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerClassDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerDTO;
import com.whizzosoftware.hobson.dto.property.PropertyContainerSetDTO;
import com.whizzosoftware.hobson.dto.property.TypedPropertyDTO;
import com.whizzosoftware.hobson.json.TypedPropertyValueSerializer;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskActionClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskConditionClassResource;
import org.json.JSONException;
import org.restlet.routing.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DTOHelper {
    private static Template actionClassesTemplate;
    private static Template conditionClassesTemplate;

    static {
        actionClassesTemplate = new Template("/api/v1" + TaskActionClassResource.PATH);
        conditionClassesTemplate = new Template("/api/v1" + TaskConditionClassResource.PATH);
    }

    static public PropertyContainerSetDTO mapPropertyContainerSet(PropertyContainerSet pcs) {
        return new PropertyContainerSetDTO.Builder(pcs.getId())
            .primaryContainer(mapPropertyContainer(pcs.getPrimaryProperty()))
            .containers(mapPropertyContainerList(pcs.getProperties()))
            .build();
    }

    static public PropertyContainerSet mapPropertyContainerSetDTO(PropertyContainerSetDTO dto, HubManager hubManager, LinkProvider links) {
        try {
            if (dto != null) {
                PropertyContainerSet pcs = new PropertyContainerSet();
                pcs.setId(dto.getId());
                pcs.setName(dto.getName());
                pcs.setPrimaryProperty(mapPropertyContainerDTO(dto.getPrimaryContainer(), hubManager, links));
                pcs.setProperties(mapPropertyContainerDTOList(dto.getContainers(), hubManager, links));
                return pcs;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new HobsonRuntimeException("Unable to map", e);
        }
    }

    static public List<PropertyContainer> mapPropertyContainerDTOList(List<PropertyContainerDTO> dtos, HubManager hubManager, LinkProvider links) {
        List<PropertyContainer> results = null;
        if (dtos != null) {
            results = new ArrayList<>();
            for (PropertyContainerDTO dto : dtos) {
                results.add(mapPropertyContainerDTO(dto, hubManager, links));
            }
        }
        return results;
    }

    static public List<PropertyContainerDTO> mapPropertyContainerList(List<PropertyContainer> containers) {
        List<PropertyContainerDTO> results = new ArrayList<>();
        if (containers != null) {
            for (PropertyContainer c : containers) {
                results.add(mapPropertyContainer(c));
            }
        }
        return results;
    }

    static public PropertyContainer mapPropertyContainerDTO(PropertyContainerDTO dto, HubManager hubManager, final LinkProvider links) {
        PropertyContainer pc = null;
        if (dto != null) {
            pc = new PropertyContainer();
            pc.setId(dto.getId());
            pc.setName(dto.getName());

            // map the container class
            PropertyContainerClass pcc = mapPropertyContainerClassDTO(dto.getContainerClass());
            if (pcc != null) {
                // if we can get the full container class from the task manager, use that instead
                PropertyContainerClass npcc;
                if (hubManager != null) {
                    npcc = hubManager.getContainerClass(pcc.getContext());
                    if (npcc != null) {
                        pcc = npcc;
                    }
                }
                pc.setContainerClassContext(pcc.getContext());
            }

            // copy property values
            for (String id : dto.getValues().keySet()) {
                Object value = dto.getValues().get(id);
                // if there is
                if (pcc != null) {
                    TypedProperty tp = pcc.getSupportedProperty(id);
                    if (tp != null) {
                        value = TypedPropertyValueSerializer.createValueObject(null, tp.getType(), value, links != null ? new TypedPropertyValueSerializer.DeviceContextProvider() {
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
        return pc;
    }

    static public PropertyContainerDTO mapPropertyContainer(PropertyContainer container) {
        PropertyContainerDTO dto = null;
        if (container != null) {
            dto = new PropertyContainerDTO.Builder().values(container.getPropertyValues()).build();
        }
        return dto;
    }

    static public PropertyContainerClass mapPropertyContainerClassDTO(PropertyContainerClassDTO dto) {
        PropertyContainerClass pcc = null;
        if (dto != null) {
            pcc = new PropertyContainerClass();
            pcc.setContext(createPropertyContainerClassContext(dto.getId()));
            pcc.setName(dto.getName());
            pcc.setSupportedProperties(mapTypedPropertyDTOList(dto.getSupportedProperties()));
        }
        return pcc;
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

    static public TypedProperty mapTypedPropertyDTO(TypedPropertyDTO dto) {
        return new TypedProperty(dto.getId(), dto.getName(), dto.getDescription(), TypedProperty.Type.valueOf(dto.getMediaType()));
    }

    public static PasswordChange mapPasswordChangeDTO(PasswordChangeDTO dto) {
        try {
            return new PasswordChange(dto.getCurrentPassword(), dto.getNewPassword());
        } catch (JSONException e) {
            throw new HobsonInvalidRequestException(e.getMessage());
        }
    }


    static public TypedPropertyDTO mapTypedProperty(TypedProperty tp) {
        return new TypedPropertyDTO(tp);
    }

    static public PropertyContainerClassContext createPropertyContainerClassContext(String id) {
        Map<String,Object> vars = new HashMap<>();

        String containerName = null;

        if (conditionClassesTemplate.match(id) > -1) {
            conditionClassesTemplate.parse(id, vars);
            containerName = "conditionClassId";
        } else if (actionClassesTemplate.match(id) > -1) {
            actionClassesTemplate.parse(id, vars);
            containerName = "actionClassId";
        }

        if (containerName != null) {
            return PropertyContainerClassContext.create((String)vars.get("userId"), (String)vars.get("hubId"), (String)vars.get("pluginId"), (String)vars.get(containerName));
        } else {
            return null;
        }
    }

    static public void populatePluginDTO(HobsonPlugin plugin, String configClassLink, PropertyContainerClass configClass, String configLink, PropertyContainer config, String imageLink, HobsonPluginDTO.Builder builder) {
        populatePluginDTO(
            plugin.getName(),
            null,
            plugin.getVersion(),
            plugin.getType(),
            plugin.isConfigurable(),
            plugin.getStatus(),
            configClassLink,
            configClass,
            configLink,
            config,
            imageLink,
            builder
        );
    }

    static public void populatePluginDTO(PluginDescriptor pd, String configClassLink, PropertyContainerClass configClass, String configLink, PropertyContainer config, String imageLink, HobsonPluginDTO.Builder builder) {
        populatePluginDTO(
            pd.getName(),
            pd.getDescription(),
            pd.getVersionString(),
            pd.getType(),
            pd.isConfigurable(),
            pd.getStatus(),
            configClassLink,
            configClass,
            configLink,
            config,
            imageLink,
            builder
        );
    }

    static public void populatePluginDTO(String name, String description, String version, PluginType type, Boolean configurable, PluginStatus status, String configClassLink, PropertyContainerClass configClass, String configLink, PropertyContainer config, String imageLink, HobsonPluginDTO.Builder builder) {
        builder.name(name).description(description).version(version).type(type).configurable(configurable).status(status);

        if (configClassLink != null) {
            PropertyContainerClassDTO.Builder pccdtob = new PropertyContainerClassDTO.Builder(configClassLink);
            if (configClass != null) {
                for (TypedProperty tp : configClass.getSupportedProperties()) {
                    pccdtob.supportedProperty(new TypedPropertyDTO(tp));
                }
            }
            builder.configurationClass(pccdtob.build());
        }

        if (configLink != null) {
            PropertyContainerDTO.Builder pcdtob = new PropertyContainerDTO.Builder(configLink);
            if (config != null) {
                pcdtob.values(config.getPropertyValues());
            }
            builder.configuration(pcdtob.build());
        }

        if (imageLink != null) {
            builder.image(new ImageDTO.Builder(imageLink).build());
        }
    }
}
