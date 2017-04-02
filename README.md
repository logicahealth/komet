ISAAC
======================

ISAAC Object Chronicle Project

To deploy, set a profile in your settings.xml with the repository you want to deploy to, 
patterned after these entries:

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

 

mvn clean deploy -Psnapshot-deploy

Release Notes
mvn jgitflow:release-start jgitflow:release-finish -DreleaseVersion=3.08 -DdevelopmentVersion=3.09-SNAPSHOT

mvn jgitflow:release-start jgitflow:release-finish -Prelease-deploy