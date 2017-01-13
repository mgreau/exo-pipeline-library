String call() {
  if (env.GIT_COMMIT == null) {
    env.GIT_COMMIT = getOutput("git log --stat --pretty=short --graph -n 1").trim()
  }
  env.GIT_COMMIT
}