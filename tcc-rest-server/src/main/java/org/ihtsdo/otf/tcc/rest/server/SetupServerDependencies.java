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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.apache.maven.cli.MavenCli;

/**
 *
 * @author kec
 */
public class SetupServerDependencies {

    ServletContext context;

    public SetupServerDependencies(ServletContext context) {
        this.context = context;
    }

    public void run(String args[]) {
        String appHome = System.getProperty("user.home") + "/app-server";
        System.out.println("App home: " + appHome);
        File app = new File(appHome);
        if(!app.exists()){
            app.mkdir();
        }
        System.out.println("The default user settings file " + MavenCli.DEFAULT_USER_SETTINGS_FILE.getAbsolutePath());
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
        System.setProperty("org.ihtsdo.otf.tcc.datastore.working-dir", appHome);
        System.setProperty("org.ihtsdo.otf.tcc.datastore.bdb-location", appHome + "/berkeley-db");
        System.setProperty(MavenCli.LOCAL_REPO_PROPERTY, appHome + "/.m2");

        MavenCli cli = new MavenCli();

        //Write to the pom.xml
        File pomDir = new File(appHome + "/src/main/resources/org/ihtsdo/otf/server-setup");
        if (!pomDir.exists()) {
            pomDir.mkdirs();
        }

        BufferedReader pomReader = null;
        BufferedWriter pomWriter = null;

        InputStream pom = null;
        String settingsLine = "";
        try {
            pom = context.getResourceAsStream("/WEB-INF/classes/org/ihtsdo/otf/serversetup/pom.xml");
            pomReader = new BufferedReader(new InputStreamReader(pom, "UTF-8"));
            pomWriter = new BufferedWriter(
                    new OutputStreamWriter(
                    new FileOutputStream(pomDir + "/pom.xml"),
                    "UTF-8"));
            while ((settingsLine = pomReader.readLine()) != null) {
                pomWriter.write(settingsLine);
            }

            pomReader.close();
            pomWriter.close();
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            Logger.getLogger(SetupServerDependencies.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SetupServerDependencies.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SetupServerDependencies.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Write the settings.xml file
        File m2Home = new File(appHome + "/.m2");
        if (!m2Home.exists()) {
            m2Home.mkdir();
        }

        BufferedReader settingsReader = null;
        BufferedWriter settingsWriter = null;

        String s = "";
        try {
            settingsReader = new BufferedReader(new InputStreamReader(new FileInputStream(MavenCli.DEFAULT_USER_SETTINGS_FILE.getAbsolutePath()), "UTF-8"));
            settingsWriter = new BufferedWriter(
                    new OutputStreamWriter(
                    new FileOutputStream(m2Home + "/settings.xml"),
                    "UTF-8"));
            while ((s = settingsReader.readLine()) != null) {
                settingsWriter.write(s);
            }

            settingsReader.close();
            settingsWriter.close();
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            Logger.getLogger(SetupServerDependencies.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SetupServerDependencies.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SetupServerDependencies.class.getName()).log(Level.SEVERE, null, ex);
        }

        if ((args == null) || (args.length == 0)) {
            args = new String[]{"install"};
        }

        int result = cli.doMain(args, pomDir.getAbsolutePath(), System.out, System.out);
        if (result == 0) {
            System.out.println("Embedded maven build succeeded: " + result);
        } else {
            System.out.println("Embedded maven build failed: " + result);
        }
    }
}
