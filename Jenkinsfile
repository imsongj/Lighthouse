node {
    stage('Checkout') {
        sh "echo start checkout"
        git url: 'https://a409:h9NAosdLaePBZytxr48f@lab.ssafy.com/s09-webmobile2-sub2/S09P12A409.git', branch: 'develop'
        sh "echo checkout done"
    }

    stage('Build') {
        sh "echo start building ,,"
        sh "cd backend/lighthouse"

        dir ('backend') {
            dir ('lighthouse') {
                sh "pwd"
                sh "ls"
                sh "chmod +x gradlew"
                sh "./gradlew compileQuerydsl"
                sh "./gradlew compileJava"
            }
        }
        
        sh "echo build finished"
    }

    stage ('unit test') {

    }

    stage('Deploy') {
        sh "echo deployment not yet determined"
    }
}