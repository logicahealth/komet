// This jenkins pipeline config file allows isaac to be built in jenkins, assuming your jenkins installation
// contains the necessary plugins.
// The suggested set of plugins to utilize this file is:
//
// git:3.9.1
// git-client:2.7.3
// workflow-aggregator:2.5
// pipeline-maven:3.5.11
// maven-plugin:3.1.2
// durable-task:1.25
// tasks:4.52
// junit-attachments:1.5
// ws-cleanup:0.34

pipeline {
	agent any
	options {
		buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
	}
	environment {
		MVN_TASK = "${DEPLOY ?: "install"}"
	}
	tools {
		// Maven installation declared in the Jenkins "Global Tool Configuration"
		maven 'M3' 
	}
	stages {
		stage('Build') {
			//with a gitflow pattern, we can't build master multiple times, as you can't overwrite non-snapshot builds on nexus.
			when { 
				not { 
					branch 'master' 
				}
			}
			steps {
				//by default, this runs mvn clean install.  If you want it to deploy, DEPLOY should be specified in jenkins -> configure system -> env variables
				//Set it to something like 'deploy -DaltDeploymentRepository=snapshotRepo::default::http://52.61.165.55:9092/nexus/content/repositories/snapshots/'
				sh "mvn clean $MVN_TASK"
				openTasks high: 'FIXME', normal: 'TODO', pattern: '**/*.java'
			}
		}
	}
	post { 
		always { 
			junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
			cleanWs()
		}
	}
}