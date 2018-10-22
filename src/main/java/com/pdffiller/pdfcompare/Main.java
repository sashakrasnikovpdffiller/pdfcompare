package com.pdffiller.pdfcompare;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.pdffiller.pdfcompare.service.CompareService;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        try {
            if (args.length < 2) {
                throw new IllegalArgumentException("Bad command line. Expected arguments: file1 file2 [image directory] [page threshold] [pixel threshold] [kernel size]\n" +
                        "file1 and file2 have to be unprotected pdf-files\n" +
                        "image directory -- optional directory to output images with differences, use - or /dev/null to skip image creation\n" +
                        "page threshold -- allowed difference percent, default = 5% of pixels\n" +
                        "pixel threshold -- intensity difference, default = 64 of 256, use 0 for exact match\n" +
                        "kernel size -- window size to do gaussian smoothing, default = 5 px, no smoothing = 1 px");
            }

            String file1 = args[0];
            String file2 = args[1];
            double pageThreshold = (args.length >= 4) ? Double.parseDouble(args[3]) : 5;
            double pixelThreshold = (args.length >= 5) ? Double.parseDouble(args[4]) : 64;
            int kernelSize = (args.length >= 6) ? Math.max(Integer.decode(args[5]), 1) : 5;
            String imageDir = (args.length >= 3) ? args[2] : "-";
            String json;

            json = CompareService.processPdfs(file1, file2, imageDir, pageThreshold, pixelThreshold, kernelSize);
            System.out.print(json);
            writeStringToFile(json,file2.substring(0,file2.lastIndexOf("."))+".json");
        } catch (Exception e) {
            System.out.println("Unexpected exception:" + e.toString());
        }
    }

    private static void writeStringToFile(String text, String filename) throws IOException {
        File file = new File(filename);
        file.createNewFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        }
    }
}
