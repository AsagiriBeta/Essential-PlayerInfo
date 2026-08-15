package com.jackdaw.essentialinfo.auxiliary.userInfo;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class YamlUtils {
    private YamlUtils() {
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> readFile(File file) throws IOException {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Object loaded = yaml.load(inputStream);
            if (!(loaded instanceof Map<?, ?> map)) {
                return new HashMap<>();
            }
            return new HashMap<>((Map<String, Object>) map);
        }
    }

    public static void writeFile(File file, Object object) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        Representer representer = new Representer(options);
        Yaml yaml = new Yaml(representer, options);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(object, writer);
        }
    }
}
