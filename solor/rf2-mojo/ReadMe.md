Release Note:

mvn clean deploy -DaltDeploymentRepository=vadev::default::http://vadev.mantech.com:8081/nexus/content/repositories/snapshots

mvn jgitflow:release-start jgitflow:release-finish -DuseReleaseProfile=false -DreleaseVersion=3.0 -DdevelopmentVersion=3.1-SNAPSHOT -DaltDeploymentRepository=vadev::default::http://vadev.mantech.com:8081/nexus/content/repositories/releases
