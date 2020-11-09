package com.sri.gradle.randoop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sri.gradle.randoop.utils.ClasslistGenerator;
import com.sri.gradle.randoop.utils.Javafinder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class RandoopPluginTest {
  @Test public void testJavafinder(){
    Path dir = new File("src/main/java/com/sri/gradle/randoop/utils").toPath();
    System.out.println(dir);
    List<File> filesAvailable = Javafinder.findJavaFiles(dir);
    assertEquals(filesAvailable.size(), 6);
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