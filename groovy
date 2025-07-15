pipeline {
  agent { label 'dev' }

  environment {
    IMAGE_NAME = 'my-html-app'
    DOCKERHUB_REPO = 'umakrishna/my-html-app'
    CONTAINER_NAME = 'html-container'
  }

  stages {
    stage('Clone Repository') {
      steps {
        git branch: 'main', url: 'https://github.com/umakrishna-2002/docker.git'
      }
    }

    stage('Build Docker Image') {
      steps {
        sh 'docker build -t $IMAGE_NAME .'
      }
    }

    stage('Tag Image') {
      steps {
        sh 'docker tag $IMAGE_NAME $DOCKERHUB_REPO:latest'
      }
    }

    stage('Login to Docker Hub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
        }
      }
    }

    stage('Push to Docker Hub') {
      steps {
        sh 'docker push $DOCKERHUB_REPO:latest'
      }
    }

    stage('Stop & Remove Old Container') {
      steps {
        sh '''
          docker stop $CONTAINER_NAME || true
          docker rm $CONTAINER_NAME || true
        '''
      }
    }

    stage('Run Container') {
      steps {
        sh 'docker run -d --name $CONTAINER_NAME -p 80:80 $DOCKERHUB_REPO:latest'
      }
    }
  }
}
