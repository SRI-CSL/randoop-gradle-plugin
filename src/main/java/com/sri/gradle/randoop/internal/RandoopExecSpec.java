package com.sri.gradle.randoop.internal;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.utils.Classfinder;
import com.sri.gradle.randoop.utils.JavaProjectHelper;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.process.JavaForkOptions;

public class RandoopExecSpec {
  private final List<Action<JavaForkOptions>> configureFork = new ArrayList<>();
  private FileCollection classpath;
  private String main;
  private Object[] args = new Object[0];
  private File workingDirectory = Constants.USER_WORKING_DIR;

  private static List<Class<?>> findAllCompiledClasses(final Project project, String packageName) {
    final JavaProjectHelper projectHelper = new JavaProjectHelper(project);
    final List<URL> compiledClasses = new LinkedList<>();
    project
        .files(projectHelper.getBuildMainDir())
        .forEach(
            e -> {
              try {
                compiledClasses.add(e.toURI().toURL());
              } catch (MalformedURLException ex) {
                throw new RandoopExecException("Could not add artifact!", ex);
              }
            });

    final URLClassLoader classLoader = new URLClassLoader(convert(compiledClasses));
    final List<Class<?>> allClassesOfPackage = Classfinder.findClasses(packageName, classLoader);
    return ImmutableList.copyOf(allClassesOfPackage);
  }

  private static URL[] convert(final Collection<URL> urls) {
    return urls.toArray(new URL[0]);
  }

  public void args(Object... args) {
    this.args = Stream.concat(Arrays.stream(this.args), Arrays.stream(args)).toArray(Object[]::new);
  }

  public void forkOptions(Action<JavaForkOptions> configureFork) {
    this.configureFork.add(configureFork);
  }

  public Object[] getArgs() {
    return args;
  }

  public FileCollection getClasspath() {
    return classpath;
  }

  public List<Action<JavaForkOptions>> getConfigureFork() {
    return configureFork;
  }

  public String getMain() {
    return main;
  }

  public File getWorkingDir() {
    return workingDirectory;
  }

  public void setArgs(Object... args) {
    if (args == null) {
      this.args = new Object[0];
    } else {
      this.args = args;
    }
  }

  public void setClasspath(FileCollection classpath) {
    this.classpath = classpath;
  }

  public void setClassListFile(File projectDir, File classListFile) {
    if (classListFile != null) {
      setClassListFile(projectDir.toPath(), classListFile.toPath());
    }
  }

  public void setClassListFile(Path projectDir, Path classListFile) {
    Objects.requireNonNull(projectDir);
    Objects.requireNonNull(classListFile);
    final Path resolved = projectDir.resolve(classListFile);
    args(String.format("--classlist=%s", resolved));
  }

  public void setCommand(String command) {
    if (!Strings.isNullOrEmpty(command)) {
      args(command);
    }
  }

  public void setDebugChecks(boolean flag) {
    args(String.format("--debug-checks=%s", flag));
  }

  public void setForkOptions() {
    final ForkOptions options = new ForkOptions();
    forkOptions(
        fork -> {
          fork.setWorkingDir(getWorkingDir());
          fork.setJvmArgs(options.getJvmArgs());
          fork.setMinHeapSize(options.getMemoryInitialSize());
          fork.setMaxHeapSize(options.getMemoryMaximumSize());
          fork.setDefaultCharacterEncoding(StandardCharsets.UTF_8.name());
        });
  }

  public void setFlakyTestBehavior(String enumStr) {
    args(String.format("--flaky-test-behavior=%s", enumStr));
  }

  public void setJUnitOutputDir(File projectDir, File junitOutputDir) {
    setJUnitOutputDir(projectDir.toPath(), junitOutputDir.toPath());
  }

  public void setJUnitOutputDir(Path projectDir, Path junitOutputDir) {
    final Path resolved = projectDir.resolve(junitOutputDir);
    args(String.format("--junit-output-dir=%s", resolved));
  }

  public void setJUnitPackageName(String packageName) {
    args(String.format("--junit-package-name=%s", packageName));
  }

  public void setJUnitReflectionAllowed(boolean flag) {
    args(String.format("--junit-reflection-allowed=%s", flag));
  }

  public void setMain(String main) {
    this.main = main;
  }

  public void setNoErrorRevealingTests(boolean flag) {
    args(String.format("--no-error-revealing-tests=%s", flag));
  }

  public void setOnlyTestPublicMembers(boolean value) {
    args("--only-test-public-members=" + value);
  }

  public void setOutputLimit(int limit) {
    args(String.format("--output-limit=%d", limit));
  }

  public void setOutOfMemoryErrorOption(OutOfMemoryError option) {
    args("--oom-exception=" + option);
  }

  public void setRegressionTestBasename(String name) {
    if (name != null && !name.isEmpty()) {
      args(String.format("--regression-test-basename=%s", name));
    }
  }

  public void setSilentlyIgnoreBadClassNames() {
    args("--silently-ignore-bad-class-names=true");
  }

  public void setStopOnErrorTest(boolean flag) {
    args(String.format("--stop-on-error-test=%s", flag));
  }

  public void setTimelimit(int timeLimitSeconds) {
    args("--time-limit=" + timeLimitSeconds);
  }

  public void setTestClasses(Project project, String... packs) {
    final Set<Class<?>> uniqueClasses = new HashSet<>();
    for (String each : packs) {
      uniqueClasses.addAll(findAllCompiledClasses(project, each));
    }

    // Add project classes
    for (Class<?> currentClass : uniqueClasses) {
      args("--testclass=" + currentClass.getName());
    }
  }

  public void setUseThreads() {
    args("--usethreads");
  }

  public void setWorkingDir(Path workingDir) {
    Objects.requireNonNull(workingDir);
    setWorkingDir(workingDir.toFile());
  }

  public void setWorkingDir(File workingDir) {
    this.workingDirectory = workingDir;
  }
}
