package com.sri.gradle.randoop.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.utils.ImmutableStream;
import com.sri.gradle.randoop.utils.MoreFiles;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

@SuppressWarnings("UnstableApiUsage")
public class RandoopEvidence extends DescribedTask {

  private static final Set<LineProcessor> PROCESSORS = ImmutableSet.of(
      new RandoopVersion(),
      new ExploredClasses(),
      new PublicMembers(),
      new NormalExec(),
      new ExceptionalExec(),
      new NormalTermination(),
      new ExceptionalTermination(),
      new MemoryUsage(),
      new RegressionTestCount(),
      new InvalidTestCount()
  );

  private final DirectoryProperty junitOutputDir;

  public RandoopEvidence() {
    this.junitOutputDir = getProject().getObjects().directoryProperty(); // unchecked warning
  }

  @TaskAction
  public void randoopEvidence() {
    final File junitOutputDir = getJunitOutputDir().getAsFile().get();
    Set<File> randoopGeneratedTests =
        MoreFiles.getMatchingFiles(
            junitOutputDir.toPath(),
            Constants.EXPECTED_RANDOOP_TEST_NAME_REGEX);

    Map<String, String> generatedTests = new HashMap<>();
    generatedTests.put("ACTIVITY", "TEST_GENERATION");
    generatedTests.put("AGENT", "RANDOOP");
    generatedTests.put("GENERATED_TEST_COUNT", String.valueOf(randoopGeneratedTests.size()));

    final Path randoopLogFile = getProject()
        .getProjectDir()
        .toPath()
        .resolve(Constants.RANDOOP_SUMMARY_FILE_NAME);

    final Path randoopJsonFile = getProject()
        .getProjectDir()
        .toPath()
        .resolve(Constants.RANDOOP_DETAILS_FILE_NAME);

    final ReadWriteRandoopDetails mainProcessor = new ReadWriteRandoopDetails(randoopLogFile,
        randoopJsonFile);

    try {
      final Map<String, String> allRecords = mainProcessor.mergeRecords(generatedTests);
      mainProcessor.writeTo(allRecords);
      getLogger().debug(allRecords.size() + " records extracted.");
    } catch (IOException ioe){
      throw new GradleException("Unable to process " + randoopLogFile, ioe);
    }

    getLogger().quiet("Successfully generated evidence");
  }

  @OutputDirectory
  public DirectoryProperty getJunitOutputDir() {
    return this.junitOutputDir;
  }

  @Override
  protected String getTaskName() {
    return Constants.RANDOOP_DETAILS_TASK_NAME;
  }

  @Override
  protected String getTaskDescription() {
    return Constants.RANDOOP_DETAILS_TASK_DESCRIPTION;
  }


  static class ReadWriteRandoopDetails {
    private final Path inputFile;
    private final Path outFile;

    ReadWriteRandoopDetails(Path inputFile, Path outFile){
      this.inputFile = Objects.requireNonNull(inputFile);
      this.outFile = outFile;
    }

    List<Map<String, String>> processLineByLine() throws IOException {
      Preconditions.checkArgument(Files.exists(inputFile));
      List<Map<String, String>> allRecords = new LinkedList<>();
      try (Scanner scanner =  new Scanner(inputFile, Constants.ENCODING.name())){
        while (scanner.hasNextLine()){
          final Map<String, String> record = processLine(scanner.nextLine());
          if (!record.isEmpty()){
            allRecords.add(record);
          }
        }
      }

      return ImmutableStream.listCopyOf(
          allRecords.stream().filter(m -> !m.isEmpty()));
    }

    Map<String, String> processLine(String line){
      for (LineProcessor each : PROCESSORS){
        final Optional<Map<String, String>> record = each.process(line);
        if (record.isPresent()){
          return record.get();
        }
      }

      return ImmutableMap.of();
    }

    void writeTo(Map<String, String> otherRecord) throws IOException {
      final Map<String, Map<String, String>> jsonDoc = new HashMap<>();
      jsonDoc.put(Constants.EVIDENCE_ONLY.toUpperCase(Locale.ROOT), otherRecord);

      if (Files.exists(outFile)){
        Files.delete(outFile);
      }

      final Gson gson = new GsonBuilder().setPrettyPrinting().create();
      try (Writer writer = Files.newBufferedWriter(outFile)) {
        gson.toJson(jsonDoc, writer);
      }

    }

