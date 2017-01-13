
/**
* Send notifications by mail
*/
String call(env, currentBuild, String mailTo = 'exo-swf-notifications@exoplatform.com'){

  def msg = new org.exoplatform.swf.Notification(currentBuild, env)

  // Send message if previous build failed
  if((msg.buildStatus.equals('SUCCESSFUL') && !hudson.model.Result.SUCCESS.equals(currentBuild.rawBuild.getPreviousBuild()?.getResult()))
  || msg.buildStatus.equals('FAILURE')) {
    emailext(to: mailTo,subject: msg.subject,body: msg.buildHTML(gitCommit()), attachLog: true, recipientProviders: [[$class: 'FailingTestSuspectsRecipientProvider'], [$class: 'FirstFailingBuildSuspectsRecipientProvider'], [$class: 'UpstreamComitterRecipientProvider'], [$class: 'CulpritsRecipientProvider']])
  } 
  else {
    echo 'no mail sent, still SUCCESS status!'
  }
}