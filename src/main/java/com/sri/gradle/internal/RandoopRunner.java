package com.sri.gradle.internal;

import com.sri.gradle.utils.Classfinder;
import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.Immutable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;

public class RandoopRunner implements Runner {

  private static final String MAIN = "randoop.main.Main";
  private static final String GENERATE_TESTS = "gentests";
  private static final String SRC_DIR = "src/main/java";
  private static final String COMPILED_CLASSES = "classes/java/main/";

  private final Command.Builder builder;
  private final Project project;

  private final List<URL> classpathUrls;
  private int timeLimitSec;
  private String packageName;
  private String outputDir;

  public RandoopRunner(Project project) {
    this.builder = Command.create()
        .arguments("java")
        .arguments("-ea")
        .permitNonZeroExitStatus();
    this.project = project;

    this.classpathUrls = new LinkedList<>();
    this.timeLimitSec = 30;
    this.packageName = null;
    this.outputDir = null;

  }

  public RandoopRunner setClasspath(RegularFileProperty jarfile, DirectoryProperty outdir){
    final File randoopJar = jarfile.getAsFile().get();
    final File projectOut = outdir.getAsFile().get();
    return setGeneratorJar(randoopJar)
        .setOutputDir(projectOut)
        .resolveSourceDir()
        .setProjectDependencies();
  }

  public RandoopRunner setProjectDependencies(){
    classpathUrls.addAll(loadProjectDependencies(project));
    classpathUrls.add(loadPluginJarWithRandoop());
    return this;
  }

  public RandoopRunner setGeneratorJar(File jarfile){
    classpathUrls.add(loadProjectClasses(jarfile.getAbsolutePath()));
    return this;
  }

  public RandoopRunner setOutputDir(File outputDir){
    this.outputDir = outputDir.getAbsolutePath();
    classpathUrls.add(loadProjectClasses(this.outputDir));
    return this;
  }

  public RandoopRunner resolveSourceDir(){
    final File sourceDir = project.getLayout().getProjectDirectory().dir(SRC_DIR).getAsFile();
    classpathUrls.add(loadProjectClasses(sourceDir.getAbsolutePath()));
    return this;
  }

  public RandoopRunner setTimelimit(int timeLimitSeconds){
    this.timeLimitSec = timeLimitSeconds;
    return this;
  }

  public RandoopRunner setPackageName(String packageName){
    this.packageName = packageName;
    return this;
  }

  @Override public boolean run() throws RunnerException {

    try {
      final String classPath = classpathUrls.stream()
          .map(URL::toString)
          .collect(Collectors.joining(File.pathSeparator));

      builder
          .arguments("-classpath", classPath)
          .arguments(MAIN)
          .arguments(GENERATE_TESTS)
          .arguments("--time-limit=" + timeLimitSec)
          .arguments("--debug-checks=true")
          .arguments("--junit-package-name=" + packageName)
          .arguments("--junit-output-dir=" + outputDir);

      // Add project classes
      final List<Class<?>> compiledClasses = findAllCompiledClasses(project, packageName);
      for (Class<?> currentClass : compiledClasses) {
        project.getLogger().info("Add class " + currentClass.getName());
        builder.arguments("--testclass=" + currentClass.getName());
      }

      builder.execute().forEach(System.out::println);
      return true;
    } catch (Exception e){
      project.getLogger().error("Unable to run RandoopRunner", e);
      return false;
    }
  }


  static List<Class<?>> findAllCompiledClasses(final Project project, String packageName){
    final List<URL> compiledClasses = new LinkedList<>();
    project.files(project.getLayout().getBuildDirectory().dir(COMPILED_CLASSES))
        .forEach(e -> {
          try {
            compiledClasses.add(e.toURI().toURL());
          } catch (MalformedURLException ex) {
            throw new RunnerException("Could not add artifact!", ex);
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

  private static List<URL> loadProjectDependencies(final Project project) throws RunnerException {
    final List<URL> urls = new LinkedList<>();
    try {
      Configuration configuration = project.getConfigurations().getByName("compile");
      configuration.forEach(file -> {
        try {
          urls.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new RunnerException("Could not add artifact!", e);
        }
      });

      project.files(project.getLayout().getBuildDirectory().dir(COMPILED_CLASSES))
          .forEach(e -> {
            try {
              urls.add(e.toURI().toURL());
            } catch (MalformedURLException ex) {
              throw new RunnerException("Could not add artifact!", ex);
            }
          });
    } catch (UnknownConfigurationException e) {
      project.getLogger().quiet(e.getMessage());
    }

    return urls;
  }

  private static URL loadProjectClasses(String sourceDirectory) throws RunnerException {
    final URL source;
    try {
      source = createUrlFrom(sourceDirectory);
    } catch (MalformedURLException e) {
      throw new RunnerException("Could not create source path!", e);
    }
    return source;
  }

  private static URL createUrlFrom(final String path) throws MalformedURLException {
    return new File(path).toURI().toURL();
  }
}
