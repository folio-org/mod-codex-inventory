

buildMvn {
  publishModDescriptor = 'yes'
  publishAPI = 'no'
  mvnDeploy = 'yes'
  sqBranch = ['sq']

  doDocker = {
    buildJavaDocker {
      publishMaster = 'yes'
      healthChk = 'yes'
      healthChkCmd = 'curl -sS --fail -o /dev/null  http://localhost:8081/apidocs/ || exit 1'
    }
  }
}

