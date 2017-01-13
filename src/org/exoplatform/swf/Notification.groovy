package org.exoplatform.swf;

class Notification implements java.io.Serializable {

  def currentBuild
  def env

  String subject
  String buildStatus = 'FAILURE'
  String color = 'red'
  String colorCode  = '#FF0000'

  Notification(currentBuild, env){
    // build status of null means successful
    this.buildStatus =  currentBuild.result ?: 'SUCCESSFUL'

    // Override default values based on build status
    if (buildStatus == 'STARTED') {
      this.color = 'yellow'
      this.colorCode = '#FFFF00'
    } else if (buildStatus == 'SUCCESSFUL') {
      this.color = 'blue'
      this.colorCode = '#00FF00'
    } else {
      this.color = 'red'
      this.colorCode = '#FF0000'
    }
    this.subject = "[exo-swf][notif] ${env.JOB_NAME} - Build #[${env.BUILD_NUMBER}] - ${buildStatus}!"
    this.currentBuild = currentBuild
    this.env = env
  }

  /**
  * Format the jenkins job message for JIRA 
  */
  public String buildJiraMarkup(String gitCommit){
    String jiraMarkupStatus = '(x)'
    if (buildStatus == 'SUCCESSFUL') {
      jiraMarkupStatus = '(/)'
    }
    def content = "${jiraMarkupStatus} ${this.buildStatus}: Integrated in !${this.env.JENKINS_URL}/images/16x16/${this.color}.png! [${this.env.JOB_NAME} #${this.env.BUILD_NUMBER}|${this.env.BUILD_URL}] " + "\n----\n" + gitCommit

    return content
  } 

  /**
  * Format the jenkins job message for HTML target (email...)
  */
  public String buildHTML(String gitCommit){
    def content = "${this.buildStatus}: Integrated in <a href='${this.env.BUILD_URL}'><img src='https://ci.exoplatform.org/images/16x16/${this.color}.png'/> <b>${this.env.JOB_NAME}</b> #${this.env.BUILD_NUMBER}</a> " + "<br/><br/><pre>${gitCommit}</pre>"

    return content
  } 
}