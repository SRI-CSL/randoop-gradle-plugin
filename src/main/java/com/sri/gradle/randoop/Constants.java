package com.sri.gradle.randoop;

import java.io.File;

public class Constants {
  // plugin-related constants
  public static final String GROUP = "Randoop";
  public static final String RANDOOP_PLUGIN_DESCRIPTION = "Generates tests for a given project using Randoop.";
  public static final String RANDOOP_PLUGIN_EXTENSION = "runRandoop";
  public static final String PROJECT_LIBS_DIR = "libs";

  // task-related constants
  public static final String TASK_GENERATE_TESTS = "generateTests";
  public static final String TASK_GENERATE_TESTS_DESCRIPTION = "Automatically generates unit tests for Java classes.";
  public static final String TASK_GENERATE_CLASS_LIST = "generateClassListFile";
  public static final String TASK_GENERATE_CLASS_LIST_DESCRIPTION = "Generates a file that lists classes to test. All of their methods are methods under test";
  public static final String TASK_CHECK_FOR_RANDOOP = "checkForRandoop";
  public static final String TASK_CHECK_FOR_RANDOOP_DESCRIPTION = "Checks if Randoop is installed on one's computer.";

  // tool-related constants
  public static final String RANDOOP_MAIN_CLASS = "randoop.main.Main";
  public static final String RANDOOP_JAR = "randoop.jar";
  public static final String PROJECT_MAIN_SRC_DIR = "src/main/java";
  public static final String PROJECT_MAIN_CLASS_DIR = "classes/java/main";
  public static final String PROJECT_TEST_CLASS_DIR = "classes/java/test";
  public static final String PROJECT_TEST_RESOURCES_DIR = "src/test/resources";
  public static final String CLASS_LIST_FILE = "classlist.txt";
  public static final String BAD_RANDOOP_ERROR = "Unable to run Randoop. Are you sure randoop.jar is in your path?";
  public static final String BAD_CLASS_LIST_ERROR = "Unable to find src directory. Are you sure src exists?";

  // general constants
  public static final String NEW_LINE = System.getProperty("line.separator");
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");
  public static final String PATH_SEPARATOR = System.getProperty("path.separator");
  public static final File USER_WORKING_DIR = new File(System.getProperty("user.dir"));
}
