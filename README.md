ISAAC: Informatics Analytic Architecture
======================
[![Knowledge%20Snapshot%20Build Actions Status](https://github.com/logicahealth/komet/workflows/Knowledge%20Snapshot%20Build/badge.svg)](https://github.com/logicahealth/komet/actions)

A dynamic semantic architecture for the analysis of models, logic, and language.


## Environment

* Java 11 (Oracle or OpenJDK) (Java 8 support deprecated, and on another branch of the source code)
* Maven  3.5 or newer

## JDK 11 notes
Launch the GUI using sh.komet.fx.stage.Komet, rather than MainApp, to avoid all sorts of JavaFX problems with modular java.


## Linux Issues
To allow the debugger to work properly when debugging an FX GUI app, you likely will need to disable screengrab.

```
-Dsun.awt.disablegrab=true
```

## KOMET notes
To enable the GUI to load content directly from your Maven repository, set the parameter as appropriate:

```
 -DM2_PATH=/mnt/STORAGE/Work/Maven/repository/
```
## developer flags...
```
-DSHOW_BETA_FEATURES=true 
-DISAAC_DEBUG=true
```

## Apple Issues

Use "jenv exec mvn clean install" to get maven using the correct jdk.

## IntelliJ issues

* IntelliJ memory issues - https://stackoverflow.com/questions/13578062/how-to-increase-ide-memory-limit-in-intellij-idea-on-mac#13581526

Be aware... newer versions of IntelliJ "should" make this a non issue as is indicated in the last post in that file:

It looks like IDEA solves this for you (like everything else). When loading a large project and letting it thrash, it will open a dialog to up the memory settings. Entering 2048 for Xmx and clicking "Shutdown", then restarting IDEA makes IDEA start up with more memory. This seems to work well for Mac OS, though it never seems to persist for me on Windows (not sure about IDEA 12).

## Gitflow
This project uses GitFlow: https://nvie.com/posts/a-successful-git-branching-model/
There are tools in the maven package to make branching easier **IF** you want the version number in all of the poms changed to match to the 
feature branch name.  If you do NOT want to change the poms when you make a feature branch, you should probably use another mechanism to branch.

## Creating a feature branch
```
mvn gitflow:feature-start
```
You will be prompted for the feature name:
```
What is a name of feature branch? feature/: F2
```
## Finishing a feature branch
```
mvn gitflow:feature-finish
```


## Deploying to a repository

* Option 1: To deploy, set a profile in your settings.xml with the repository you want to deploy to, patterned after these entries:

```xml
  <profile>
      <id>release-deploy</id>
      <properties>
        <altDeploymentRepository>central::default::http://artifactory.isaac.sh/artifactory/libs-release-local</altDeploymentRepository>
      </properties>
  </profile>

  <profile>
      <id>snapshot-deploy</id>
      <properties>
         <altDeploymentRepository>central::default::http://artifactory.isaac.sh/artifactory/libs-snapshot-local</altDeploymentRepository>
      </properties>
  </profile>

```

* Option 2: just use the parameter:

```
-DaltDeploymentRepository=central::default::http://artifactory.isaac.sh/artifactory/libs-snapshot-local

```

**Note, in either case, the value 'central' is a variable that needs to align with a 'server' section in your settings.xml file.**

```xml
  <server>
    <id>central</id>
    <username>your server username</username>
    <password>your server password</password>
  </server>
```

Then you can deploy to the repository of your choice using the following command:  

```
mvn clean deploy -Psnapshot-deploy
```

## Bitbucket Pipelines 
There is a Bitbucket pipelines configuration provided, which builds the source on openjdk11.  It also provides a custom deploy step you can run.
Before running the custom (snapshot) deploy step in bitbucket, you need to set the following three variables in your bitbucket pipelines configuration:
```
DEPLOYMENT_SNAPSHOT_REPO
REPO_USERNAME
REPO_PASSWORD
```
#GitFlow Implementation
We use the Git-Flow Maven plugin hosted on GitHub by Aleksandr Mashchenko

https://github.com/aleksandr-m/gitflow-maven-plugin

## Travis
Travis is already configured for this repository.  See the .travis.yml file for how to configure it, if you are forking and want to use travis on your own 
server.

## Jenkins
The provided Jenkinsfile runs this as a jenkins pipeline.  See comments in the Jenkinsfile for details on what plugins need to be installed on your Jenkins server
to build this code.

## Performing a release
Make sure that offline is set to false in your settings.xml file. 

```
$ mvn gitflow:release-start gitflow:release-finish\
         -DreleaseVersion=4.64 -DdevelopmentVersion=4.65-SNAPSHOT\
         -DpostReleaseGoals="clean deploy"

$ mvn gitflow:release -Prelease
```

## Other tips

To turn off messages such as the following from JAXB:

```
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by com.sun.xml.bind.v2.runtime.reflect.opt.Injector (file:/home/tra/.m2/repository/com/sun/xml/bind/jaxb-impl/2.3.0/jaxb-impl-2.3.0.jar) to method java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int)
WARNING: Please consider reporting this to the maintainers of com.sun.xml.bind.v2.runtime.reflect.opt.Injector
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
```

Add the following flag to the java start  command

```
--add-opens java.base/java.lang=ALL-UNNAMED
```
