package com.sri.gradle.internal;

import static java.util.Arrays.stream;

import com.sri.gradle.Constants;
import com.sri.gradle.utils.Classfinder;
import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.Immutable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;

public class RandoopCommand {

  private static final String BAD_RANDOOP_ERROR = "Unable to run Randoop. Are you sure randoop.jar is in your path?";

  private final Command.Builder builder;

  private final List<URL> classpathUrls;
  private Object[] args = new Object[0];
  private final String taskName;

  public RandoopCommand(String taskName) {
    this.builder = Command.create()
        .arguments("java")
        .arguments("-ea")
        .permitNonZeroExitStatus();

    this.taskName = taskName;
    this.classpathUrls = new LinkedList<>();

  }

  public Object[] getArgs() {
    return args;
  }

  private void args(Object... args) {
    this.args = Stream.concat(
        stream(this.args), stream(args)).toArray(Object[]::new);
  }

  public RandoopCommand setClasspath(Project project, RegularFileProperty jarfile, DirectoryProperty outdir){
    final File randoopJar = jarfile.getAsFile().get();
    final File projectOut = outdir.getAsFile().get();
    return setGeneratorJar(randoopJar)
        .setJunitOutputDir(projectOut)
        .resolveSourceDir(project)
        .setProjectDependencies(project);
  }

  public RandoopCommand silentlyIgnoreBadClassNames(){
    args("--silently-ignore-bad-class-names=true");
    return this;
  }

  public RandoopCommand setJUnitReflectionAllowed(boolean flag){
    args(String.format("--junit-reflection-allowed=%s", flag));
    return this;
  }

  public RandoopCommand setOnlyTestPublicMembers(){
    args("--only-test-public-members=true");
    return this;
  }

  public RandoopCommand setOutputLimit(int limit){
    args(String.format("--output-limit=%d", limit));
    return this;
  }

  public RandoopCommand setDiscardOutOfMemoryErrors(){
    args("--oom-exception=INVALID");
    return this;
  }

  public RandoopCommand setNoErrorRevealingTests(boolean flag){
    args(String.format("--no-error-revealing-tests=%s", flag));
    return this;
  }

  public RandoopCommand setIncludeOutOfMemoryErrorsInErrorRevealingTests(){
    args("--oom-exception=ERROR");
    return this;
  }

  public RandoopCommand setIncludeOutOfMemoryErrorsInRegressionTests(){
    args("--oom-exception=EXPECTED");
    return this;
  }

  public RandoopCommand setRegressionTestBasename(String name){
    Optional.ofNullable(name).ifPresent(n -> args(String.format("--regression-test-basename=%s", n)));
    return this;
  }

  public RandoopCommand setProjectDependencies(Project project){
    classpathUrls.addAll(loadProjectDependencies(project));
    classpathUrls.add(loadPluginJarWithRandoop());
    return this;
  }

  public RandoopCommand setGeneratorJar(File jarfile){
    classpathUrls.add(loadProjectClasses(jarfile.getAbsolutePath()));
    return this;
  }

  public RandoopCommand setJunitOutputDir(File outputDir){
    String outDir = outputDir.getAbsolutePath();
    classpathUrls.add(loadProjectClasses(outDir));

    args(String.format("--junit-output-dir=%s", outDir));

    return this;
  }

  public RandoopCommand resolveSourceDir(Project project){
    final File sourceDir = project.getLayout().getProjectDirectory().dir(Constants.PATH_TO_SRC_DIR).getAsFile();
    classpathUrls.add(loadProjectClasses(sourceDir.getAbsolutePath()));
    return this;
  }

  public RandoopCommand setTimelimit(int timeLimitSeconds){
    args("--time-limit=" + timeLimitSeconds);
    return this;
  }

  public RandoopCommand setJUnitPackageName(String packageName){
    args(String.format("--junit-package-name=%s", packageName));
    return this;
  }

  public RandoopCommand setUseThreads(){
    args("--usethreads");
    return this;
  }

  public RandoopCommand setStopOnErrorTest(boolean flag){
    args(String.format("--stop-on-error-test=%s", flag));
    return this;
  }

  public RandoopCommand setDebugChecks(boolean flag){
    args(String.format("--debug-checks=%s", flag));
    return this;
  }

  public RandoopCommand setFlakyTestBehavior(String enumStr){
    args(String.format("--flaky-test-behavior=%s", enumStr));
    return this;
  }

  public RandoopCommand setTestClasses(Project project, String... packs){
    final Set<Class<?>> uniqueClasses = new HashSet<>();
    for (String each : packs){
      uniqueClasses.addAll(findAllCompiledClasses(project, each));
    }

    // Add project classes
    for (Class<?> currentClass : uniqueClasses) {
      args("--testclass=" + currentClass.getName());
    }

    return this;
  }

  public RandoopCommand setClassListFile(File classListFile){
    Optional.ofNullable(classListFile)
        .ifPresent(f -> args(String.format("--classlist=%s", f.getAbsolutePath())));

    return this;
  }

  public List<String> execute() throws RandoopCommandException {

    try {
      final String classPath = classpathUrls.stream()
          .map(URL::toString)
          .collect(Collectors.joining(File.pathSeparator));

      builder
          .arguments("-classpath", classPath)
          .arguments(Constants.MAIN)
          .arguments(taskName)
          .arguments(getArgs());

      return builder.execute();
    } catch (Exception e){
      throw new RandoopCommandException(BAD_RANDOOP_ERROR, e);
    }
  }


  static List<Class<?>> findAllCompiledClasses(final Project project, String packageName){
    final List<URL> compiledClasses = new LinkedList<>();
    project.files(project.getLayout().getBuildDirectory().dir(Constants.PATH_TO_COMPILED_CLASSES))
        .forEach(e -> {
          try {
            compiledClasses.add(e.toURI().toURL());
          } catch (MalformedURLException ex) {
            throw new RandoopCommandException("Could not add artifact!", ex);
          }
        });

    final URLClassLoader classLoader = new URLClassLoader(convert(compiledClasses));
    final List<Class<?>> allClassesOfPackage = Classfinder.findClasses(packageName, classLoader);
    return Immutable.listOf(allClassesOfPackage);
  }

  private static URL[] convert(final Collection<URL> urls) {
    return urls.toArray(new URL[0]);
  }

  private URL loadPluginJarWithRandoop() {
    return getClass().getProtectionDomain().getCodeSource().getLocation();
  }

  private static List<URL> loadProjectDependencies(final Project project) throws RandoopCommandException {
    final List<URL> urls = new LinkedList<>();
    try {
      Configuration configuration = project.getConfigurations().getByName("compile");
      configuration.forEach(file -> {
        try {
          urls.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new RandoopCommandException("Could not add artifact!", e);
        }
      });

      project.files(project.getLayout().getBuildDirectory().dir(Constants.PATH_TO_COMPILED_CLASSES))
          .forEach(e -> {
            try {
              urls.add(e.toURI().toURL());
            } catch (MalformedURLException ex) {
              throw new RandoopCommandException("Could not add artifact!", ex);
            }
          });
    } catch (UnknownConfigurationException e) {
      project.getLogger().quiet(e.getMessage());
    }

    return urls;
  }

  private static URL loadProjectClasses(String sourceDirectory) throws RandoopCommandException {
    final URL source;
    try {
      source = createUrlFrom(sourceDirectory);
    } catch (MalformedURLException e) {
      throw new RandoopCommandException("Could not create source path!", e);
    }
    return source;
  }

  private static URL createUrlFrom(final String path) throws MalformedURLException {
    return new File(path).toURI().toURL();
  }
}
