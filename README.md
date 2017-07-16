ISAAC: Informatics Analytic Architecture
======================

A dynamic semantic architecture for the analysis of models, logic, and language.

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

Then you can deploy to the repository of your choice using the following command:  

mvn clean deploy -Psnapshot-deploy


## Performing a release
```
$ mvn jgitflow:release-start jgitflow:release-finish \
         -DreleaseVersion=3.08 -DdevelopmentVersion=3.09-SNAPSHOT

$ mvn jgitflow:release-start jgitflow:release-finish -Prelease-deploy
```