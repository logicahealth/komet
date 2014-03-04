OTF-Versioning-Service
======================

Repository for the versioning module

JavaFX
------

If you do not have access to a repository with the jfxrt libraries, then you
will need to copy them to your local repository before building the project.
This can be done with the following command:

```
mvn org.codeartisans.javafx:javafx-deployer-maven-plugin:1.2:install
```


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
