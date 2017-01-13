package org.exoplatform.swf;

/**
* Get the variable value from:
* - the config job object first
* - then the config env variable
* - finally the default value:  defaultVarValue
*/
def getValue(defaultVarName, defaultVarValue, config, env) {
  if (config.containsKey(defaultVarName)){
    return config[defaultVarName]
  } else if (env."${defaultVarName}" != null){
    return env."${defaultVarName}"
  }
  return defaultVarValue
}

/**
*  Replace values in pom.xml files
*/
def replaceInPoms(String pathToSearchPoms, String findValue, String replaceWith ){
  sh "find -type f -name 'pom.xml' | xargs sed -i -r 's/${property}[0-9][0-9]{0,2}.[0-9][0-9]{0,2}(.[0-9][0-9]{0,2})?(.[0-9][0-9]{0,2})?</${property}${newVersion}</g'"
}
