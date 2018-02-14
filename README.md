ISAAC: Informatics Analytic Architecture
======================

[![Build Status](https://travis-ci.org/darmbrust/ISAAC.svg?branch=develop)](https://travis-ci.org/darmbrust/ISAAC) [![Dependency Status](https://www.versioneye.com/user/projects/5a83a90c0fb24f704a7142d0/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/5a83a90c0fb24f704a7142d0)

A dynamic semantic architecture for the analysis of models, logic, and language.

This software now depends on the Oracle Berkeley JE database. This database is licensed under Apache 2, 
so the license of the database is not an issues. Unfortunately, Oracle does not choose to distribute this database
via maven central, so developers may have to manually download and install the Berkeley DB JE artifacts themselves. 

If this is the case, Berkeley DB JE can be downloaded from: 

http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/downloads/index.html

Download the Berkeley DB Java Edition 7.5.11, and upload it to your repository using the following coordinate: 

```
   <groupId>com.sleepycat</groupId>
   <artifactId>je</artifactId>
   <version>7.5.11</version>

```
Help for manual install may be found here: 

https://www.mkyong.com/maven/how-to-include-library-manully-into-maven-local-repository/

If any developer or sponsor wants to take a lead on porting the database to Xodus (also Apache 2, but 
distributed via maven central), please make yourselves known, and we will help you get started :-)



## FAQ

1) If you are running the build for the first time and it's broken with the following error:

[ERROR] /Users/patrick/s/osehra/ISAAC/core/api/src/main/java/sh/isaac/api/util/HeadlessToolkit.java:[101,33] cannot find symbol
[ERROR]   symbol:   class HitInfo

Make sure java is at the latest 1.8.x version.  Also, make sure maven is using the same jdk.  If you are using jenv to have multiple java versions on your machine... be aware jenv does not set the output of /usr/libexec/java correctly.

Example:

# jenv versions
  system
  1.8
* 1.8.0.92 (set by /Users/patrick/.jenv/version)
  9-ea
  oracle64-1.8.0.92
  oracle64-9-ea

# java -version
java version "1.8.0_92"
Java(TM) SE Runtime Environment (build 1.8.0_92-b14)
Java HotSpot(TM) 64-Bit Server VM (build 25.92-b14, mixed mode)

# mvn -version
Apache Maven 3.5.0 (ff8f5e7444045639af65f6095c62210b5713f426; 2017-04-03T13:39:06-06:00)
Maven home: /usr/local/Cellar/maven/3.5.0/libexec
Java version: 9-ea, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.12.6", arch: "x86_64", family: "mac"

# jenv exec mvn -version
Apache Maven 3.5.0 (ff8f5e7444045639af65f6095c62210b5713f426; 2017-04-03T13:39:06-06:00)
Maven home: /usr/local/Cellar/maven/3.5.0/libexec
Java version: 1.8.0_92, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.12.6", arch: "x86_64", family: "mac"


Use "jenv exec mvn clean install" to get maven using the correct jdk.

The first time I attempted to build this app these were the problems I had and creating the proper settings.xml file and running "jenv exec mvn clean install" fixed the problem.

2) IntelliJ memory issues.

https://stackoverflow.com/questions/13578062/how-to-increase-ide-memory-limit-in-intellij-idea-on-mac#13581526

Be aware... newer versions of IntelliJ "should" make this a non issue as is indicated in the last post in that file:

It looks like IDEA solves this for you (like everything else). When loading a large project and letting it thrash, it will open a dialog to up the memory settings. Entering 2048 for Xmx and clicking "Shutdown", then restarting IDEA makes IDEA start up with more memory. This seems to work well for Mac OS, though it never seems to persist for me on Windows (not sure about IDEA 12).




## Deploying to a repository
To deploy, set a profile in your settings.xml with the repository you want to deploy to, 
patterned after these entries:



```
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

Or, alternatively, just use the parameter: 
```
-DaltDeploymentRepository=central::default::http://artifactory.isaac.sh/artifactory/libs-snapshot-local

```
Note, in either case, the value 'central' is a variable that needs to align with a 'server' section in your settings.xml file.
```
  <server>
    <id>central</id>
    <username>your server username</username>
    <password>your server password</password>
  </server>
```

Then you can deploy to the repository of your choice using the following command:  

mvn clean deploy -Psnapshot-deploy

## Bitbucket Pipelines 
There is a Bitbucket pipelines configuration provided, which builds the source on openjdk8.  It also provides a custom deploy step you can run.
Before running the custom (snapshot) deploy step in bitbucket, you need to set the following three variables in your bitbucket pipelines configuration:
```
DEPLOYMENT_SNAPSHOT_REPO
REPO_USERNAME
REPO_PASSWORD
```

## Performing a release

Make sure that offline is set to false in your settings.xml file. 
```
$ mvn jgitflow:release-start jgitflow:release-finish \
         -DreleaseVersion=3.08 -DdevelopmentVersion=3.09-SNAPSHOT

$ mvn jgitflow:release-start jgitflow:release-finish -Prelease-deploy
```
