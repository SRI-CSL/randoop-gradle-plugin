package com.sri.gradle.internal;

import com.sri.gradle.utils.Classfinder;
import com.sri.gradle.utils.Command;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;

public class RandoopGenerator implements Generator {

  private static final String MAIN = "randoop.main.Main";
  private static final String GENERATE_TESTS = "gentests";
  private static final String SRC_DIR = "src/main/java";

  private final Command.Builder builder;
  private final Project project;

  public RandoopGenerator(Project project) {
    this.builder = Command.create()
        .arguments("java")
        .arguments("-ea")
        .permitNonZeroExitStatus();
    this.project = project;
  }

  @Override public void generate(GeneratorOptions config) {
    try {

      final String sourceDir = project.getLayout().getProjectDirectory().dir(SRC_DIR).getAsFile()
          .getAbsolutePath();
      final String outputDir = config.getOutputDir();
      final String packageName = config.getPackageName();
      final int timeout = config.getTimeoutSeconds();
      final String randoopJar = config.getRandoopJarPath();

      final List<URL> urls = new LinkedList<>();
      urls.add(loadProjectClasses(randoopJar));
      urls.add(loadProjectClasses(sourceDir));
      urls.add(loadProjectClasses(outputDir));
      urls.addAll(loadProjectDependencies(project));
      urls.add(loadPluginJarWithRandoop());

      final String classPath = urls.stream()
          .map(URL::toString)
          .collect(Collectors.joining(File.pathSeparator));

      builder
          .arguments("-classpath", classPath)
          .arguments(MAIN)
          .arguments(GENERATE_TESTS)
          .arguments("--time-limit=" + timeout)
          .arguments("--debug-checks=true")
          .arguments("--junit-package-name=" + packageName)
          .arguments("--junit-output-dir=" + outputDir);

      // Add project classes
      final List<URL> compiledClasses = new LinkedList<>();
      project.files(project.getLayout().getBuildDirectory().dir("classes/java/main/"))
          .forEach(e -> {
            try {
              compiledClasses.add(e.toURI().toURL());
            } catch (MalformedURLException ex) {
              throw new RuntimeException("Could not add artifact!", ex);
            }
          });
      final URLClassLoader classLoader = new URLClassLoader(convert(compiledClasses));
      final List<Class<?>> allClassesOfPackage = Classfinder.find(packageName, classLoader);
      for (Class<?> currentClass : allClassesOfPackage) {
        project.getLogger().info("Add class " + currentClass.getName());
        builder.arguments("--testclass=" + currentClass.getName());
      }

      builder.execute().forEach(System.out::println);
    } catch (Exception e) {
      e.printStackTrace();
      throw new GradleException("Unable to generate tests", e);
    }
  }

  private static URL[] convert(final Collection<URL> urls) {
    return urls.toArray(new URL[0]);
  }

  private URL loadPluginJarWithRandoop() {
    return getClass().getProtectionDomain().getCodeSource().getLocation();
  }

  private static List<URL> loadProjectDependencies(final Project project) throws RuntimeException {
    final List<URL> urls = new LinkedList<>();
    try {
      Configuration configuration = project.getConfigurations().getByName("compile");
      configuration.forEach(file -> {
        try {
          urls.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
          throw new RuntimeException("Could not add artifact!", e);
        }
      });

      project.files(project.getLayout().getBuildDirectory().dir("classes/java/main/com/foo"))
          .forEach(e -> {
            try {
              urls.add(e.toURI().toURL());
            } catch (MalformedURLException ex) {
              throw new RuntimeException("Could not add artifact!", ex);
            }
          });
    } catch (UnknownConfigurationException e) {
      System.out.println(e.getMessage());
    }

    return urls;
  }

  private static URL loadProjectClasses(String sourceDirectory) throws RuntimeException {
    final URL source;
    try {
      source = createUrlFrom(sourceDirectory);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Could not create source path!", e);
    }
    return source;
  }

  private static URL createUrlFrom(final String path) throws MalformedURLException {
    return new File(path).toURI().toURL();
  }
}
