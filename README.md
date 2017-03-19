ISAAC
======================

ISAAC Object Chronicle Project

mvn clean deploy -DaltDeploymentRepository=serverId::default::http://artifactory.isaac.sh/artifactory/libs-snapshot

Release Notes
mvn jgitflow:release-start jgitflow:release-finish -DreleaseVersion=3.08 -DdevelopmentVersion=3.09-SNAPSHOT

To run HP Fortify scan on child projects (assuming Fortify application and license installed)
        $ mvn -Dmaven.test.skip=true -Dfortify.sca.buildId={PROJECT_NAME} -Dfortify.sca.toplevel.artifactId=isaac-parent com.hpe.security.fortify.maven.plugin:sca-maven-plugin:clean
        $ mvn -Dmaven.test.skip=true -Dfortify.sca.buildId={PROJECT_NAME} -Dfortify.sca.toplevel.artifactId=isaac-parent com.hpe.security.fortify.maven.plugin:sca-maven-plugin:translate
        $ mvn -Dmaven.test.skip=true -Dfortify.sca.buildId={PROJECT_NAME} -Dfortify.sca.toplevel.artifactId=isaac-parent com.hpe.security.fortify.maven.plugin:sca-maven-plugin:scan



