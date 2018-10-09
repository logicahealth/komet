/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Comparator;

/**
 *
 * @author kec
 */
public class DirectoryUtil {

    public static void cleanDirectory(String pathToBeDeleted) throws IOException {
        cleanDirectory(new File(pathToBeDeleted).toPath());
    }

    public static void cleanDirectory(File pathToBeDeleted) throws IOException {
        cleanDirectory(pathToBeDeleted.toPath());
    }

    public static void cleanDirectory(Path pathToBeDeleted) throws IOException {
        deleteDirectory(pathToBeDeleted);
        Files.createDirectory(pathToBeDeleted);
    }

    public static void deleteDirectory(String pathToBeDeleted) throws IOException {
        deleteDirectory(new File(pathToBeDeleted).toPath());
    }

    public static void deleteDirectory(File pathToBeDeleted) throws IOException {
        deleteDirectory(pathToBeDeleted.toPath());
    }

    public static void deleteDirectory(Path pathToBeDeleted) throws IOException {
        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static void moveDirectory(Path originalPath, Path newPath) throws IOException {
        Files.walk(originalPath)
                .forEach(source -> copy(source, newPath.resolve(originalPath.relativize(source))));
        deleteDirectory(originalPath);
    }

    private static void copy(Path source, Path dest) {
        try {
            if (Files.exists(dest) && Files.isDirectory(dest)) {
                // delete directory, then copy. 
                deleteDirectory(dest);
            } 
            Files.copy(source, dest, REPLACE_EXISTING);
            
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
