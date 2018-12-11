package com.pdffiller.pdfcompare.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pdffiller.pdfcompare.comparer.Comparer;

import java.io.FileInputStream;
import java.io.InputStream;

public final class CompareService {
    public static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    public static String processPdfs(String fileName1, String fileName2) throws Exception {
        try (
                InputStream stream1 = new FileInputStream(fileName1);
                InputStream stream2 = new FileInputStream(fileName2)
        ) {
            return gson.toJson(processPdfFillableFields(stream1, stream2));
        }
    }

    public static Comparer processPdfFillableFields(InputStream pdfStream1, InputStream pdfStream2) {
        Comparer pdfCmp = new Comparer(pdfStream1, pdfStream2);
        pdfCmp.doComparison();
        return pdfCmp;
    }

    public static String processPdfs(String fileName1, String fileName2, String imageDir, double pageThreshold, double pixelThreshold, int kernelSize, boolean forceRecheck) throws Exception {
        try (
                InputStream stream1 = new FileInputStream(fileName1);
                InputStream stream2 = new FileInputStream(fileName2)
        ) {
            return gson.toJson(processPdfFillableFields(stream1, stream2, imageDir, pageThreshold, pixelThreshold, kernelSize, forceRecheck));
        }
    }

    public static Comparer processPdfFillableFields(InputStream pdfStream1, InputStream pdfStream2, String imageDir, double pageThreshold, double pixelThreshold, int kernelSize, boolean forceRecheck) {
        Comparer pdfCmp = new Comparer(pdfStream1, pdfStream2, pageThreshold, pixelThreshold, kernelSize, forceRecheck);
        pdfCmp.doComparison();
        if ((imageDir != "-") && (imageDir != "/dev/null")) {
            pdfCmp.exportCompareImages(imageDir);
        }
        return pdfCmp;
    }
}