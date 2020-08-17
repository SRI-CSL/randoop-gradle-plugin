package com.sri.gradle;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.sri.gradle.utils.Classfinder;
import com.sri.gradle.utils.ClasslistGenerator;
import com.sri.gradle.utils.Command;
import com.sri.gradle.utils.Javafinder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class RandoopPluginTest {
  @Test public void testClassfinder(){
    List<Class<?>> files = Classfinder.findClasses(
        "com.sri.gradle", getClass().getClassLoader());

    assertFalse(files.isEmpty());
  }

  @Test public void testJavafinder(){
    Path dir = new File("src/main/java/com/sri/gradle/utils").toPath();
    System.out.println(dir);
    List<File> filesAvailable = Javafinder.findJavaFiles(dir);
    assertThat(filesAvailable.size(), is(5));
  }

  @Test public void testCommandBuilder(){
    List<String> filesAvailable = Command.create().arguments("ls")
        .permitNonZeroExitStatus().execute();

    assertThat(filesAvailable.size(), is(9));
  }

  @Test public void testClasslistGeneration(){
    Path dir = new File("src/main/java/com/sri/gradle/utils").toPath();
    System.out.println(dir);
    List<File> javaFiles = Javafinder.findJavaFiles(dir);

    Path classListFile = new File("src/test/java/resources/classlist.txt").toPath();
    Path resourceDirPath = new File("src/test/java/resources/").toPath();

    ClasslistGenerator.generateClasslist(javaFiles, classListFile, resourceDirPath);

    assertTrue(Files.exists(classListFile));

    if(Files.exists(classListFile)){
      ClasslistGenerator.deleteFile(classListFile);
    }
  }
}