package com.sri.gradle.randoop;

import static com.sri.gradle.randoop.Constants.GROUP;
import static com.sri.gradle.randoop.Constants.RANDOOP_PLUGIN_DESCRIPTION;
import static com.sri.gradle.randoop.Constants.RANDOOP_PLUGIN_EXTENSION;
import static com.sri.gradle.randoop.Constants.TASK_CHECK_FOR_RANDOOP;
import static com.sri.gradle.randoop.Constants.TASK_GENERATE_CLASS_LIST;
import static com.sri.gradle.randoop.Constants.TASK_GENERATE_TESTS;

import com.sri.gradle.randoop.tasks.CheckForRandoop;
import com.sri.gradle.randoop.tasks.GenerateClasslist;
import com.sri.gradle.randoop.tasks.Gentests;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class RandoopPlugin implements Plugin<Project> {

  @Override public void apply(Project project) {
    project.getPlugins().apply(JavaPlugin.class);

    RandoopPluginExtension extension = project.getExtensions().create(
        RANDOOP_PLUGIN_EXTENSION, RandoopPluginExtension.class, project);

    final CheckForRandoop checkForRandoop = createCheckForRandoop(project);
    final GenerateClasslist generateClasslist = createGenerateClasslist(project);

    final Gentests gentests = createGentestsTask(project, extension);
    gentests.dependsOn("build", checkForRandoop, generateClasslist);

    project.getLogger().quiet("Executing " + gentests.getName());
  }

  private Gentests createGentestsTask(Project project, RandoopPluginExtension extension) {
    final Gentests generateTestTask = project.getTasks().create(TASK_GENERATE_TESTS, Gentests.class);
    generateTestTask.setGroup(GROUP);
    generateTestTask.setDescription(RANDOOP_PLUGIN_DESCRIPTION);

    generateTestTask.getRandoopJar().set(extension.getRandoopJar());
    generateTestTask.getJunitOutputDir().set(extension.getJunitOutputDir());
    generateTestTask.getTimeoutSeconds().set(extension.getTimeoutSeconds());
    generateTestTask.getStopOnErrorTest().set(extension.getStopOnErrorTest());
    generateTestTask.getFlakyTestBehavior().set(extension.getFlakyTestBehavior());
    generateTestTask.getNoErrorRevealingTests().set(extension.getNoErrorRevealingTests());
    generateTestTask.getJunitReflectionAllowed().set(extension.getJunitReflectionAllowed());
    generateTestTask.getUsethreads().set(extension.getUsethreads());
    generateTestTask.getOutputLimit().set(extension.getOutputLimit());
    generateTestTask.getJunitPackageName().set(extension.getJunitPackageName());

    return generateTestTask;
  }

  private CheckForRandoop createCheckForRandoop(Project project){
    final CheckForRandoop checkForRandoop = project.getTasks().create(
        TASK_CHECK_FOR_RANDOOP, CheckForRandoop.class);
    checkForRandoop.setGroup(GROUP);
    checkForRandoop.setDescription(Constants.TASK_CHECK_FOR_RANDOOP_DESCRIPTION);
    return checkForRandoop;
  }

  private GenerateClasslist createGenerateClasslist(Project project){
    final GenerateClasslist generateClasslist = project.getTasks().create(
        TASK_GENERATE_CLASS_LIST, GenerateClasslist.class);
    generateClasslist.setGroup(GROUP);
    generateClasslist.setDescription("");
    return generateClasslist;
  }
}