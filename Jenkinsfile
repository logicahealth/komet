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
			steps {
				//DEPLOY should be specified in jenkins -> configure system -> env variables - if you don't want it to deploy, leave the value blank.
				//Or, set it to something like 'deploy -DaltDeploymentRepository=snapshotRepo::default::http://52.61.165.55:9092/nexus/content/repositories/snapshots/'
				//If you leave it blank, it will execute 'mvn clean install'
				sh "mvn clean $MVN_TASK"
				openTasks high: 'FIXME', normal: 'TODO', pattern: '**/*.java'
			}
		}
	}
	post { 
		always { 
			junit '**/target/surefire-reports/*.xml'
			cleanWs()
		}
	}
}