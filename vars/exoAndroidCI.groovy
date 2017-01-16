/**
*  
*/
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def exoSWF = new org.exoplatform.swf.ExoSWFCommands()
    def utils = new org.exoplatform.swf.Utils()

    //init values
    def M2_REPO_IN_CONTAINER = utils.getValue('m2RepositoryPath', '/home/ciagent/.m2/repository', config, env)
    def JOB_SUFFIX = utils.getValue('jobSuffix', 'ci', config, env)
    def GRADLE_GOALS = utils.getValue('gradleTask', 'clean test assemble', config, env)
    def MAVEN_SETTINGS_FILE_ID = utils.getValue('m2SettingsId', 'exo-ci-maven-settings', config, env)
    def GIT_CREDENTIALS_ID = utils.getValue('gitCredentialsId', 'ciagent', config, env)
    def DOCKER_RUN_PARAMS = utils.getValue('dockerRunParams', '', config, env)
    def JOB_NAME = env.JOB_NAME.replaceAll('/','-')

    // required values
    def DOCKER_IMAGE = utils.getValue('dockerImage', '', config, env)
    def GIT_URL = utils.getValue('gitUrl', '', config, env)
    def GIT_BRANCH = utils.getValue('gitBranch', '', config, env)

    // Mail configuration
    def mailTo = "mgreau@exoplatform.com"

    def eXoAndroid = docker.image(DOCKER_IMAGE);
    def pipelineError = null

    stage('Checkout ' + GIT_BRANCH){
      // checkout scm with credentials
      git branch: GIT_BRANCH, credentialsId: GIT_CREDENTIALS_ID, url: GIT_URL
    }

    stage('Build'){
      // Create m2 cache (use docker volume)
      def m2Cache = exoSWF.createMavenCacheVolume(JOB_NAME)
      // Use custom settings.xml file on project workspace directory
      configFileProvider(
                  [configFile(fileId: "${MAVEN_SETTINGS_FILE_ID}",  targetLocation: 'settings.xml')]) {
        try {
          eXoAndroid.inside("${DOCKER_RUN_PARAMS} -v ${m2Cache}:${M2_REPO_IN_CONTAINER}") {
            sh "gradle ${GRADLE_GOALS}"
          }
        } catch (error) {
          currentBuild.result = 'FAILURE'
          pipelineError = error
        } finally {
          // Delete temporary settings file
          sh 'rm -f settings.xml'
        }
      }
    }
    stage('Publish Reports'){
      junit allowEmptyResults: true, testResults: '**/app/build/test-results/release/*.xml'
    }

    stage('Send Notifications'){

      // Send notification to inform about Build status
      mailNotification(env,currentBuild, mailTo)
      // Add comment to JIRA
      jiraNotification(env,currentBuild)
      
      // Clean up the workspace at the end (except in failure, and unstable cases)
      step([$class: 'WsCleanup', cleanWhenSuccess: false, cleanWhenFailure: false, cleanWhenUnstable: false])
    }
}