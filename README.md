# Drawbot_image_to_gcode_v2
<img src="pics/github1.png" height="411" alt="Drawbot_image_to_gcode_v2 example output"/>

This code is used to generate gcode for drawbots, polargraphs or other vertical drawing machines. \
It takes an original image, manipulates it and generates a drawing path that kinda sorta looks like the original image. \
This code was specifically written to work with multiple Copic markers. \
The code was intended to be heavily modified to generate different and unique drawing styles.

If your clipboard contains a URL to an image, the code will download it.  This makes finding usable images easy and straight forword.
If your clipboard contains a URL to a web site, the code will crash and burn in a spectacular fashion.

## Key Bindings:
| Key | Description |
| ------------- |:-------------|
| p | Load next "Path Finding Module" (PFM) |
| r | Rotate drawing |
| [ | Zoom in |
| ] | Zoom out |
| \ | Reset drawing zoom, offset and rotation |
| O | Display original image (capital letter) |
| o | Display image to be drawn after pre-processing (lower case letter) |
| l | Display image after the path finding module has manipulated it |
| d | Display drawing with all pens |
| <ctrl> 1 | Display drawing, pen 0 only |
| <ctrl> 2 | Display drawing, pen 1 only |
| <ctrl> 3 | Display drawing, pen 2 only |
| <ctrl> 4 | Display drawing, pen 3 only |
| <ctrl> 5 | Display drawing, pen 4 only |
| <ctrl> 6 | Display drawing, pen 5 only |
| <ctrl> 7 | Display drawing, pen 6 only |
| <ctrl> 8 | Display drawing, pen 7 only |
| <ctrl> 9 | Display drawing, pen 8 only |
| <ctrl> 0 | Display drawing, pen 9 only |
| S | Stop path finding prematurely |
| Esc | Exit running program |
| < | Decrease the total number of lines drawn |
| > | Increase the total number of lines drawn |
| g | Generate all gcode, SVGs, and PDFs with lines as displayed |
| G | Toggle grid |
| t | Redistribute percentage of lines drawn by each pen evenly |
| y | Redistribute 100% of lines drawn to pen 0 ( Black/White/Sharpie ) |
| 9 | Change distribution of lines drawn (lighten) |
| 0 | Change distribution of lines drawn (darken) |
| 1 | Increase percentage of lines drawn by pen 0 |
| 2 | Increase percentage of lines drawn by pen 1 |
| 3 | Increase percentage of lines drawn by pen 2 |
| 4 | Increase percentage of lines drawn by pen 3 |
| 5 | Increase percentage of lines drawn by pen 4 |
| 6 | Increase percentage of lines drawn by pen 5 |
| 7 | Increase percentage of lines drawn by pen 6 |
| 8 | Increase percentage of lines drawn by pen 7 |
| 9 | Increase percentage of lines drawn by pen 8 |
| 0 | Increase percentage of lines drawn by pen 9 |
| shift 0 | Decrease percentage of lines drawn by pen 0 |
| shift 1 | Decrease percentage of lines drawn by pen 1 |
| shift 2 | Decrease percentage of lines drawn by pen 2 |
| shift 3 | Decrease percentage of lines drawn by pen 3 |
| shift 4 | Decrease percentage of lines drawn by pen 4 |
| shift 5 | Decrease percentage of lines drawn by pen 5 |
| shift 6 | Decrease percentage of lines drawn by pen 6 |
| shift 7 | Decrease percentage of lines drawn by pen 7 |
| shift 8 | Decrease percentage of lines drawn by pen 8 |
| shift 9 | Decrease percentage of lines drawn by pen 9 |
| shift 0 | Decrease percentage of lines drawn by pen 0 |
| { | Change Copic marker sets, increment |
| } | Change Copic marker sets, decrement |


Examples of drawings made with this software:  http://dullbits.com/drawbot/gallery
