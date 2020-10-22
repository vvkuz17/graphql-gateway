package ru.yandex.cloud.graphql.gateway.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class FileUtils {
    public static String readFile(String path) {
        try {
            return Files.exists(Paths.get(path)) ?
                    String.join("\n", Files.readAllLines(Paths.get(path))) :
                    Resources.toString(Resources.getResource(path), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
