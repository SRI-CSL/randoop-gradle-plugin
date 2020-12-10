package com.sri.gradle.randoop;

import static com.sri.gradle.randoop.Constants.CHECK_FOR_RANDOOP_TASK_NAME;
import static com.sri.gradle.randoop.Constants.CLEANUP_RANDOOP_TASK_NAME;
import static com.sri.gradle.randoop.Constants.GENERATE_CLASS_LIST_TASK_DESCRIPTION;
import static com.sri.gradle.randoop.Constants.GENERATE_CLASS_LIST_TASK_NAME;
import static com.sri.gradle.randoop.Constants.GENERATE_TESTS_TASK_NAME;
import static com.sri.gradle.randoop.Constants.GROUP;
import static com.sri.gradle.randoop.Constants.RANDOOP_DETAILS_TASK_DESCRIPTION;
import static com.sri.gradle.randoop.Constants.RANDOOP_DETAILS_TASK_NAME;
import static com.sri.gradle.randoop.Constants.RANDOOP_PLUGIN_DESCRIPTION;
import static com.sri.gradle.randoop.Constants.RANDOOP_PLUGIN_EXTENSION;

import com.sri.gradle.randoop.extensions.RandoopJavaCompileExtension;
import com.sri.gradle.randoop.extensions.RandoopPluginExtension;
import com.sri.gradle.randoop.tasks.CheckForRandoop;
import com.sri.gradle.randoop.tasks.CleanupRandoopOutput;
import com.sri.gradle.randoop.tasks.GenerateClasslist;
import com.sri.gradle.randoop.tasks.Gentests;
import com.sri.gradle.randoop.tasks.JavaCompileMutator;
import com.sri.gradle.randoop.tasks.RandoopEvidence;
import com.sri.gradle.randoop.utils.JavaProjectHelper;
import java.util.Optional;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.compile.JavaCompile;

