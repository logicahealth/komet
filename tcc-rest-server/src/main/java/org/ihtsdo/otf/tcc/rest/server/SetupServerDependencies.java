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

    public void run(String args[]) throws IOException {
        String appHome = System.getProperty("user.home") + "/app-server";

        context.log("App home: " + appHome);

        File app = new File(appHome);

        if (!app.exists()) {
            app.mkdir();
        }

        context.log("The default user settings file " + MavenCli.DEFAULT_USER_SETTINGS_FILE.getAbsolutePath());
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
        System.setProperty("org.ihtsdo.otf.tcc.datastore.working-dir", appHome);
        System.setProperty("org.ihtsdo.otf.tcc.datastore.bdb-location", appHome + "/berkeley-db");

        MavenCli cli = new MavenCli();

        // Write to the pom.xml
        File pomDir = new File(appHome);

        if (!pomDir.exists()) {
            pomDir.mkdirs();
        }

        String username = System.getProperty("org.ihtsdo.otf.tcc.repository.username");
        String password = System.getProperty("org.ihtsdo.otf.tcc.repository.password");
        String pomResource = "/WEB-INF/classes/org/ihtsdo/otf/serversetup/pom.xml";
        String settingsResource = "/WEB-INF/classes/org/ihtsdo/otf/serversetup/settings.xml";

        context.log("pom: " + context.getResource(pomResource));

        BufferedReader pomReader =
                new BufferedReader(new InputStreamReader(context.getResourceAsStream(pomResource), "UTF-8"));
        BufferedWriter pomWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pomDir
                + "/pom.xml"), "UTF-8"));

        try {
            String settingsLine;

            while ((settingsLine = pomReader.readLine()) != null) {
                pomWriter.write(settingsLine);
            }
        } finally {
            pomReader.close();
            pomWriter.close();
        }

        // Write the settings.xml file

        context.log("settings: " + context.getResource(settingsResource));
        File settingsFile = new File(appHome, "settings.xml");
        context.log("settings path: " + settingsFile.getAbsolutePath());

        BufferedReader settingsReader =
                new BufferedReader(new InputStreamReader(context.getResourceAsStream(settingsResource), "UTF-8"));
        BufferedWriter settingsWriter = new BufferedWriter(
                new OutputStreamWriter(
                new FileOutputStream(settingsFile), "UTF-8"));
        
        try {
            String s;

            while ((s = settingsReader.readLine()) != null) {
                s = s.replace("<username>user</username>", "<username>" + username + "</username>");
                s = s.replace("<password>password</password>", "<password>" + password + "</password>");
                s = s.replace("<localRepository>mvn-repo</localRepository>", "<localRepository>" + appHome + "/mvn-repo</localRepository>");
                settingsWriter.write(s);
            }
        } finally{
            settingsReader.close();
            settingsWriter.close();
        }

        if ((args == null) || (args.length == 0)) {
            args = new String[]{"-settings",
                settingsFile.getAbsolutePath(),
                "--update-snapshots",
                "install"};
        }

        ByteArrayOutputStream stringStream = new ByteArrayOutputStream();
        PrintStream mavenOutputStream = new PrintStream(stringStream);
        int result = cli.doMain(args, pomDir.getAbsolutePath(), mavenOutputStream,
                mavenOutputStream);

        context.log(stringStream.toString());

        if (result == 0) {
            context.log("Embedded maven build succeeded: " + result);
        } else {
            context.log("Embedded maven build failed: " + result);
            throw new IOException("Embedded maven build failed");
        }
    }
}