    Map<String, String> mergeRecords(Map<String, String> other) throws IOException {
      // uses a map that guarantees insertion order of inserted records
      final Map<String, String> merged = new IdentityHashMap<>();

      final List<Map<String, String>> records = new LinkedList<>(processLineByLine());
      for (Map<String, String> each : records){
        if (each.isEmpty()) continue;
        merged.putAll(each);
      }

      merged.putAll(other);

      return ImmutableMap.copyOf(merged);
    }
  }

  static abstract class LineProcessor {
    static final String VERSION_DETAILS = "Randoop for Java version";
    static final String EXPLORED_CLASSES = "Will explore";
    static final String PUBLIC_MEMBERS = "PUBLIC MEMBERS";
    static final String NORMAL_EXECUTIONS = "Normal method executions";
    static final String EXCEPTIONAL_EXECUTIONS = "Normal method executions";
    static final String AVG_NORMAL_TERMINATION_TIME = "Average method execution time (normal termination)";
    static final String AVG_EXCEPTIONAL_TERMINATION_TIME = "Average method execution time (exceptional termination)";
    static final String MEMORY_USAGE = "Approximate memory usage";
    static final String REGRESSION_TEST_COUNT = "Regression test count";
    static final String INVALID_TESTS_GENERATED = "Invalid tests generated";
    abstract Optional<Map<String, String>> process(String text);
  }

  static class RandoopVersion extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(VERSION_DETAILS)) return Optional.empty();
      String[] infoArray = text
          .substring(text.indexOf("\"") + 1, text.lastIndexOf("\""))
          .split(",");

      final String localChanges = infoArray[1].isEmpty() ? "NONE" : infoArray[1].trim().split(" ")[0].trim();
      final Map<String, String> records = new HashMap<>();
      records.put("RANDOOP_VERSION", infoArray[0]);
      records.put("CHANGES", localChanges);
      records.put("BRANCH", infoArray[2].trim().split(" ")[1].trim());
      records.put("COMMIT", infoArray[3].trim().split(" ")[1].trim());
      records.put("DATE", infoArray[4].trim());

      return Optional.of(records);
    }
  }

  static class ExploredClasses extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(EXPLORED_CLASSES)) return Optional.empty();
      String[] infoArray = text.split(" ");
      final Map<String, String> records = new HashMap<>();
      records.put("EXPLORED_CLASSES", infoArray[2].trim());
      return Optional.of(records);
    }
  }

  static class PublicMembers extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(PUBLIC_MEMBERS)) return Optional.empty();
      String[] infoArray = text.split("=");
      final Map<String, String> records = new HashMap<>();
      records.put("PUBLIC_MEMBERS", infoArray[1].trim());
      return Optional.of(records);
    }

  }

  static class NormalExec extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(NORMAL_EXECUTIONS)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, String> records = new HashMap<>();
      records.put("NORMAL_EXECUTIONS", infoArray[1].trim());
      return Optional.of(records);
    }
  }

  static class ExceptionalExec extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(EXCEPTIONAL_EXECUTIONS)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, String> records = new HashMap<>();
      records.put("EXCEPTIONAL_EXECUTIONS", infoArray[1].trim());
      return Optional.of(records);
    }
  }

  static class NormalTermination extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(AVG_NORMAL_TERMINATION_TIME)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, String> records = new HashMap<>();
      records.put("AVG_NORMAL_TERMINATION_TIME", infoArray[1].trim());
      return Optional.of(records);
    }
  }


  static class ExceptionalTermination extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(AVG_EXCEPTIONAL_TERMINATION_TIME)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, String> records = new HashMap<>();
      records.put("AVG_EXCEPTIONAL_TERMINATION_TIME", infoArray[1].trim());
      return Optional.of(records);
    }
  }


  static class MemoryUsage extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(MEMORY_USAGE)) return Optional.empty();
      String[] infoArray = text.split(" ");
      final Map<String, String> records = new HashMap<>();
      records.put("MEMORY_USAGE", infoArray[3].trim());
      return Optional.of(records);
    }
  }

  static class RegressionTestCount extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(REGRESSION_TEST_COUNT)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, String> records = new HashMap<>();
      records.put("REGRESSION_TEST_COUNT", infoArray[1].trim());
      return Optional.of(records);
    }
  }

  static class InvalidTestCount extends LineProcessor {
    @Override Optional<Map<String, String>> process(String text) {
      if (!text.startsWith(INVALID_TESTS_GENERATED)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, String> records = new HashMap<>();
      records.put("INVALID_TESTS_GENERATED", infoArray[1].trim());
      return Optional.of(records);
    }
  }
}
