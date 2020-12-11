# Randoop Gradle Plugin

[![License](https://img.shields.io/badge/license-apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

(experimental) randoop gradle plug-in

This Gradle plug-in creates a task named `runRandoop` to run [Randoop](https://randoop.github.io/randoop/) on Java projects.

## Configuration

To use this plug-in you must apply the Randoop plug-in to the root project’s `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
	id 'com.sri.gradle.randoop' version '0.0.1-SNAPSHOT'
}
```

Then, you must specify you must specify a few configuration parameters in your `runRandoop` configuration. 
For example, the location where the plug-in can find the Randoop tool, the Randoop output directory,
the name of the package for the generated JUnit files, etc.

In the following example, Randoop is in the project's `libs` directory, the output directory is
the project's `src/test/java` directory, and the JUnit package name is `com.foo`:

```groovy
runRandoop {
    randoopJar = file("libs/randoop.jar")
    junitOutputDir = file("${projectDir}/src/test/java")
    ....
    junitPackageName = 'com.foo'
}
```

You can find a complete example of this configuration below:

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
    implementation 'com.google.code.gson:gson:2.8.6'
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

On this `runRandoop` extension object, you can change the following settings:

* `randoopJar`=_file_. The location where the plugin can find Randoop.
* `junitOutputDir`=_file_. Randoop output directory
* `timeoutSeconds`=_int_. Maximum number of seconds to spend generating tests. Zero means no limit. If nonzero, Randoop is nondeterministic: it may generate different test suites on different runs.
* `stopOnErrorTest`=_boolean_. Stop generation as soon as one error-revealing test has been generated. (default false)
* `flakyTestBehavior`=_enum_. What to do if Randoop generates a flaky test. A flaky test is one that behaves differently on different executions. Options include: (1) halt: Randoop halts with a diagnostic message; (2) discard: Discard the flaky test; (3) output: Output the flaky test, but with flaky assertions commented out.
* `noErrorRevealingTests`=_boolean_. Whether to output error-revealing tests.
* `junitReflectionAllowed`=_boolean_. Whether to use JUnit's standard reflective mechanisms for invoking tests.
* `usethreads`=_boolean_. If true, Randoop executes each test in a separate thread and kills tests that take too long to finish.
* `outputLimit`=_int_. The number of error-revealing and regression tests reaches the output limit.
* `junitPackageName`=_string_. Name of the package for the generated JUnit files. When the package is the same as the package of a class under test, then package visibility rules are used to determine whether to include the class or class members in a test.

## Using a locally-built plug-in

You can build the plug-in locally rather than downloading it from Maven Central.

To build the plug-in from source, run the `./gradlew build` command.

If you want to use a locally-built version of the plug-in, you must add the following configuration to
the `settings.gradle` file of your Gradle project:

```
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

Then, you can publish the plug-in to your local Maven repository simply by running the `./gradlew publishToMavenLocal` command.
Make sure you run both the `clean` and the `build` tasks before running this command:

```sh
› ./gradlew clean
› ./gradlew build
› ./gradlew publishToMavenLocal
```

After that, you can use any of the Gradle tasks offered by the Randoop plug-in.

## Randoop Tasks

The plug-in offers the following Gradle tasks:

- `checkForRandoop` - Checks if Randoop is in the CLASSPATH.
- `cleanupRandoopOutput` - Deletes all Randoop-generated tests; including the `classlist.txt` file.
- `generateClassListFile` - Generates a file that lists classes that Randoop will explore to generate tests.
- `generateTests` - Generates tests for a given project using Randoop (Main task).
- `randoopEvidence` - Produces an evidence artifact containing the specific details of the Randoop execution.

As shown above, the main task of this plug-in is the `generateTests` task. However, any task can be executed independently.

**Additional build properties:**

-   `-Pevidence.only` - Tells the plug-in to only generate the `randoop-evidence.json` file; i.e., test generation won't be triggered. Assumes the build task has already generated tests using Randoop and thus there is a `randoop-summary.txt` file.

If the above property is not provided, then the plug-in will sequentially execute `cleanupRandoopOutput`, `checkForRandoop`, `generateClassListFile`, `generateTests`, and `randoopEvidence` tasks.

## An example: Applying the Randoop plug-in to a simple Java project

A simple example of how to use this plug-in on a basic Java project can be found at this project's
`consumer` sub-directory. Here are the steps for using the plug-in:

1. Make sure you publish the plug-in to Maven local first (See steps above)

If `build` tasks throws an error, try running the used commands with the `--stacktrace` or `--debug` options. E.g.,

```sh
› ./gradlew clean
› ./gradlew build --stacktrace
› ./gradlew publishToMavenLocal
```

2. Run either `randoopEvidence` or `generateTests` tasks

If you want to see the plug-in in action, you can just run the `generateTests` task. That should be enough.
This task will generate test Java files using Randoop. However, if you want to generate an evidence artifact that provides a summary of a Randoop execution,
which will also run the `generateTests` task, then you can run the `randoopEvidence` task.

```sh
› cd consumer
› ./gradlew randoopEvidence
```

On any execution, this task will execute all the Gradle tasks offered by this plugin unless the `-Pevidence.only` property is appended to this command.
This property tells the plug-in to just re-generate the evidence artifact.

```sh
› ./gradlew randoopEvidence -Pevidence.only
```

## Results

Besides generating test Java files, the tasks `generateTests` and `randoopEvidence` generates two files:
`randoop-summary.txt` and `randoop-evidence.json`. The first (see next) provides a summary of a Randoop execution.

```text
Randoop for Java version "4.2.3, local changes, branch master, commit 6fb16d1, 2020-03-31".
Will explore 2 classes
PUBLIC MEMBERS=6
Explorer = ForwardGenerator(steps: 0, null steps: 0, num_sequences_generated: 0;
    allSequences: 0, regresson seqs: 0, error seqs: 0=0=0, invalid seqs: 0, subsumed_sequences: 0, num_failed_output_test: 0;
    runtimePrimitivesSeen:38)

Progress update: steps=1, test inputs generated=0, failing inputs=0      (Wed Dec 09 12:16:00 PST 2020     38MB used)
Progress update: steps=1000, test inputs generated=544, failing inputs=0      (Wed Dec 09 12:16:14 PST 2020     168MB used)
Progress update: steps=1989, test inputs generated=1077, failing inputs=0      (Wed Dec 09 12:16:30 PST 2020     49MB used)
Normal method executions: 162575
Exceptional method executions: 1

Average method execution time (normal termination):      0.104
Average method execution time (exceptional termination): 0.275
Approximate memory usage 49MB
Explorer = ForwardGenerator(steps: 1989, null steps: 912, num_sequences_generated: 1077;
    allSequences: 1077, regresson seqs: 1076, error seqs: 0=0=0, invalid seqs: 0, subsumed_sequences: 0, num_failed_output_test: 1;
    runtimePrimitivesSeen:38)

About to look for failing assertions in 548 regression sequences.

Regression test output:
Regression test count: 548
Writing regression JUnit tests...
Created file /Users/<user-id>/dev/descert/randoop-gradle-plugin/consumer/src/test/java/com/foo/RegressionTest0.java
Created file /Users/<user-id>/dev/descert/randoop-gradle-plugin/consumer/src/test/java/com/foo/RegressionTest1.java
Created file /Users/<user-id>/dev/descert/randoop-gradle-plug-in/consumer/src/test/java/com/foo/RegressionTestDriver.java
Wrote regression JUnit tests.
About to look for flaky methods.

Invalid tests generated: 0
```

The second file is the evidence artifact, which is built using information from the `randoop-summary.txt` file.

```json
{
  "DETAILS": {
    "MEMORY_USAGE": "256MB",
    "INVALID_TESTS_GENERATED": "0",
    "COMMIT": "6fb16d1",
    "RANDOOP_VERSION": "4.2.3",
    "AVG_NORMAL_TERMINATION_TIME": "0.0827",
    "REGRESSION_TEST_COUNT": "658",
    "GENERATED_TEST_COUNT": "3",
    "BRANCH": "master",
    "PUBLIC_MEMBERS": "6",
    "EXPLORED_CLASSES": "2",
    "DATE": "2020-03-31",
    "AVG_EXCEPTIONAL_TERMINATION_TIME": "0.210",
    "NORMAL_EXECUTIONS": "210283",
    "ACTIVITY": "TEST_GENERATION",
    "CHANGES": "local",
    "AGENT": "RANDOOP"
  }
}
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
