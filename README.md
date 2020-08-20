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

## Task

* `gentests` - runs Randoop'.

## Slides

[Slides](https://docs.google.com/presentation/d/1YtCBqJ29rMNYvAiiHstXdWf9F68M34Hp2iufOAfETHw/edit?usp=sharing).
