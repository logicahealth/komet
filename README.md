VA-OCHRE
======================

ISAAC Object Chronicle Project
ISAAC's Object Chronicle (OCHRE) project provides the ability to store, retrieve, and edit identified objects, and maintains a complete chronicle of all changes.


Release Notes
mvn jgitflow:release-start jgitflow:release-finish -DreleaseVersion=2.33 -DdevelopmentVersion=2.34-SNAPSHOT -DaltDeploymentRepository=maestro::default::https://va.maestrodev.com/archiva/repository/va-releases  -DdefaultOriginUrl=https://github.com/Apelon-VA/va-ochre.git

NetBeans API
------

If you do not already have this repository in your `settings.xml`, you will need to add it:

```
<repository>
  <id>netbeans</id>
  <name>NetBeans</name>
  <url>http://bits.netbeans.org/maven2/</url>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
</repository>
```
