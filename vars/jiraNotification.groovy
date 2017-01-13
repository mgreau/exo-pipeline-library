String call(env, currentBuild){

    def jiraId = gitCommitJiraID()

    if (jiraId != null){
        def msg = new org.exoplatform.swf.Notification(currentBuild, env)
        // Send message if previous build failed
        if((msg.buildStatus.equals('SUCCESSFUL') && !hudson.model.Result.SUCCESS.equals(currentBuild.rawBuild.getPreviousBuild()?.getResult()))
            || msg.buildStatus.equals('FAILURE')) {    
            def content = msg.buildJiraMarkup(gitCommit())
            jiraComment(issueKey: jiraId, body: content)
        }
    }
    else {
        echo "No JIRA_ID in the commit message."
    }
}