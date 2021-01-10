package net.minecraftforge.mapsy.util.mcp;

import java.util.Map;

public class MCPConfig {

    public String version;
    private Map<String, Object> data;

    public transient Map<String, byte[]> zipFiles;

    @SuppressWarnings ("unchecked")
    public String getData(String... path) {
        if (data == null) { return null; }
        Map<String, Object> level = data;
        for (String part : path) {
            if (!level.containsKey(part)) { return null; }
            Object val = level.get(part);
            if (val instanceof String) { return (String) val; }
            if (val instanceof Map) { level = (Map<String, Object>) val; }
        }
        return null;
    }
}