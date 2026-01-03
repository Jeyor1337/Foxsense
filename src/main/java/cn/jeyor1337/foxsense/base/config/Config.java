package cn.jeyor1337.foxsense.base.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.jeyor1337.foxsense.base.manager.ModuleManager;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.ColorValue;
import cn.jeyor1337.foxsense.base.value.ModeValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import cn.jeyor1337.foxsense.base.value.Value;

public class Config {
    private final File configFile;
    private final String name;
    private final ModuleManager moduleManager;
    private final Gson gson;

    public Config(File directory, String name, ModuleManager moduleManager) {
        this.name = name;
        this.configFile = new File(directory, name + ".json");
        this.moduleManager = moduleManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void save() {
        JsonObject configJson = new JsonObject();
        JsonObject modulesJson = new JsonObject();

        for (Module module : moduleManager.getModules()) {
            JsonObject moduleJson = new JsonObject();

            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("keybind", module.getKeybind());

            if (!module.getValues().isEmpty()) {
                JsonArray valuesJson = new JsonArray();
                for (Value<?> value : module.getValues()) {
                    JsonObject valueJson = new JsonObject();
                    valueJson.addProperty("name", value.getName());

                    if (value instanceof BooleanValue) {
                        valueJson.addProperty("type", "boolean");
                        valueJson.addProperty("value", (Boolean) value.getValue());
                    } else if (value instanceof NumberValue) {
                        valueJson.addProperty("type", "number");
                        valueJson.addProperty("value", ((NumberValue) value).getValue());
                    } else if (value instanceof ModeValue) {
                        valueJson.addProperty("type", "mode");
                        valueJson.addProperty("value", (String) value.getValue());
                    } else if (value instanceof ColorValue) {
                        valueJson.addProperty("type", "color");
                        valueJson.addProperty("value", (Integer) value.getValue());
                    }

                    if (!value.getDescription().isEmpty()) {
                        valueJson.addProperty("description", value.getDescription());
                    }

                    valuesJson.add(valueJson);
                }
                moduleJson.add("values", valuesJson);
            }

            modulesJson.add(module.getName(), moduleJson);
        }

        configJson.add("modules", modulesJson);
        configJson.addProperty("version", "1.0");

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configJson, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!configFile.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject configJson = gson.fromJson(reader, JsonObject.class);

            if (configJson == null || !configJson.has("modules")) {
                return;
            }

            JsonObject modulesJson = configJson.getAsJsonObject("modules");

            for (Module module : moduleManager.getModules()) {
                String moduleName = module.getName();

                if (!modulesJson.has(moduleName)) {
                    continue;
                }

                JsonObject moduleJson = modulesJson.getAsJsonObject(moduleName);

                if (moduleJson.has("enabled")) {
                    boolean enabled = moduleJson.get("enabled").getAsBoolean();
                    module.setEnabled(enabled);
                }

                if (moduleJson.has("keybind")) {
                    int keybind = moduleJson.get("keybind").getAsInt();
                    module.setKeybind(keybind);
                }

                if (moduleJson.has("values")) {
                    JsonArray valuesJson = moduleJson.getAsJsonArray("values");
                    for (JsonElement valueElement : valuesJson) {
                        JsonObject valueJson = valueElement.getAsJsonObject();
                        String valueName = valueJson.get("name").getAsString();
                        String valueType = valueJson.get("type").getAsString();

                        Value<?> value = module.getValue(valueName);
                        if (value == null) {
                            continue;
                        }

                        JsonElement valueElementData = valueJson.get("value");
                        switch (valueType) {
                            case "boolean":
                                if (value instanceof BooleanValue) {
                                    ((BooleanValue) value).setValue(valueElementData.getAsBoolean());
                                }
                                break;
                            case "number":
                                if (value instanceof NumberValue) {
                                    ((NumberValue) value).setValue(valueElementData.getAsDouble());
                                }
                                break;
                            case "mode":
                                if (value instanceof ModeValue) {
                                    ((ModeValue) value).setValue(valueElementData.getAsString());
                                }
                                break;
                            case "color":
                                if (value instanceof ColorValue) {
                                    ((ColorValue) value).setValue(valueElementData.getAsInt());
                                }
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public File getConfigFile() {
        return configFile;
    }
}
