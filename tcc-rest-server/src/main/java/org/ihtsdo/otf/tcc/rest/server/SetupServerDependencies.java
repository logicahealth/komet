/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.rest.server;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.maven.cli.MavenCli;

//~--- JDK imports ------------------------------------------------------------
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import javax.servlet.ServletContext;

/**
 *
 * @author kec
 */
public class SetupServerDependencies {

    ServletContext context;

    public SetupServerDependencies(ServletContext context) {
        this.context = context;
    }

    public boolean execute() throws IOException {
        return execute(null);
    }

    public boolean execute(String args[]) throws IOException {
        String appHome = System.getenv("CATALINA_HOME") + "/temp/bdb";

        context.log("App home: " + appHome);

        File app = new File(appHome);

        if (!app.exists()) {
            app.mkdirs();
        }

        context.log("The default user settings file " + MavenCli.DEFAULT_USER_SETTINGS_FILE.getAbsolutePath());
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
        System.setProperty("org.ihtsdo.otf.tcc.datastore.working-dir", appHome + "/target");
        System.setProperty("org.ihtsdo.otf.tcc.datastore.bdb-location", appHome + "/target/berkeley-db");

        MavenCli cli = new MavenCli();

        // Write to the pom.xml
        File pomDir = new File(appHome);

        if (!pomDir.exists()) {
            pomDir.mkdirs();
        }

        String username = System.getProperty("org.ihtsdo.otf.tcc.repository.username");
        if (username == null || username.isEmpty()) {
            context.log("WARNING: Username is null. Please set "
                    + "'org.ihtsdo.otf.tcc.repository.username'  to a proper "
                    + "value in the CATALINA_OPTS environmental variable");
        }
        String password = System.getProperty("org.ihtsdo.otf.tcc.repository.password");
        if (password == null || password.isEmpty()) {
            context.log("WARNING: Password is null. Please set "
                    + "'org.ihtsdo.otf.tcc.repository.password' to a proper "
                    + "value in the CATALINA_OPTS environmental variable");
        }
        String pomResource = "/WEB-INF/classes/org/ihtsdo/otf/serversetup/pom.xml";
        String settingsResource = "/WEB-INF/classes/org/ihtsdo/otf/serversetup/settings.xml";

        context.log("pom: " + context.getResource(pomResource));

        File pomFile = new File(pomDir + "/pom.xml");
        if (pomFile.exists()) {
            context.log("Pom file exists. Now deleting.");
            pomFile.delete();
        }
        BufferedReader pomReader
                = new BufferedReader(new InputStreamReader(context.getResourceAsStream(pomResource), "UTF-8"));
        BufferedWriter pomWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pomFile), "UTF-8"));

        try {
            String settingsLine;

            while ((settingsLine = pomReader.readLine()) != null) {
                pomWriter.write(settingsLine + "\n");
            }
        } finally {
            pomReader.close();
            pomWriter.close();
        }

        // Write the settings.xml file
        context.log("settings: " + context.getResource(settingsResource));
        File settingsFile = new File(appHome, "settings.xml");
        if (settingsFile.exists()) {
            context.log("Settings file exists. Now deleting.");
            settingsFile.delete();
        }
        context.log("settings path: " + settingsFile.getAbsolutePath());

        BufferedReader settingsReader
                = new BufferedReader(new InputStreamReader(context.getResourceAsStream(settingsResource), "UTF-8"));
        BufferedWriter settingsWriter = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(settingsFile), "UTF-8"));

        try {
            String s;

            while ((s = settingsReader.readLine()) != null) {
                s = s.replace("<username>user</username>", "<username>" + username + "</username>");
                s = s.replace("<password>password</password>", "<password>" + password + "</password>");
                s = s.replace("<localRepository>mvn-repo</localRepository>", "<localRepository>" + appHome + "/mvn-repo</localRepository>");
                settingsWriter.write(s + "\n");
            }
        } finally {
            settingsReader.close();
            settingsWriter.close();
        }

        if ((args == null) || (args.length == 0)) {
            args = new String[]{"-e", "-C",
                "-settings", settingsFile.getAbsolutePath(),
                "-U",
                "clean", "install"};
        }

        OutputStream stringStream = new ContextLoggerStream();
        PrintStream mavenOutputStream = new PrintStream(stringStream);

        int result = cli.doMain(args, pomDir.getAbsolutePath(), mavenOutputStream,
                mavenOutputStream);

        context.log(stringStream.toString());

        if (result == 0) {
            context.log("Embedded maven build succeeded: " + result);
        } else {
            context.log("Embedded maven build failed: " + result);
            return false;
        }
        return true;
    }

    public void deleteAppDir() throws IOException {
        String appHome = System.getenv("CATALINA_HOME") + "/temp/bdb";

        context.log("App home: " + appHome);

        File app = new File(appHome);

        if (app.exists()) {
            delete(app);
        }

    }

    public void deleteBdb() throws IOException {
        String bdbDir = System.getenv("CATALINA_HOME") + "/temp/bdb/target/berkeley-db";

        context.log("Bdb home: " + bdbDir);

        File bdb = new File(bdbDir);

        if (bdb.exists()) {
            delete(bdb);
        }
    }

    public static void delete(File file)
            throws IOException {

        if (file.isDirectory()) {

            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                String files[] = file.list();

                for (String temp : files) {
                    File fileDelete = new File(file, temp);

                    delete(fileDelete);
                }

                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    private class ContextLoggerStream extends OutputStream {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            if (b == '\n' || b == '\r') {
                context.log(bytes.toString());
                bytes.reset();
            } else {
                bytes.write(b);
            }
        }

    }
}
