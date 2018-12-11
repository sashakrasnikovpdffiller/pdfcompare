package com.pdffiller.pdfcompare.comparer;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

import com.google.gson.annotations.Expose;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;


public class Comparer {
    // Documents to work
    private PDFDocument exportPdf1;
    private PDFDocument exportPdf2;

    // Base constants
    public static int KernelSize = 5; // Size of blurring window

    public static double grayscaleDifference = 64; // Grayscale difference upper limit to count as difference
    public static final double grayscalePixelDarkLevel = 224; // Grayscale pixel upper limit to count as visible pixel
    public static double singlePageLimit = 7.5; // Upper difference limit for single page
    public static double allPagesLimit = 5; // Upper difference limit for multiple pages
    public static boolean forceRecheck = false; // Force recheck for files with different number of pages

    public static int differenceColor = 0xFFFF00FF; // Color to mark different pixels

    // State names - ideally it has to be SAME_CONTENT
    public static final String PROCESSING = "Comparison in process / unfinished";
    public static final String DIFFERENT_PAGE_COUNT = "Number of pages in pdf doesn't match";
    public static final String DIFFERENT_PAGE_CONTENT = "Pages content doesn't match";
    public static final String SAME_CONTENT = "All the same";
    public static final String ERROR = "Error in processing: see error message";

    // Processing results
    @Expose
    public boolean result = false; // Final result
    @Expose
    public String state = Comparer.PROCESSING; // State of processing
    @Expose
    public String errorMsg = ""; // Error message if there was any
    @Expose
    public double comparisonTotal;
    @Expose
    public List<Double> comparisonData; // Data for each page
    public List<Image> comparisonImages; // Images with marked differences for each page

    public Comparer(InputStream pdf1, InputStream pdf2) {
        try {
            // Open pdf
            this.exportPdf1 = new PDFDocument();
            this.exportPdf1.load(pdf1);
            this.exportPdf2 = new PDFDocument();
            this.exportPdf2.load(pdf2);
        } catch (Exception e) {
            this.state = Comparer.ERROR;
            this.errorMsg = e.getMessage();
        }
    }

    public Comparer(InputStream pdf1, InputStream pdf2, double pageThreshold, double pixelThreshold, int kernelSize, boolean forceRecheck) {
        try {
            // Open pdf
            this.exportPdf1 = new PDFDocument();
            this.exportPdf1.load(pdf1);
            this.exportPdf2 = new PDFDocument();
            this.exportPdf2.load(pdf2);

            this.singlePageLimit = pageThreshold * 1.5;
            this.allPagesLimit = pageThreshold;
            this.grayscaleDifference = pixelThreshold;
            this.KernelSize = kernelSize;
            this.forceRecheck = forceRecheck;
        } catch (Exception e) {
            this.state = Comparer.ERROR;
            this.errorMsg = e.getMessage();
        }
    }

