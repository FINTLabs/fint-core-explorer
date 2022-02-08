// pipeline {
//     agent { label 'docker' }
//     stages {
//         stage('Build') {
//             steps {
//                 withDockerRegistry([credentialsId: 'fintlabsacr.azurecr.io', url: 'https://fintlabsacr.azurecr.io']) {
//                     sh "docker build --tag ${GIT_COMMIT} ."
//                 }
//             }
//         }
//         stage('Publish') {
//             when { branch 'main' }
//             steps {
//                 sh "docker tag ${GIT_COMMIT} fintlabsacr.azurecr.io/fint-core-explorer:build.${BUILD_NUMBER}_${GIT_COMMIT}"
//                 withDockerRegistry([credentialsId: 'fintlabsacr.azurecr.io', url: 'https://fintlabsacr.azurecr.io']) {
//                     sh "docker push fintlabsacr.azurecr.io/fint-core-explorer:build.${BUILD_NUMBER}_${GIT_COMMIT}"
//                 }
//                 kubernetesDeploy configs: 'k8s.yaml', kubeconfigId: 'aks-beta-fint'
//                 kubernetesDeploy configs: 'k8s.yaml', kubeconfigId: 'aks-api-fint'
//             }
//         }
//     }
// }