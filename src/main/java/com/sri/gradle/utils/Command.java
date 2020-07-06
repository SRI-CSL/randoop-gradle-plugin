package com.sri.gradle.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Command {

  private final PrintStream stdout;
  private final PrintStream stderr;
  private final List<String> args;

  private final Map<String, String> environment;
  private final File workingDirectory;
  private final boolean permitNonZeroExitStatus;

  private volatile Process process;

  /**
   * Constructs a new Command using elements specified in its builder.
   *
   * @param builder the command builder.
   */
  private Command(Builder builder) {
    final Builder nonNullBuilder = Objects.requireNonNull(builder);

    this.stdout = nonNullBuilder.stdout;
    this.stderr = nonNullBuilder.stderr;
    this.args = new ArrayList<>(nonNullBuilder.args);
    this.environment = nonNullBuilder.env;

    this.workingDirectory = nonNullBuilder.workingDirectory;
    this.permitNonZeroExitStatus = nonNullBuilder.permitNonZeroExitStatus;

    // checks if we maxed out the number of budgeted arguments
    if (nonNullBuilder.maxCommandLength != -1) {
      final String string = toString();
      if (string.length() > nonNullBuilder.maxCommandLength) {
        throw new IllegalStateException(
            "Maximum command length " +
                nonNullBuilder.maxCommandLength + " exceeded by: " + string);
      }
    }
  }

  /**
   * Creates a Command.Builder object
   */
  public static Builder create() {
    return create(System.out, System.err);
  }

  /**
   * Creates a Command.Builder object
   */
  public static Builder create(PrintStream stdout, PrintStream stderr) {
    return new Builder(stdout, stderr);
  }

  /**
   * starts the command
   *
   * @throws IOException if unable to start command.
   */
  public void start() throws IOException {
    if (isStarted()) {
      throw new IllegalStateException("Already started!");
    }

    stdout.println("executing " + this);

    final ProcessBuilder processBuilder = new ProcessBuilder().command(args)
        .redirectErrorStream(true);

    if (workingDirectory != null) {
      processBuilder.directory(workingDirectory);
    }

    processBuilder.environment().putAll(environment);

    stdout.println("INFO: " + String.format("Current process: %s", toString()));

    process = processBuilder.start();
  }

  /**
   * @return true if the process has started; false otherwise.
   */
  public boolean isStarted() {
    return process != null;
  }

  /**
   * @return the current input stream used by running process.
   */
  public InputStream getInputStream() {
    if (!isStarted()) {
      throw new IllegalStateException("Not started!");
    }

    return process.getInputStream();
  }

  /**
   * Returns the output returned by process.
   *
   * @return the output on terminal.
   * @throws IOException          unexpected behavior occurred.
   * @throws InterruptedException unexpected behavior occurred.
   */
  public List<String> gatherOutput() throws IOException, InterruptedException {
    if (!isStarted()) {
      throw new IllegalStateException("Not started!");
    }

    try (BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(getInputStream(), StandardCharsets.UTF_8))) {
      final List<String> outputLines = new ArrayList<>();

      String outputLine;
      while ((outputLine = bufferedReader.readLine()) != null) {
        if (stdout != null) {
          stdout.println(outputLine);
        }

        outputLines.add(outputLine);
      }

      int exitValue = process.waitFor();

      if (exitValue != 0 && !permitNonZeroExitStatus) {
        throw new CommandFailedException(args, outputLines);
      }

      return outputLines;

    }
  }

  /**
   * @return the output displayed on the terminal.
   */
  public List<String> execute() {
    try {
      start();
      return gatherOutput();
    } catch (IOException e) {
      stderr.println(e.getMessage());
      throw new RuntimeException("Failed to execute process: " + args, e);
    } catch (InterruptedException e) {
      stderr.println(e.getMessage());
      throw new RuntimeException("Interrupted while executing process: " + args, e);
    }
  }

  @Override public String toString() {
    final String entrySetAsString = environment.entrySet().stream().map(Object::toString)
        .collect(Collectors.joining(" "));
    String envString = !environment.isEmpty() ? (entrySetAsString + " ") : "";
    return envString + String.join(" ", args);

  }

  /**
   * Command builder
   */
  public static class Builder {

    private final PrintStream stdout;
    private final PrintStream stderr;
    private final List<String> args;
    private final Map<String, String> env;

    private File workingDirectory;
    private boolean permitNonZeroExitStatus;
    private int maxCommandLength;

    /**
     * Creates a command builder.
     */
    Builder(PrintStream stdout, PrintStream stderr) {
      this.stdout = Objects.requireNonNull(stdout);
      this.stderr = Objects.requireNonNull(stderr);

      this.workingDirectory = null;
      this.permitNonZeroExitStatus = false;

      this.maxCommandLength = Integer.MAX_VALUE;

      this.args = new ArrayList<>();
      this.env = new LinkedHashMap<>();
    }

    public List<String> getArgs() {
      return Immutable.listOf(args);
    }

    /**
     * Sets the command's arguments.
     *
     * @param args the command's arguments.
     * @return self
     */
    public Builder arguments(Object... args) {
      return arguments(Immutable.listOf(Arrays.stream(args).map(Object::toString)));
    }

    /**
     * Sets the command's list of arguments.
     *
     * @param args the command's list of arguments.
     * @return self
     */
    public Builder arguments(List<String> args) {
      this.args.addAll(args);
      return this;
    }

    /**
     * Sets an environment's variable.
     *
     * @param key   key identifying the variable
     * @param value the value of the variable
     * @return self
     */
    public Builder environment(String key, String value) {
      env.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
      return this;
    }

    /**
     * Sets the command's working directory.
     *
     * @param localWorkingDirectory the command's working directory.
     * @return self
     */
    public Builder workingDirectory(File localWorkingDirectory) {
      this.workingDirectory = Objects.requireNonNull(localWorkingDirectory);
      return this;
    }

    /**
     * Prevents command from throwing if the invoked process returns a nonzero exit code.
     *
     * @return self
     */
    public Builder permitNonZeroExitStatus() {
      this.permitNonZeroExitStatus = true;
      return this;
    }

    /**
     * Sets the permitted length of a command in its String representation.
     *
     * @param maxLength the length of a command (string representation of command)
     * @return self.
     */
    public Builder maxCommandLength(int maxLength) {
      this.maxCommandLength = maxLength;
      return this;
    }

    /**
     * @return the built command.
     */
    public Command build() {
      return new Command(this);
    }

    /**
     * Shortcut to execute a command
     *
     * @return a list of lines representing the output of the command.
     */
    public List<String> execute() {
      return build().execute();
    }


    @Override public String toString() {
      final String left = this.args.toString();
      String right = Optional.ofNullable(workingDirectory)
          .map(File::toString)
          .orElse("");
      return left + " : " + right;
    }
  }

  /**
   * Command failed to execute exception.
   */
  private static class CommandFailedException extends RuntimeException {

    /**
     * Construct a new CommandFailedException object.
     *
     * @param args        list of command's args.
     * @param outputLines list of output lines displayed on terminal.
     */
    CommandFailedException(List<String> args, List<String> outputLines) {
      this(formatMessage(args, outputLines));
    }

    /**
     * Construct a new CommandFailedException object.
     *
     * @param message exception message
     */
    CommandFailedException(String message) {
      super(message);
    }

    /**
     * Turns a list of args and output lines into a formatted message.
     *
     * @param args        list of command's args.
     * @param outputLines list of output lines displayed on terminal.
     * @return formatted message.
     */
    static String formatMessage(List<String> args, List<String> outputLines) {
      StringBuilder result = new StringBuilder();
      result.append("Command failed:");

      for (String arg : args) {
        result.append(" ").append(arg);
      }

      for (String outputLine : outputLines) {
        result.append("\n  ").append(outputLine);
      }

      return result.toString();
    }
  }
}