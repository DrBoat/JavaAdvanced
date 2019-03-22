//
//  Java Advanced
//  ru.ifmo.rain.vozleev.walk.Walk
//
//  Created by Elliot Alderson on 10/02/2019.
//  Copyright © 2019 Elliot Alderson. All rights reserved.
//

package ru.ifmo.rain.vozleev.walk;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Paths.*;


public class Walk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Enter the name of correct input and output files!");
            return;
        }

        Path outputPath;
        try {
            outputPath = Paths.get(args[1]).getParent();
            if (outputPath != null) {
                Files.createDirectories(outputPath);
            }
        } catch (InvalidPathException e) {
            System.err.println("ERROR! Output file is invalid: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("ERROR! Can't create output file directories: " + e.getMessage());
        }

        try (
                BufferedReader inputFile = new BufferedReader(new FileReader(new File(args[0]), Charset.forName("UTF-8")));
                BufferedWriter outputFile = new BufferedWriter(new FileWriter(new File(args[1]), Charset.forName("UTF-8")))
        ) {
            String pathToFile = inputFile.readLine();
            while (pathToFile != null) {
                outputFile.write(hash32(pathToFile) + " " + pathToFile + System.lineSeparator());
                pathToFile = inputFile.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.println("ERROR! File not found: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.err.println("ERROR! Unsupported Encoding: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Input ERROR: " + e.getMessage());
        }
    }

    private static final int FNV_32_PRIME = 0x01000193;

    private static String hash32(String file) {
        try (BufferedInputStream reader = new BufferedInputStream(Files.newInputStream(get(file)))) {
            int hval = 0x811c9dc5;
            byte[] bytes = new byte[1024];
            int count;
            while ((count = reader.read(bytes)) != -1) {
                for (int i = 0; i < count; i++) {
                    hval = (hval * FNV_32_PRIME) ^ (bytes[i] & 0xff);
                }
            }

            return String.format("%08x", hval);
        } catch (IOException | InvalidPathException e) {
            System.err.println("ERROR! File not found: " + file);
            return "00000000";
        }
    }
}

