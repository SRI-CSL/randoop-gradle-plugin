package com.sri.gradle.tasks;

import com.sri.gradle.Constants;
import com.sri.gradle.internal.RandoopCommand;
import java.io.File;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class CheckForRandoop extends RandoopTask {

  @TaskAction public void checkForRandoop(){
    try {
      final File randoopJar = getProject().getLayout()
          .getProjectDirectory()
          .dir(Constants.LIB_DIR)
          .file(Constants.RANDOOP_JAR)
          .getAsFile();

      new RandoopCommand(getTaskName())
          .setGeneratorJar(randoopJar)
          .execute();

    } catch (Exception e){
      throw new GradleException(
          "Randoop is not installed on this machine.\n" +
          "For latest release, see: https://github.com/randoop/randoop/releases/"
      );
    }


  }

  @Override protected String getTaskName() {
    return "help";
  }

  @Override protected String getTaskDescription() {
    return null;
  }
}
