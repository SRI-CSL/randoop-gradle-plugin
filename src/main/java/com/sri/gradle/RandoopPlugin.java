package com.sri.gradle;

import com.sri.gradle.tasks.Gentests;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RandoopPlugin implements Plugin<Project> {

  /**
   * The name of the extension for configuring the runtime behavior of the plugin.
   *
   * @see com.sri.gradle.RandoopPluginExtension
   */
  private static final String EXTENSION_NAME = "randoop";

  /**
   * The name of task generating the test files.
   *
   * @see com.sri.gradle.tasks.Gentests
   */
  private static final String GENERATE_TESTS_TASK_NAME = "gentests";

  private static final String RANDOOP_PLUGIN_DESCRIPTION = "Generates tests for a given project using Randoop.";

  @Override public void apply(Project project) {
    RandoopPluginExtension extension = project
        .getExtensions()
        .create(EXTENSION_NAME, RandoopPluginExtension.class, project);

    Gentests task = createTask(project, extension);
    project.getLogger().quiet("Executing " + task.getName());
  }


  private Gentests createTask(Project project, RandoopPluginExtension extension) {
    Gentests generateTestTask = project.getTasks().create(GENERATE_TESTS_TASK_NAME, Gentests.class);
		generateTestTask.setDescription(RANDOOP_PLUGIN_DESCRIPTION);
    generateTestTask.dependsOn("assemble");
    generateTestTask.setPackageName(extension.getPackageNameProvider());
    generateTestTask.getOutdir()
        .set(project.getLayout().getProjectDirectory().dir(extension.getOutdirProvider()));
    generateTestTask.getRandoopJar().set(extension.getRandoopJarProvider());
    generateTestTask.setTimeoutSeconds(extension.getTimeoutSecondsProvider());
    return generateTestTask;
  }
}