    public boolean doComparison() {
        try {
            // Check page number
            if ((this.exportPdf1.getPageCount() != this.exportPdf2.getPageCount()) & !this.forceRecheck) {
                this.state = Comparer.DIFFERENT_PAGE_COUNT;
                return this.result;
            }
            // Render for pdf-files from ghost4j
            SimpleRenderer renderer = new SimpleRenderer();
            renderer.setResolution(144);

            // Smoothing/blurring operations prepare
            float[] kernelValues = new float[KernelSize*KernelSize];
            Arrays.fill(kernelValues, 1f / (KernelSize*KernelSize));
            Kernel kernel = new Kernel(KernelSize, KernelSize, kernelValues);
            BufferedImageOp blurring = new ConvolveOp(kernel);

            // Render pdf to list of images
            List<Image> pdfImages1 = renderer.render(this.exportPdf1);
            List<Image> pdfImages2 = renderer.render(this.exportPdf2);

            this.comparisonImages = new ArrayList<>();
            this.comparisonData = new ArrayList<>();

            double totalDifferentPixels = .0;
            double totalPixels = .0;

            for (int pageIdx = 0; pageIdx < this.exportPdf1.getPageCount(); pageIdx++) {
                // Image with differences will be based on first pdf renders
                BufferedImage compareResult = (BufferedImage)pdfImages1.get(pageIdx);

                // Prepare buffered images to save blurred results
                BufferedImage pdfBlur1 = new BufferedImage(
                        pdfImages1.get(pageIdx).getWidth(null),
                        pdfImages1.get(pageIdx).getHeight(null),
                        BufferedImage.TYPE_BYTE_GRAY);
                BufferedImage pdfBlur2 = new BufferedImage(
                        pdfImages2.get(pageIdx).getWidth(null),
                        pdfImages2.get(pageIdx).getHeight(null),
                        BufferedImage.TYPE_BYTE_GRAY);
                // Blurring
                blurring.filter((BufferedImage)pdfImages1.get(pageIdx), pdfBlur1);
                blurring.filter((BufferedImage)pdfImages2.get(pageIdx), pdfBlur2);

                // Check width of compared pages to omit cropping problems
                int imgWidth = Math.min(pdfBlur1.getWidth(), pdfBlur2.getWidth());
                int imgHeight = Math.min(pdfBlur1.getHeight(), pdfBlur2.getHeight());

                // Save state if renders have different size
                boolean pdfSameSize = (pdfBlur1.getWidth() == pdfBlur2.getWidth()) && (pdfBlur1.getHeight() == pdfBlur2.getHeight());

                // Counters for pixels in pdfs and different pixels between pdfs
                int pdfPix1 = 0;
                int pdfPix2 = 0;
                int diffPix = 0;

                Color pdfColor1;
                Color pdfColor2;
                for (int xPos = 0; xPos < imgWidth; xPos++) {
                    for (int yPos = 0; yPos < imgHeight; yPos++) {
                        // Take colors for same pixels compare and count difference if there is any
                        pdfColor1 = new Color(pdfBlur1.getRGB(xPos, yPos));
                        pdfColor2 = new Color(pdfBlur2.getRGB(xPos, yPos));
                        if (pdfColor1.getRed() < Comparer.grayscalePixelDarkLevel) {
                            pdfPix1++;
                        }
                        if (pdfColor2.getRed() < Comparer.grayscalePixelDarkLevel) {
                            pdfPix2++;
                        }
                        if (Math.abs(pdfColor1.getRed() - pdfColor2.getRed()) > Comparer.grayscaleDifference) {
                            compareResult.setRGB(xPos, yPos, Comparer.differenceColor);
                            diffPix++;
                        }
                    }
                }

                // Difference: percent of different pixels between pdfs to average number of pixels in both pdfs
                double differencePercent = ((pdfPix1 + pdfPix2) > 0) ? 200f * diffPix / (pdfPix1 + pdfPix2) : 0;

                // Save results
                comparisonImages.add(compareResult);
                comparisonData.add(differencePercent);
                totalDifferentPixels += differencePercent*(pdfPix1 + pdfPix2);
                totalPixels += (pdfPix1 + pdfPix2);

                // Check single page
                if (differencePercent > Comparer.singlePageLimit) {
                    this.state = Comparer.DIFFERENT_PAGE_CONTENT;
                }
            }

            comparisonTotal = totalDifferentPixels / totalPixels;
            // Check total weighted difference
            if (comparisonTotal  > Comparer.allPagesLimit) {
                this.state = Comparer.DIFFERENT_PAGE_CONTENT;
            }

            if (Comparer.PROCESSING.equals(this.state)) {
                this.state = Comparer.SAME_CONTENT;
            }

            this.result = Comparer.SAME_CONTENT.equals(this.state);
        } catch (Exception e) {
            this.state = Comparer.ERROR;
            this.result = false;
            this.errorMsg = e.getMessage();
        }
        return this.result;
    }

    // Save all pictures to provided folder
    public boolean exportCompareImages(String exportPath) {
        try {
            File pathDir = new File(exportPath);
            if (!(pathDir.exists() && pathDir.isDirectory())) {
                pathDir.mkdir();
            }
            for (int i = 0; i < comparisonImages.size(); i++) {
                ImageIO.write((RenderedImage) comparisonImages.get(i), "png", new File(exportPath, (i + 1) + ".png"));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
