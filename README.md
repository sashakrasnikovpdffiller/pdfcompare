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

## Output 
JSON with results
```{"result":false,"state":"Pages content doesn't match","errorMsg":"","comparisonTotal":57.670885933896244,"comparisonData":[25.579998016357422,64.3471908569336,61.99942398071289,77.02323150634766]}```

* result - bool - true if pdf-files same, false if pdf-files different
* state - string - details for result (Number of pages in pdf doesn't match // Pages content doesn't match // All the same // Error in processing: see error message)
* errorMsg - string - error message if exception occured
* comparisonTotal - double (optional) - average difference percent, won't be calculated if files have different number of pages
* comparisonData - array of doubles (optional) - difference percent for each page, won't be calculated if files have different number of pages
