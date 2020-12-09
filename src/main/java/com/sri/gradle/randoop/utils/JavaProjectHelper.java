package com.sri.gradle.randoop.utils;

import com.google.common.collect.ImmutableSet;
import com.sri.gradle.randoop.Constants;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public class JavaProjectHelper {

  private final Project project;

  public JavaProjectHelper(Project project) {
    this.project = project;
  }

  public Set<File> getClasspath(File... additionalFiles) {
    final Set<File> classpath = new HashSet<>();
    for (File each : additionalFiles) {
      if (each == null) continue;
      classpath.add(each);
    }

    classpath.add(getBuildMainDir().getAsFile());
    classpath.add(getBuildTestDir().getAsFile());

    classpath.addAll(getRuntimeClasspath());
    return classpath;
  }

  public <T extends Task> T task(String taskName, Class<T> taskType) {
    return taskType.cast(task(getProject(), taskName));
  }

  public static Task task(Project project, String taskName) {
    return project.getTasks().getByName(taskName);
  }

  public <T extends Task> Optional<T> findTask(String taskName, Class<T> taskType) {
    return findTask(taskName).map(taskType::cast);
  }

  public Optional<Task> findTask(String taskName) {
    return Optional.ofNullable(project.getTasks().findByName(taskName));
  }

  public Optional<File> findClassListFile() {
    final Directory resourcesDir = getTestResourcesDir();
    final Path resourcesDirPath = resourcesDir.getAsFile().toPath();
    if (!Files.exists(resourcesDirPath)) {
      getProject().getLogger().debug("Unable to find resources directory");
      return Optional.empty();
    }

    final RegularFile classlistFile = resourcesDir.file(Constants.CLASS_LIST_FILE);
    final File cf = classlistFile.getAsFile();
    if (!Files.exists(cf.toPath())) return Optional.empty();

    return Optional.of(cf);
  }

  public Set<File> getRuntimeClasspath() {
    return ImmutableSet.copyOf(getTestSourceSet(getProject()).getRuntimeClasspath().getFiles());
  }

  public Project getProject() {
    return project;
  }

  public File getDriverDir() {
    return new File(getProject().getBuildDir(), "driver");
  }

  public DirectoryProperty getBuildDir() {
    Objects.requireNonNull(getProject());
    return getProject().getLayout().getBuildDirectory();
  }

  public Directory getSrcMainDir() {
    Objects.requireNonNull(getProject());
    return getProjectDir().dir(Constants.PROJECT_MAIN_SRC_DIR);
  }

  public Directory getSrcTestDir() {
    Objects.requireNonNull(getProject());
    return getProjectDir().dir(Constants.PROJECT_TEST_SRC_DIR);
  }

  public Directory getTestResourcesDir() {
    Objects.requireNonNull(getProject());
    return getProjectDir().dir(Constants.PROJECT_TEST_RESOURCES_DIR);
  }

  public RegularFile getClassListFile() {
    return getTestResourcesDir().file(Constants.CLASS_LIST_FILE);
  }

  public Directory getProjectDir() {
    Objects.requireNonNull(getProject());
    return getProject().getLayout().getProjectDirectory();
  }

  public Directory getBuildMainDir() {
    return getBuildDir().dir(Constants.PROJECT_MAIN_CLASS_DIR).get();
  }

  public Directory getBuildTestDir() {
    return getBuildDir().dir(Constants.PROJECT_TEST_CLASS_DIR).get();
  }

  public SourceSet getTestSourceSet() {
    return sourceSet(getProject(), SourceSet.TEST_SOURCE_SET_NAME);
  }

  public static SourceSet sourceSet(Project project, String sourceSetName) {
    return sourceSets(project).getByName(sourceSetName);
  }

  public static SourceSetContainer sourceSets(Project project) {
    return extension(project, SourceSetContainer.class);
  }

  public static <T> T extension(Project project, Class<T> extensionType) {
    return project.getExtensions().getByType(extensionType);
  }

  private static SourceSet getTestSourceSet(Project project) {
    Objects.requireNonNull(project);
    return ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("test");
  }

  public boolean hasProperty(String property){
    return property != null && getProject().hasProperty(property);
  }
}
