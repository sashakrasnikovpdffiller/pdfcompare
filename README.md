# pdfcompare

Contains comparator for pdffiles

## Requirements

sudo apt install ghostscript libgs-dev

## Arguments
* file1 file2 -- two pdf-files to compare (should be unprotected)
* (optional) image output folder -- folder to save images with differences: each page produces png-image with background from second image and overlay with different pixels. Use - (dash) or /dev/null to suppress image output
* (optional) threshold for page -- for each page difference will be calculated as ratio between total number of non-white pixels and number of different pixels. Default value - 5%. Use 0% to run strict comparison
* (optional) threshold for pixels -- for each pixel there should be calculated difference in intensity levels. Default value - 64. Use 0 to run strict comparison
* (optional) kernel size -- to set smooting kernel size, helps to smooth sub-pixel rendering issues. Default value - 5 (5\*5px kernel). Use 1 for strict pixel-to-pixel comparison.
