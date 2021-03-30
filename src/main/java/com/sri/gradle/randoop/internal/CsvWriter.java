package com.sri.gradle.randoop.internal;

import com.sri.gradle.randoop.tasks.RandoopEvidence.Record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

public class CsvWriter {
    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public static void writeCsv(Path outputDir, String csvPath, StringBuilder sb, Set<Record> evidence){
        final Iterator<Record> iterator = evidence.iterator();
        while(iterator.hasNext()){
            final Record r = iterator.next();
            String eK = escapeSpecialCharacters(r.getKey());
            sb.append(eK);
            if (iterator.hasNext()){
                sb.append(",");
            }
        }
        sb.append("\n");
        final Path resolvedPath = outputDir.resolve(csvPath);
        writeCsv(resolvedPath, sb, evidence);
    }

    public static void writeCsv(Path csvPath, StringBuilder sb, Set<Record> map) {
        System.out.println("Writing " + csvPath);
        try (PrintWriter writer = new PrintWriter(csvPath.toFile())) {

            final Iterator<Record> iterator = map.iterator();
            while(iterator.hasNext()){
                final Record each = iterator.next();
                String eV = escapeSpecialCharacters(each.getVal().toString());
                sb.append(eV);
                if (iterator.hasNext()){
                    sb.append(",");
                } else {
                    sb.append("\n");
                }
            }

            writer.write(sb.toString());
            System.out.printf("Generated %s file%n", csvPath);

        } catch (FileNotFoundException e) {
            System.out.println("Unable to generate " + csvPath + ". Reason: " + e.getLocalizedMessage());
        }

    }
}

