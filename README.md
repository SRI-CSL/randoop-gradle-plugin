# Randoop Gradle Plugin

[![License](https://img.shields.io/badge/license-apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

(experimental) randoop gradle plugin

This Gradle plug-in creates a task to run [Randoop](https://randoop.github.io/randoop/) on Java projects.

## Integration

To use this plug-in, [you must have downloaded Randoop and have it as dependency](https://github.com/randoop/randoop/releases/latest).

Add the plug-in dependency and apply it in your project's `build.gradle`:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        ...
        classpath 'com.sri.gradle:randoop-gradle-plugin:1.0'
    }
}
```

## Applying the Plugin

### Java

```groovy
apply plugin: 'java'
apply plugin: 'com.sri.gradle.randoop'
```

## Randoop configuration

In the build.gradle of the project that applies the plugin:

```groovy
runRandoop {
    randoopJar = file("libs/randoop.jar")
    junitOutputDir = file("${projectDir}/src/test/java")
    timeoutSeconds = 30
    stopOnErrorTest = false
    flakyTestBehavior = 'output'
    noErrorRevealingTests = true
    junitReflectionAllowed = false
    usethreads = true
    outputLimit = 2000
    junitPackageName = 'com.foo'
}
```

## Randoop Tasks

-   `cleanupRandoopOutput` - Deletes Randoop-generated files in `junitOutputDir`.
-   `checkForRandoop` - Checks if Randoop is in the project's classpath.
-   `generateClassListFile` - Generates a classList.txt file from the current project's classes.
-   `generateTests` - Generates unit tests with Randoop for classes in classList.txt file.
-   `runRandoop` - Checks if Randoop generated the unit tests

Additional build properties:

-   `-Prebuild` - Tells the plugin to re-build all tests files in the specified `junitOutputDir`


## License

    Copyright (C) 2020 SRI International

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
