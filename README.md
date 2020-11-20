# Randoop Gradle Plugin

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
