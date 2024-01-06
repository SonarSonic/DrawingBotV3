.. _drawing-area:

======================
Drawing Area
======================

In this settings panel you can configure the output size of your drawing. If you're output is for print for the best results you should enter paper size / pen width to allow DrawingBotV3 to optimise the drawing for plotting.

All changes you make to the drawing size will be automatically update the preview rendered when the "Display Mode" is set to "Image", to apply these changes to the final drawing you should run the PFM again with the **Start** button.

There are a number of presets already included which cover typical paper sizes, you can also create you own presets.

- **Use Original Sizing:** If the original images size should be used, recommended for digital only output, not recommended for print only outputs.
- **Input Units:** To specify the units for the width/height/padding, either mm, cm, inches or pixels
- **Width:** The width of the drawing area, in the current Input Units.
- **Height:** The height of the drawing area, in the current Input Units.
- **Orientation:** Either Portrait/Landscape, if you change presets your current orientation will be maintained.
- **Padding:** The margins of the drawing area, in the order Left/Right/Top/Bottom, in the current Input Units, you can also gang these controls to have a consistent padding size.
- **Scaling Mode:** How to rescale the image to fit into the new dimensions, this resizing occurs before pre-processing so will still be optimised to the paper size / pen width

  - **Crop to fit**: Crops the source image to fill the entire Drawing Area.
  - **Scale to fit**: Scales the source image so the entire image is visible within the Drawing Area.
  - **Stretch to fit**: Stretches the source image to fill the Drawing Area.

- **Rescale to Pen Width**: When activated the image will be rescaled to the specified "pen width".
- **Rescale Mode**: Controls the method used to rescale the Drawing to the specified Pen Width, affects the quality/accuracy of the final plot but also affects plotting times.

  - **High Quality**: Uses the maximum Plotting Size to create the best quality results. The PFM will use only the original image or an upscaled version as a reference.
  - **Low Quality**: Uses the optimal Plotting Size for lowest processing time. The PFM may use the original image, an upscaled version or a downscaled version depending on the given Pen Width and Width / Height.
  - **Off**: Disables the any image rescaling, the PFM will only use the original image. This is only recommended for Digital Only results. Bypassing rescaling will result in a plot with incorrect density and previews in the viewport won't match the final plot.

- **Pen Width (mm)**: The nib size to be used when plotting, this will define the resolution of the image used so where possible use smaller pens or more detailed plots. If you are using multiple sizes often the average of all sizes will give the best result or the most used size.
- **Canvas Colour:** Used for changing the canvas colour of your drawing, this is primarily a visual aid if plotting on non-white materials, however when using :ref:`colour-match` this setting is more important.
- **Background Colour:** Used for changing the background colour of the viewport area.
- **Clipping Modes:** Allows you to choose if shapes should overflow the edges of the drawing/page or not, this will only have an effect on some PFMS, as some PFMS only work within the image provided anyway. You have three options

    - **Drawing**: Clip the geometries to the drawings edges, this will prevent any shapes overlapping the edges of the drawing.
    - **Page**: Clip the geometries to the page’s edges, this will allow shapes to overlap into the padding area of the drawing, this can create a more organic style as the drawing will have a less distinct border.
    - **None**: Allow geometries to overflow the page and drawing