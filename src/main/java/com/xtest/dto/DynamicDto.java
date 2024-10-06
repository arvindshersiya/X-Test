package com.xtest.dto;

import java.util.HashMap;
import java.util.Map;

public class DynamicDto {
    private Map<String, Object> properties;

    public DynamicDto() {
        this.properties = new HashMap<>();
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
