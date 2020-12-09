package com.sri.gradle.randoop;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class Constants {

  // plugin-related constants
  public static final String GROUP = "Randoop";
  public static final String RANDOOP_PLUGIN_DESCRIPTION =
      "Generates tests for a given project using Randoop.";
  public static final String RANDOOP_PLUGIN_EXTENSION = "runRandoop";
  public static final String PROJECT_LIBS_DIR = "libs";
  public static final String COMPILE_TEST_DRIVER = "compileRandoopTests";

  // task-related constants
  public static final String GENERATE_TESTS_TASK_NAME = "generateTests";
  public static final String GENERATE_TESTS_TASK_DESCRIPTION =
      "Automatically generates unit tests for Java classes.";
  public static final String GENERATE_CLASS_LIST_TASK_NAME = "generateClassListFile";
  public static final String GENERATE_CLASS_LIST_TASK_DESCRIPTION =
      "Generates a file that lists classes that Randoop will explore to generate tests.";
  public static final String CHECK_FOR_RANDOOP_TASK_NAME = "checkForRandoop";
  public static final String CHECK_FOR_RANDOOP_TASK_DESCRIPTION =
      "Checks if Randoop is in CLASSPATH.";
  public static final String CLEANUP_RANDOOP_TASK_NAME = "cleanupRandoopOutput";
  public static final String CLEANUP_RANDOOP_TASK_DESCRIPTION =
      "Deletes all Randoop-generated tests.";
  public static final String RANDOOP_DETAILS_TASK_NAME = "randoopEvidence";
  public static final String RANDOOP_DETAILS_TASK_DESCRIPTION =
      "Produces an evidence artifact containing the specific details of the Randoop execution.";
  public static final String EVIDENCE_ONLY = "evidence.only";

  public static final String RANDOOP_SUMMARY_FILE_NAME = "randoop-summary.txt";
  public static final String RANDOOP_DETAILS_FILE_NAME = "randoop-evidence.json";
  public static final Charset ENCODING = StandardCharsets.UTF_8;

  // Regular expression which matches expected names of JUnit test classes.
  // thx to https://github.com/sevntu-checkstyle/sevntu.checkstyle
  public static final Pattern EXPECTED_RANDOOP_TEST_NAME_REGEX =
      Pattern.compile(
          "RegressionTest\\d*|RegressionTests\\d*|RegressionTest.+|RegressionTests.+|ErrorTest\\d*|ErrorTests\\d*|ErrorTest.+|ErrorTests.+");

  // tool-related constants
  public static final String RANDOOP_MAIN_CLASS = "randoop.main.Main";
  public static final String RANDOOP_JAR = "randoop.jar";
  public static final String PROJECT_MAIN_SRC_DIR = "src/main/java";
  public static final String PROJECT_TEST_SRC_DIR = "src/test/java";
  public static final String PROJECT_MAIN_CLASS_DIR = "classes/java/main";
  public static final String PROJECT_TEST_CLASS_DIR = "classes/java/test";
  public static final String PROJECT_TEST_RESOURCES_DIR = "src/test/resources";
  public static final String CLASS_LIST_FILE = "classlist.txt";
  public static final String BAD_RANDOOP_ERROR =
      "Unable to run Randoop. Are you sure randoop.jar is in your path?";
  public static final String BAD_CLASS_LIST_ERROR =
      "Unable to find src directory. Are you sure src exists?";

  // general constants
  public static final String NEW_LINE = System.getProperty("line.separator");
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");
  public static final String PATH_SEPARATOR = System.getProperty("path.separator");
  public static final File USER_WORKING_DIR = new File(System.getProperty("user.dir"));
  public static final OutputStream QUIET_OUTPUT = new OutputStream() {
    @Override public void write(int b) throws IOException {
      // nothing;
    }
  };
}
