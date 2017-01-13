#!/usr/bin/groovy
package org.exoplatform.swf;

import groovy.json.JsonSlurper

/**
* Replace value of any element in Maven POM XMl file.
*
* @pathToSearchPoms path to find pom.xml files
* @xmlPath XMLPath to select XML elements in POM
* @searchedValue value to find in pom.xml files
* @replaceWith value to set inplace of find value
*/
def replaceInPoms(String pathToSearchPoms, String xmlPath, String searchedValue, String replaceWith){
    sh "pwd && find ${pathToSearchPoms} -type f -name pom.xml | xargs -t -n 1 xmlstarlet ed -P --inplace -N pom=http://maven.apache.org/POM/4.0.0 -u \"${xmlPath}[contains(text(), '${searchedValue}')]\" -x \"concat(substring-before(.,'${searchedValue}'), '${replaceWith}')\""
}

/**
* Replace POM version
*
* @pathToSearchPoms path to find pom.xml files
* @searchedValue value to find in pom.xml files
* @replaceWith value to set inplace of find value
*/
def updatePomVersion(String pathToSearchPoms, String searchedValue, String replaceWith){
  replaceInPoms(pathToSearchPoms, '/pom:project/pom:version', searchedValue, replaceWith)
}

/**
* Replace Parent POM version
* @pathToSearchPoms path to find pom.xml files
* @searchedValue value to find in pom.xml files
* @replaceWith value to set inplace of find value
*/
def updateParentPomVersion(String pathToSearchPoms, String searchedValue, String replaceWith){
  replaceInPoms(pathToSearchPoms, '/pom:project/pom:parent/pom:version', searchedValue, replaceWith)
}

/**
* Replace POM property value
* @pathToSearchPoms path to find pom.xml files
* @searchedValue value to find in pom.xml files
* @replaceWith value to set inplace of find value
*/
def updatePomPropertyValue(String pathToSearchPoms, String property, String searchedValue, String replaceWith){
  replaceInPoms(pathToSearchPoms, "/pom:project/pom:properties/pom:${property}", searchedValue, replaceWith)
}


/**
* Create volume cache for .m2 repository
*/
def createMavenCacheVolume(String jobName){
  def volumeName = "m2-cache-${jobName}"
  sh "docker volume create --name m2-cache-${jobName}"

  return volumeName
}

/**
* Reset existing remote branch from source branch in different remote url
* @param path: path to the git project
* @param project project name to process
* @param gitUrl: URL Git used to clone the project
* @param sourceRemoteId: identifier for the remote where to find the source code (ex: exoplatform)
* @param sourceBranch git branch source
* @param targetRemoteId identifier for the remote where to push the target code (ex: exodev)
* @param targetBranch git branch to update/create
*/
def resetRemoteBranch(path, project, gitUrl, sourceRemoteId, sourceBranch, targetRemoteId, targetBranch){
   // to work in a container (JENKINS-33510)
   def cdToPath = ''
   if (path != null && !path.equals('')){
     cdToPath="cd ${path} && "
   }
   // Update git project
   sh "${cdToPath} pwd && git config --list"

   // Add remote URL for source code to base on (sourceRemoteUrl)
   if (!sourceRemoteId.equals(targetRemoteId)){
     def sourceRemoteUrl = "git@github.com:${sourceRemoteId}/${project}.git"
     sh "${cdToPath} git remote add source ${sourceRemoteUrl} "

     // Update git project with all remote branches
     sh "${cdToPath} git remote update --prune"
      // Update source code branch from remote
      sh "${cdToPath} git checkout -b localBranch source/${sourceBranch}"
   } else {
      // Update git project with all remote branches
      sh "${cdToPath} git remote update --prune"
      // Update source code branch from remote
      sh "${cdToPath} git checkout -b localBranch origin/${sourceBranch}"
   }
   // Clean and get last commits
   sh "${cdToPath} git clean -df"
   sh "${cdToPath} git pull"
   // Create branch and reset it to the sourceBranch
   sh "${cdToPath} git checkout -B ${targetBranch}"
   sh "${cdToPath} git reset --hard localBranch"
   // Push force translation branch to be up to date with sourceBranch
   sh "${cdToPath} git push -f origin ${targetBranch} --set-upstream"
}