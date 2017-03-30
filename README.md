# Drawbot_image_to_gcode_v2
This code is used to generate gcode for drawbots, polargraphs or other vertical drawing machines. \
It takes an original image, manipulates it and generates a drawing path that kinda sorta looks like the original image.
This code was specifically written to work with multiple Copic markers.
The code was intended to be heavily modified to generate different and unique drawing styles.

## Key bindings:
| Key | Description |
| ------------- |:-------------|
| r | Rotate drawing |
| [ | Zoom in |
| ] | Zoom out |
| \ | Reset drawing zoom, offset and rotation |
| O | Display original image (capital letter) |
| o | Display image to be drawn after pre-processing (lower case letter) |
| l | Display image after the path finding module has manipulated it |
| d | Display drawing with all pens |
| Q | Display drawing, pen 0 only |
| W | Display drawing, pen 1 only |
| E | Display drawing, pen 2 only |
| R | Display drawing, pen 3 only |
| T | Display drawing, pen 4 only |
| Y | Display drawing, pen 5 only |
| S | Stop path finding prematurely |
| Esc | Exit running program |
| < | Decrease the total number of lines drawn |
| > | Increase the total number of lines drawn |
| G | Generate GCode with lines as displayed |
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
| shift 0 | Decrease percentage of lines drawn by pen 0 |
| shift 1 | Decrease percentage of lines drawn by pen 1 |
| shift 2 | Decrease percentage of lines drawn by pen 2 |
| shift 3 | Decrease percentage of lines drawn by pen 3 |
| shift 4 | Decrease percentage of lines drawn by pen 4 |
| shift 5 | Decrease percentage of lines drawn by pen 5 |
| { | Change Copic marker sets, increment |
| } | Change Copic marker sets, decrement |
