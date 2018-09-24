/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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

public class SystemUtils {

    private static final boolean MACOS = "Mac OS X".equals(System.getProperty("os.name"));
    private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final boolean LINUX = System.getProperty("os.name").toLowerCase().contains("linux");

    public static boolean isMacOS() {
        return MACOS;
    }
    public static boolean isWindows() {
        return WINDOWS;
    }
    public static boolean isLinux() {
        return LINUX;
    }
}