@SuppressWarnings("unused")
public class RandoopPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getPlugins().apply(JavaPlugin.class);

    RandoopPluginExtension extension =
        project
            .getExtensions()
            .create(RANDOOP_PLUGIN_EXTENSION, RandoopPluginExtension.class, project);

    // Create plugin's main task: Gentests
    final Gentests gentests = createGentestsTask(project, extension);
    gentests.dependsOn(addAndGetRandoopTaskDependencies(project, extension));

    // Compiles
    final JavaCompile randoopJavaCompile = configureJavaCompile(project, gentests);
    RandoopEvidence getRandoopEvidence = createRandoopDetailsTask(project, extension);
    getRandoopEvidence.getOutputs().upToDateWhen(spec -> false);
    getRandoopEvidence.dependsOn(randoopJavaCompile);

    project.getLogger().quiet("Applied Randoop Gradle Plugin");
  }

  private Task addAndGetRandoopTaskDependencies(Project project, RandoopPluginExtension extension) {
    final JavaProjectHelper projectHelper = new JavaProjectHelper(project);
    // 1. Clean up any previously generated tests (if any).
    if (!project.hasProperty(Constants.EVIDENCE_ONLY)){
      final CleanupRandoopOutput cleanupRandoopOutput = createCleanupRandoop(project, extension);
      cleanupRandoopOutput.getOutputs().upToDateWhen(spec -> false);
    }

    Optional<JavaCompile> javaCompile =
        projectHelper.findTask(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);

    if (!javaCompile.isPresent()) {
      throw new GradleException("JavaPlugin is available in this project");
    }

    final JavaCompile aJavaCompile = javaCompile.get();
    if (!project.hasProperty(Constants.EVIDENCE_ONLY)){
      aJavaCompile.dependsOn(CLEANUP_RANDOOP_TASK_NAME);
    }

    // 2. Build project and then check if Randoop is in CLASSPATH
    // Checks if Randoop is in your CLASSPATH
    final CheckForRandoop checkForRandoop = createCheckForRandoop(project);
    checkForRandoop.dependsOn(JavaBasePlugin.BUILD_TASK_NAME);

    // 3. If it is in the CLASSPATH, then Generate-class-list
    // Generates a list of classes that Randoop will use as input in order to generate unit tests.
    final GenerateClasslist generateClasslist = createGenerateClasslist(project);
    generateClasslist.dependsOn(checkForRandoop);

    return generateClasslist;
  }

  private JavaCompile configureJavaCompile(Project project, Gentests gentests) {
    final JavaProjectHelper projectHelper = new JavaProjectHelper(project);

    Optional<JavaCompile> javaCompile =
        projectHelper.findTask(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, JavaCompile.class);

    if (!javaCompile.isPresent()) {
      throw new GradleException("JavaPlugin is available in this project");
    }

    final JavaCompile aJavaCompile = javaCompile.get();
    // Creates a new Randoop JavaCompile extension, which creates a
    // Constants.COMPILE_TEST_DRIVER task.
    final RandoopJavaCompileExtension driverExtension =
        aJavaCompile
            .getExtensions()
            .create("driverExtension", RandoopJavaCompileExtension.class, project);

    driverExtension.setCompileTestDriverSeparately(true);

    // Gets the newly created Randoop task
    final JavaCompile testDriverJavaCompile =
        projectHelper.task(Constants.COMPILE_TEST_DRIVER, JavaCompile.class);
    testDriverJavaCompile.dependsOn(gentests);
    final JavaCompileMutator compileMutator =
        new JavaCompileMutator(project, aJavaCompile.getClasspath());

    // Don't convert to lambda. More info:
    // https://github.com/gradle/gradle/issues/5510#issuecomment-416860213
    gentests.doLast(
        new Action<Task>() {
          @Override
          public void execute(Task ignored) {
            compileMutator.mutateJavaCompileTask(testDriverJavaCompile);
          }
        });

    return testDriverJavaCompile;
  }

  private RandoopEvidence createRandoopDetailsTask(
      Project project, RandoopPluginExtension extension) {
    final RandoopEvidence randoopEvidence =
        project.getTasks().create(RANDOOP_DETAILS_TASK_NAME, RandoopEvidence.class);
    randoopEvidence.setGroup(GROUP);
    randoopEvidence.setDescription(RANDOOP_DETAILS_TASK_DESCRIPTION);

    randoopEvidence.getJunitOutputDir().set(extension.getJunitOutputDir());

    return randoopEvidence;
  }

  private Gentests createGentestsTask(Project project, RandoopPluginExtension extension) {
    final Gentests generateTestTask =
        project.getTasks().create(GENERATE_TESTS_TASK_NAME, Gentests.class);
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

  private CleanupRandoopOutput createCleanupRandoop(
      Project project, RandoopPluginExtension extension) {
    final CleanupRandoopOutput cleanupRandoopOutput =
        project.getTasks().create(CLEANUP_RANDOOP_TASK_NAME, CleanupRandoopOutput.class);
    cleanupRandoopOutput.setGroup(GROUP);
    cleanupRandoopOutput.setDescription(Constants.CLEANUP_RANDOOP_TASK_DESCRIPTION);
    cleanupRandoopOutput.getJunitOutputDir().set(extension.getJunitOutputDir());
    return cleanupRandoopOutput;
  }

  private CheckForRandoop createCheckForRandoop(Project project) {
    final CheckForRandoop checkForRandoop =
        project.getTasks().create(CHECK_FOR_RANDOOP_TASK_NAME, CheckForRandoop.class);
    checkForRandoop.setGroup(GROUP);
    checkForRandoop.setDescription(Constants.CHECK_FOR_RANDOOP_TASK_DESCRIPTION);
    return checkForRandoop;
  }

  private GenerateClasslist createGenerateClasslist(Project project) {
    final GenerateClasslist generateClasslist =
        project.getTasks().create(GENERATE_CLASS_LIST_TASK_NAME, GenerateClasslist.class);
    generateClasslist.setGroup(GROUP);
    generateClasslist.setDescription(GENERATE_CLASS_LIST_TASK_DESCRIPTION);
    return generateClasslist;
  }
}
