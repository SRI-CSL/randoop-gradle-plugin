# Randoop Gradle Plugin

[![License](https://img.shields.io/badge/license-apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

(experimental) randoop gradle plugin

This Gradle plug-in creates a task named `runRandoop` to run [Randoop](https://randoop.github.io/randoop/) on Java projects.

## Configuration

To use this plug-in you must apply the Randoop plugin to the root project’s `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
	id 'com.sri.gradle.randoop' version '0.0.1-SNAPSHOT'
}
```

Then, you must specify the location where the plugin can find Randoop,
as well as other Randoop's required parameters such as Randoop output directory,
and the name of the package for the generated JUnit files.

In the following example, Randoop is in the project's libs directory, the output directory is
the project's test directory, and the JUnit package name is `com.foo`:

```groovy
runRandoop {
    randoopJar = file("libs/randoop.jar")
    junitOutputDir = file("${projectDir}/src/test/java")
    ....
    junitPackageName = 'com.foo'
}
```

You can find an example of this configuration below:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
	id 'com.sri.gradle.randoop' version '0.0.1-SNAPSHOT'
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url 'https://plugins.gradle.org/m2/'
    }
}

dependencies {
    implementation 'com.google.guava:guava:28.0-jre'
    testImplementation 'org.hamcrest:hamcrest:2.2'
    testImplementation 'junit:junit:4.13'
}

tasks.withType(Javadoc){
	enabled = false
}

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

## Using a locally-built plugin

You can build the plugin locally rather than downloading it from Maven Central.

To build the plugin from source, run `./gradlew build`.

If you want to use a locally-built version of the plugin, you can publish the plugin to your
local Maven repository by running `./gradlew publishToMavenLocal`. Then, add the following to
the `settings.gradle` file in the Gradle project that you want to use the plugin:

```
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

## Randoop Tasks

The plug-in support the following tasks. The main task of this plug-in is the `runRandoop` task, which
runs other supporting tasks, such as `generateClassListFile`. The entire list of tasks is presented here: 

-   `cleanupRandoopOutput` - Deletes Randoop-generated files in `junitOutputDir`.
-   `checkForRandoop` - Checks if Randoop is in the project's classpath.
-   `generateClassListFile` - Generates a classList.txt file from the current project's classes.
-   `generateTests` - Generates unit tests with Randoop for classes in classList.txt file.
-   `runRandoop` - Runs Randoop test generator (Main task)

Additional build properties:

-   `-Prebuild` - Tells the plugin to re-build all tests files in the specified `junitOutputDir` location.

## Example

A simple example of how to use this plugin on a basic Java project can be found at this project's
`consumer` sub-directory. From your terminal, run the the following:

```groovy
› cd consumer
› ./gradlew clean; ./gradlew build; ./gradlew runRandoop -Prebuild
```

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
