package com.sri.gradle.randoop.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sri.gradle.randoop.Constants;
import com.sri.gradle.randoop.internal.CsvWriter;
import com.sri.gradle.randoop.utils.ImmutableStream;
import com.sri.gradle.randoop.utils.MoreFiles;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class RandoopEvidence extends PluginExtendedTask {

  private static final String METRICS = "RandoopTestsAndMetrics";
  private static final String CONFIG = "RandoopJUnitTestGeneration";
  private static final String QUALIFICATION = "RandoopToolQualification";

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

  private static final Map<String, RecordProcessor> CSVPROCESSORS = ImmutableMap.of(
      METRICS, new MetricsProcessor(),
      CONFIG, new ConfigProcessor(),
      QUALIFICATION, new QualificationProcessor()
  );

  private final DirectoryProperty junitOutputDir;

  private final Property<Integer> timeoutSeconds;
  private final Property<Boolean> stopOnErrorTest;
  private final Property<String> flakyTestBehavior;
  private final Property<Boolean> noErrorRevealingTests;
  private final Property<Boolean> junitReflectionAllowed;
  private final Property<Boolean> usethreads;
  private final Property<Integer> outputLimit;
  private final Property<String> junitPackageName;

  public RandoopEvidence() {
    this.junitOutputDir = getProject().getObjects().directoryProperty(); // unchecked warning

    this.timeoutSeconds = getProject().getObjects().property(Integer.class);
    this.outputLimit = getProject().getObjects().property(Integer.class);
    this.usethreads = getProject().getObjects().property(Boolean.class);
    this.noErrorRevealingTests = getProject().getObjects().property(Boolean.class);
    this.junitReflectionAllowed = getProject().getObjects().property(Boolean.class);
    this.stopOnErrorTest = getProject().getObjects().property(Boolean.class);
    this.flakyTestBehavior = getProject().getObjects().property(String.class);
    this.junitPackageName = getProject().getObjects().property(String.class);
  }

  private static void initEvidence(Map<String, Map<String, Object>> evidenceObjectMap){
    evidenceObjectMap.put(METRICS, new HashMap<>());
    evidenceObjectMap.put(CONFIG, new HashMap<>());
    evidenceObjectMap.put(QUALIFICATION, new HashMap<>());
  }

  @TaskAction
  public void randoopEvidence() {
    // main data structure
    final Map<String, Map<String, Object>> evidence = new HashMap<>();
    initEvidence(evidence);


    final File junitOutputDir = getJunitOutputDir().getAsFile().get();
    Set<File> randoopGeneratedTests =
        MoreFiles.getMatchingFiles(
            junitOutputDir.toPath(),
            Constants.EXPECTED_RANDOOP_TEST_NAME_REGEX);

    final Map<String, Object> metrics = new HashMap<>();
    metrics.put("GENERATED_TEST_FILES_COUNT", String.valueOf(randoopGeneratedTests.size()));

    final Path workingDir = getProject()
            .getProjectDir()
            .toPath();

    final List<String> unitTests = ImmutableStream.listCopyOf(
            randoopGeneratedTests.stream().map(f -> workingDir.relativize(f.toPath()).toString()));

    metrics.put("GENERATED_TEST_FILES", unitTests);

    // tool configuration
    evidence.put(CONFIG, getToolConfig(workingDir));
    // tool qualification
    final Map<String, Object> qualification = new HashMap<>();
    qualification.put("TITLE", "RandoopGradlePlugin");
    qualification.put("SUMMARY", "Runs the Randoop Tool");
    qualification.put("QUALIFIEDBY", "SRI International");
    qualification.put("USERGUIDE", "https://github.com/SRI-CSL/randoop-gradle-plugin/blob/master/README.md");
    qualification.put("INSTALLATION", "https://github.com/SRI-CSL/randoop-gradle-plugin/blob/master/README.md");
    qualification.put("ACTIVITY", "TestGeneration");

    Map<String, Object> generatedTests = new HashMap<>();
    final Path randoopLogFile = workingDir
        .resolve(Constants.RANDOOP_SUMMARY_FILE_NAME);

    final Path randoopJsonFile = workingDir
        .resolve(Constants.RANDOOP_DETAILS_FILE_NAME);

    final ReadWriteRandoopDetails mainProcessor = new ReadWriteRandoopDetails(randoopLogFile,
        randoopJsonFile);

    try {
      final Map<String, Object> allRecords = mainProcessor.mergeRecords(generatedTests);
      // update qualification
      qualification.put("RANDOOP_VERSION", allRecords.getOrDefault("RANDOOP_VERSION", "MISSING"));
      qualification.put("DATE", allRecords.getOrDefault("DATE", "MISSING"));
      qualification.put("RANDOOP_PLUGIN_VERSION", "0.1");

      evidence.put(QUALIFICATION, qualification);

      // tool & metrics
      metrics.put("CHANGES", allRecords.getOrDefault("CHANGES", "MISSING"));
      metrics.put("BRANCH", allRecords.getOrDefault("BRANCH", "MISSING"));
      metrics.put("COMMIT", allRecords.getOrDefault("COMMIT", "MISSING"));

      metrics.put("EXPLORED_CLASSES", allRecords.getOrDefault("EXPLORED_CLASSES", "0"));
      metrics.put("PUBLIC_MEMBERS", allRecords.getOrDefault("PUBLIC_MEMBERS", "0"));
      metrics.put("NORMAL_EXECUTIONS", allRecords.getOrDefault("NORMAL_EXECUTIONS", "0"));
      metrics.put("EXCEPTIONAL_EXECUTIONS", allRecords.getOrDefault("EXCEPTIONAL_EXECUTIONS", "0"));
      metrics.put("AVG_NORMAL_TERMINATION_TIME", allRecords.getOrDefault("AVG_NORMAL_TERMINATION_TIME", "0"));
      metrics.put("AVG_EXCEPTIONAL_TERMINATION_TIME", allRecords.getOrDefault("AVG_EXCEPTIONAL_TERMINATION_TIME", "0"));
      metrics.put("MEMORY_USAGE", allRecords.getOrDefault("MEMORY_USAGE", "0"));
      metrics.put("REGRESSION_TEST_COUNT", allRecords.getOrDefault("REGRESSION_TEST_COUNT", "0"));
      metrics.put("INVALID_TESTS_GENERATED", allRecords.getOrDefault("INVALID_TESTS_GENERATED", "0"));
      metrics.put("ERROR_REVEALING_TEST_COUNT", allRecords.getOrDefault("ERROR_REVEALING_TEST_COUNT", "0"));

      int x = Integer.parseInt(String.valueOf(metrics.getOrDefault("REGRESSION_TEST_COUNT", "0")));
      int y = Integer.parseInt(String.valueOf(metrics.getOrDefault("INVALID_TESTS_GENERATED", "0")));
      metrics.put("NUMBEROFTESTCASES", String.valueOf(x + y));
      evidence.put(METRICS, metrics);

      System.out.println(evidence);

      mainProcessor.writeJson(evidence);
      mainProcessor.writeCsv(evidence);
      getLogger().debug(allRecords.size() + " records extracted.");
    } catch (IOException ioe){
      throw new GradleException("Unable to process " + randoopLogFile, ioe);
    }

    getLogger().quiet("Successfully generated evidence");
  }

  @Override
  public Property<String> getFlakyTestBehavior() {
    return flakyTestBehavior;
  }

  @Override
  public Property<String> getJunitPackageName() {
    return junitPackageName;
  }

  @Override
  public Property<Integer> getTimeoutSeconds() {
    return timeoutSeconds;
  }

  @Override
  public Property<Integer> getOutputLimit() {
    return outputLimit;
  }

  @Override
  public Property<Boolean> getUsethreads() {
    return usethreads;
  }

  @Override
  public Property<Boolean> getNoErrorRevealingTests() {
    return noErrorRevealingTests;
  }

  @Override
  public Property<Boolean> getJunitReflectionAllowed() {
    return junitReflectionAllowed;
  }

  @Override
  public Property<Boolean> getStopOnErrorTest() {
    return stopOnErrorTest;
  }


  @Override
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

  private Map<String, Object> getToolConfig(Path workingDir){
    Map<String, Object> config = new HashMap<>();
    List<Record> params = new ArrayList<>();
    params.add(new Record("--time-limit", getTimeoutSeconds().get()));
    params.add(new Record("--flaky-test-behavior", getFlakyTestBehavior().get()));
    params.add(new Record("--output-limit", getOutputLimit().get()));
    params.add(new Record("--usethread", getUsethreads().get()));
    params.add(new Record("--no-error-revealing-tests", getNoErrorRevealingTests().get()));
    params.add(new Record("--stop-on-error-test", getStopOnErrorTest().get()));
    params.add(new Record("--junit-reflection-allowed", getJunitReflectionAllowed().get()));
    params.add(new Record("--junit-package-name", getJunitPackageName().get()));
    params.add(new Record("--junit-output-dir", workingDir.relativize(getJunitOutputDir().getAsFile().get().toPath()).toString()));

    config.put("PARAMETERS", params.toString());
    config.put("AUTOMATEDBY", "RandoopGradlePlugin");
    config.put("INVOKEDBY", "RandoopGradlePlugin");
    return config;
  }

  static class ReadWriteRandoopDetails {
    private final Path inputFile;
    private final Path outFile;

    ReadWriteRandoopDetails(Path inputFile, Path outFile){
      this.inputFile = Objects.requireNonNull(inputFile);
      this.outFile = outFile;
    }

    List<Map<String, Object>> processLineByLine() throws IOException {
      Preconditions.checkArgument(Files.exists(inputFile));
      List<Map<String, Object>> allRecords = new LinkedList<>();
      try (Scanner scanner =  new Scanner(inputFile, Constants.ENCODING.name())){
        while (scanner.hasNextLine()){
          final Map<String, Object> record = processLine(scanner.nextLine());
          if (!record.isEmpty()){
            allRecords.add(record);
          }
        }
      }

      return ImmutableStream.listCopyOf(
          allRecords.stream().filter(m -> !m.isEmpty()));
    }

    Map<String, Object> processLine(String line){
      for (LineProcessor each : PROCESSORS){
        final Optional<Map<String, Object>> record = each.process(line);
        if (record.isPresent()){
          return record.get();
        }
      }

      return ImmutableMap.of();
    }

    void writeJson(Map<String, Map<String, Object>> evidence) throws IOException {
      final Map<String, Map<String, Map<String, Object>>> jsonDoc = new HashMap<>();
      jsonDoc.put("Evidence", evidence);

      if (Files.exists(outFile)){
        Files.delete(outFile);
      }

      final Gson gson = new GsonBuilder().setPrettyPrinting().create();
      try (Writer writer = Files.newBufferedWriter(outFile)) {
        gson.toJson(jsonDoc, writer);
      }

    }

    void writeCsv(Map<String, Map<String, Object>> evidence){
      final Path workingDir = outFile.getParent();
      for (String each : evidence.keySet()){
        final Map<String, Object> recordMap = evidence.get(each);
        final RecordProcessor processor = CSVPROCESSORS.getOrDefault(each, null);
        if (processor == null) continue;
        processor.process(recordMap, workingDir);
      }
    }

    Map<String, Object> mergeRecords(Map<String, Object> other) throws IOException {
      // uses a map that guarantees insertion order of inserted records
      final Map<String, Object> merged = new IdentityHashMap<>();

      final List<Map<String, Object>> records = new LinkedList<>(processLineByLine());
      for (Map<String, Object> each : records){
        if (each.isEmpty()) continue;
        merged.putAll(each);
      }

      merged.putAll(other);

      return ImmutableMap.copyOf(merged);
    }
  }

  public static class Record {
    String key;
    Object val;
    Record(String key, Object val){
      this.key = key;
      this.val = val;
    }

    public String getKey() {
      return key;
    }

    public Object getVal() {
      return val;
    }

    @Override
    public String toString() {
      return key + ":" + val.toString();
    }
  }


  interface RecordProcessor {
    void process(Map<String, Object> record, Path outputDir);
    default Set<Record> recordSet(Map<String, Object> record){
      final Set<Record> records = new HashSet<>();
      for (String each : record.keySet()){
        Object value = record.get(each);
        final Record r = new Record(each, value);
        records.add(r);
      }
      return records;
    }
  }

  static class MetricsProcessor implements RecordProcessor {
    @Override
    public void process(Map<String, Object> record, Path outputDir) {
      final StringBuilder sb = new StringBuilder();
      CsvWriter.writeCsv(outputDir, "RandoopTestsAndMetrics.csv", sb, recordSet(record));
    }
  }

  static class ConfigProcessor implements RecordProcessor {
    @Override
    public void process(Map<String, Object> record, Path outputDir) {
      final StringBuilder sb = new StringBuilder();
      CsvWriter.writeCsv(outputDir, "RandoopJUnitTestGeneration.csv", sb, recordSet(record));
    }
  }

  static class QualificationProcessor implements RecordProcessor {
    @Override
    public void process(Map<String, Object> record, Path outputDir) {
      final StringBuilder sb = new StringBuilder();
      CsvWriter.writeCsv(outputDir, "RandoopToolQualification.csv", sb, recordSet(record));
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
    abstract Optional<Map<String, Object>> process(String text);
  }

  static class RandoopVersion extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(VERSION_DETAILS)) return Optional.empty();
      String[] infoArray = text
          .substring(text.indexOf("\"") + 1, text.lastIndexOf("\""))
          .split(",");

      final String localChanges = infoArray[1].isEmpty() ? "NONE" : infoArray[1].trim().split(" ")[0].trim();
      final Map<String, Object> metadata = new HashMap<>();
      metadata.put("RANDOOP_VERSION", infoArray[0]);
      metadata.put("CHANGES", localChanges);
      metadata.put("BRANCH", infoArray[2].trim().split(" ")[1].trim());
      metadata.put("COMMIT", infoArray[3].trim().split(" ")[1].trim());
      metadata.put("DATE", infoArray[4].trim());

      return Optional.of(metadata);
    }
  }

  static class ExploredClasses extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(EXPLORED_CLASSES)) return Optional.empty();
      String[] infoArray = text.split(" ");
      final Map<String, Object> records = new HashMap<>();
      records.put("EXPLORED_CLASSES", infoArray[2].trim());
      return Optional.of(records);
    }
  }

  static class PublicMembers extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(PUBLIC_MEMBERS)) return Optional.empty();
      String[] infoArray = text.split("=");
      final Map<String, Object> records = new HashMap<>();
      records.put("PUBLIC_MEMBERS", infoArray[1].trim());
      return Optional.of(records);
    }

  }

  static class NormalExec extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(NORMAL_EXECUTIONS)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, Object> records = new HashMap<>();
      records.put("NORMAL_EXECUTIONS", infoArray[1].trim());
      return Optional.of(records);
    }
  }

  static class ExceptionalExec extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(EXCEPTIONAL_EXECUTIONS)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, Object> records = new HashMap<>();
      records.put("EXCEPTIONAL_EXECUTIONS", infoArray[1].trim());
      return Optional.of(records);
    }
  }

  static class NormalTermination extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(AVG_NORMAL_TERMINATION_TIME)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, Object> records = new HashMap<>();
      records.put("AVG_NORMAL_TERMINATION_TIME", infoArray[1].trim());
      return Optional.of(records);
    }
  }


  static class ExceptionalTermination extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(AVG_EXCEPTIONAL_TERMINATION_TIME)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, Object> records = new HashMap<>();
      records.put("AVG_EXCEPTIONAL_TERMINATION_TIME", infoArray[1].trim());
      return Optional.of(records);
    }
  }


  static class MemoryUsage extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(MEMORY_USAGE)) return Optional.empty();
      String[] infoArray = text.split(" ");
      final Map<String, Object> records = new HashMap<>();
      records.put("MEMORY_USAGE", infoArray[3].trim());
      return Optional.of(records);
    }
  }

  static class RegressionTestCount extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(REGRESSION_TEST_COUNT)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, Object> records = new HashMap<>();
      records.put("REGRESSION_TEST_COUNT", infoArray[1].trim());
      return Optional.of(records);
    }
  }

  static class InvalidTestCount extends LineProcessor {
    @Override
    Optional<Map<String, Object>> process(String text) {
      if (!text.startsWith(INVALID_TESTS_GENERATED)) return Optional.empty();
      String[] infoArray = text.split(":");
      final Map<String, Object> records = new HashMap<>();
      records.put("INVALID_TESTS_GENERATED", infoArray[1].trim());
      return Optional.of(records);
    }
  }
}
