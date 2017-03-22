Release Note:

mvn clean deploy -DaltDeploymentRepository=<servername>::default::http://<artifact dns name>/<repo path>

mvn jgitflow:release-start jgitflow:release-finish -DuseReleaseProfile=false -DreleaseVersion=3.0 -DdevelopmentVersion=3.1-SNAPSHOT -DaltDeploymentRepository=<servername>::default::http://<artifact dns name>/<repo path>
