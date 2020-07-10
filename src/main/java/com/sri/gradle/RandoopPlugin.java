package com.sri.gradle;

import static com.sri.gradle.Constants.EXTENSION_RANDOOP_PLUGIN_NAME;
import static com.sri.gradle.Constants.GROUP;
import static com.sri.gradle.Constants.RANDOOP_PLUGIN_DESCRIPTION;
import static com.sri.gradle.Constants.TASK_CHECK;
import static com.sri.gradle.Constants.TASK_CHECK_FOR_RANDOOP;
import static com.sri.gradle.Constants.TASK_GENERATE_CLASS_LIST;
import static com.sri.gradle.Constants.TASK_GENERATE_TESTS;

import com.sri.gradle.tasks.CheckForRandoop;
import com.sri.gradle.tasks.GenerateClasslist;
import com.sri.gradle.tasks.Gentests;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;

public class RandoopPlugin implements Plugin<Project> {

  @Override public void apply(Project project) {
    RandoopPluginExtension extension = project.getExtensions().create(
        EXTENSION_RANDOOP_PLUGIN_NAME, RandoopPluginExtension.class, project);

    final CheckForRandoop checkForRandoop = createCheckForRandoop(project);
    final GenerateClasslist generateClasslist = createGenerateClasslist(project);

    final Gentests gentests = createGentestsTask(project, extension);
    gentests.dependsOn(checkForRandoop, generateClasslist);

    makeTaskRunWithCheck(project, gentests);

    project.getLogger().quiet("Executing " + gentests.getName());
  }

  private static void makeTaskRunWithCheck(Project project, Task task) {
    project.getTasksByName(TASK_CHECK, false).forEach(it -> it.dependsOn(task));
  }


  private Gentests createGentestsTask(Project project, RandoopPluginExtension extension) {
    final Gentests generateTestTask = project.getTasks().create(TASK_GENERATE_TESTS, Gentests.class);
    generateTestTask.setGroup(GROUP);
    generateTestTask.setDescription(RANDOOP_PLUGIN_DESCRIPTION);
    generateTestTask.dependsOn("assemble");

    generateTestTask.getRandoopJar().set(extension.getRandoopJar());
    generateTestTask.getJunitOutputDir().set(extension.getJunitOutputDir());
    generateTestTask.getTimeoutSeconds().set(extension.getTimeoutSeconds());
    generateTestTask.getStopOnErrorTest().set(extension.getStopOnErrorTest());
    generateTestTask.getFlakyTestBehavior().set(extension.getFlakyTestBehavior());
    generateTestTask.getNoErrorRevealingTests().set(extension.getNoErrorRevealingTests());
    generateTestTask.getJunitReflectionAllowed().set(extension.getJunitReflectionAllowed());
    generateTestTask.getUsethreads().set(extension.getUsethreads());
    generateTestTask.getOutputLimit().set(extension.getOutputLimit());

    setClasslistIfAvailable(generateTestTask);

    return generateTestTask;
  }

  private static void setClasslistIfAvailable(Gentests task){
    final Directory resourcesDir = task.getProject().getLayout()
        .getProjectDirectory()
        .dir("src/test/resources");
    final Path resourcesDirPath = resourcesDir.getAsFile().toPath();
    if (!Files.exists(resourcesDirPath)) {
      task.getProject().getLogger().quiet("Unable to find resources directory");
      return;
    }

    final RegularFile classlistFile = resourcesDir.file("classlist.txt");
    final File cf = classlistFile.getAsFile();
    if (!Files.exists(cf.toPath())) return;

    task.setClassListFile(cf);
  }

  private CheckForRandoop createCheckForRandoop(Project project){
    final CheckForRandoop checkForRandoop = project.getTasks().create(
        TASK_CHECK_FOR_RANDOOP, CheckForRandoop.class);
    checkForRandoop.setGroup(GROUP);
    return checkForRandoop;
  }

  private GenerateClasslist createGenerateClasslist(Project project){
    final GenerateClasslist generateClasslist = project.getTasks().create(
        TASK_GENERATE_CLASS_LIST, GenerateClasslist.class);
    generateClasslist.setGroup(GROUP);
    return generateClasslist;
  }
}