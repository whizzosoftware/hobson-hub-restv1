package com.whizzosoftware.hobson.rest.v1.util;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.property.*;
import com.whizzosoftware.hobson.dto.*;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskActionClassResource;
import com.whizzosoftware.hobson.rest.v1.resource.task.TaskConditionClassResource;
import org.restlet.routing.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DTOMapper {
    private static Template actionClassesTemplate;
    private static Template conditionClassesTemplate;

    static {
        actionClassesTemplate = new Template("/api/v1" + TaskActionClassResource.PATH);
        conditionClassesTemplate = new Template("/api/v1" + TaskConditionClassResource.PATH);
    }

    static public PropertyContainerSetDTO mapPropertyContainerSet(PropertyContainerSet pcs, LinkProvider links) {
        PropertyContainerSetDTO dto = new PropertyContainerSetDTO(pcs);
        dto.setName(pcs.getName());
        return dto;
    }

    static public PropertyContainerSet mapPropertyContainerSetDTO(PropertyContainerSetDTO dto, LinkProvider links) {
        try {
            if (dto != null) {
                PropertyContainerSet pcs = new PropertyContainerSet();
                pcs.setId(dto.getId());
                pcs.setName(dto.getName());
                pcs.setPrimaryProperty(mapPropertyContainerDTO(dto.getPrimaryContainer()));
                pcs.setProperties(mapPropertyContainerDTOList(dto.getContainers()));
                return pcs;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new HobsonRuntimeException("Unable to map", e);
        }
    }

    static public List<PropertyContainer> mapPropertyContainerDTOList(List<PropertyContainerDTO> dtos) {
        List<PropertyContainer> results = null;
        if (dtos != null) {
            results = new ArrayList<>();
            for (PropertyContainerDTO dto : dtos) {
                results.add(mapPropertyContainerDTO(dto));
            }
        }
        return results;
    }

    static public PropertyContainer mapPropertyContainerDTO(PropertyContainerDTO dto) {
        PropertyContainer pc = null;
        if (dto != null) {
            pc = new PropertyContainer();
            pc.setId(dto.getId());
            pc.setName(dto.getName());
            pc.setContainerClassContext(mapPropertyContainerClassDTO(dto.getContainerClass()).getContext());
            pc.setPropertyValues(dto.getPropertyValues());
        }
        return pc;
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

    static public TypedProperty mapTypedPropertyDTO(TypedPropertyDTO dto) {
        return new TypedProperty(dto.getId(), dto.getName(), dto.getDescription(), TypedProperty.Type.valueOf(dto.getMediaType()));
    }

    static public PropertyContainerClassContext createPropertyContainerClassContext(String id) {
        Map<String,Object> vars = new HashMap<>();
        if (conditionClassesTemplate.match(id) > -1) {
            conditionClassesTemplate.parse(id, vars);
        } else if (actionClassesTemplate.match(id) > -1) {
            actionClassesTemplate.parse(id, vars);
        }
        return PropertyContainerClassContext.create((String)vars.get("userId"), (String)vars.get("hubId"), (String)vars.get("pluginId"), (String)vars.get("containerClassId"));
    }
}
