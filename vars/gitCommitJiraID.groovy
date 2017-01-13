String call() {
    def jiraId = null
    def lastCommitMsg =  getOutput('git log --pretty=format:"%s" -n 1').trim()
    echo lastCommitMsg
    if (lastCommitMsg != null && lastCommitMsg.contains(":")){
      jiraId = lastCommitMsg.split(':')[0]
    }
   return jiraId
}