.. _pen-settings:

======================
Pen Settings
======================

.. image:: images/pen_settings_example_before.jpg
    :width: 250pt

.. image:: images/pen_settings_example_after.jpg
    :width: 250pt

*Created using Sketch Lines PFM & 8 different pens*

DrawingBotV3 supports the creation of multi-layer plots where every layer can have it's own custom :ref:`drawing_pen`.
The current selection of pens is referred to as the :ref:`drawing_set`.

The :ref:`Drawing Pens <drawing_pen>` and :ref:`Drawing Sets <drawing_set>` can be re-configured to experiment with combinations or to accurately match your own collection of pens.

These configurations can be saved as :ref:`presets`

-----

.. _drawing_pen:

Drawing Pens
^^^^^^^^^^^^^
Each drawing pen has the following settings.

**Enabled:** active if the should be used in the current plot, disabling it will make the pen completely inactive not just make it invisible. To view individual pens change "Display: " to "Selected Pen" and then highlight a pen in the table.

**Type:** typically the manufacturers name, or "Special" in the case of **Special Pens**

**Name**: typically the name given to the pen by the manufacturer, or the user defined name

**Colour**: the rgba colour of the pen, which can be customised

**Weight**: dictates how many shapes this specified pen should draw, a higher weight will increase the number of geometries the pen draws but decrease the amount other pens will draw, pens with equal weight will draw the same number of shapes.

**Stroke**: the pen's width *(subject to change)* currently this is relative to pixel size rather than actual pen size, unless "Rescale to Pen Width" has been enabled.

**%**: not user editable, the percentage of shapes which are being drawn by this pen

**Shape Count**: not user editable, the number of shapes which are being drawn by this pen

Default Drawing Pens
------------------------------------------
Currently the following manufacturers pen's are included as default. in the future more will be added.

-   Bic Cristal Ballpoint
-   Bic Intensity
-   Bic Intensity Pastel
-   Copic Original Markers
-   Copic Sketch Markers
-   Sakura Pigma Micron
-   Diamine Ink
-   Staedtler Fineliners
-   Winsor & Newton ProMarker

Special Drawing Pens
------------------------------------------
These Drawing Pens provide some unique style for digital only outputs

- **Original Colour**: The rendered colour will be sampled RGBA from the original image
- **Original Colour (Inverted)**: The rendered colour will be sampled RGBA from the original image with an Invert Filter applied.
- **Original Grayscale**: The same as Original Colour but grayscaled.
- **Original Grayscale (Inverted)**: The same as Original Colour (Inverted) but grayscaled.
- **Original Red**: The rendered colour will be sampled Red Channel from the original image
- **Original Green**: The rendered colour will be sampled Green Channel from the original image
- **Original Blue**: The rendered colour will be sampled Blue Channel from the original image
- **CMYK Colors**: Must be activated via :ref:`cmyk-separation`


------

.. _drawing_set:

Drawing Sets
^^^^^^^^^^^^^
A collection of Drawing Pens which can be saved as a :ref:`Preset <presets>`


Special Drawing Sets
---------------------

- **Original Colour**: A Drawing Set which includes one "Original Colour" Pen.
- **Original Grayscale**: A Drawing Set which includes one "Original Grayscale" Pen.
- **CMYK Separation**: Selecting this Drawing Set will active :ref:`cmyk-separation`

------

Distribution Type
^^^^^^^^^^^^^^^^^^^^
Affects how the shapes in the drawing are divided between all of the pens in the drawing set.
The best choice is chosen by the PFM when it is first run, but it can be changed to create unique styles.

**Even Weighted**: All active pens will draw a percentage of shapes relative to their current *weight*, the shapes are divided between pens in the order of the specified *Distribution Order*

**Random Weighted**: All active pens will draw a percentage of shapes relative to their current *weight*, the shapes are divided between pens randomly ignoring *Distribution Order*

**Random Squiggles**: Unlike classic *Random Weighted* distribution, random squiggles will only change pen when a *Squiggle* (continuous path without pen lifts) is finished, hence reducing plotting time.

**Luminance Weighted**: Distributes pens according to each shape's luminance in the original image, works especially well with non-Sketch PFMs by reintroducing multiple pens to the plot in a more aesthetically pleasing way than alternative distribution types.

**Preconfigured**: Pens are distributed by the :ref:`Path Finding Module <pfms>`. Used by :ref:`cmyk-separation` & :ref:`colour-match` and some specialist PFMs.

**Single Pen**: Draw all lines with the first active pen in the list according to the *Distribution Order*

------

Distribution Order
^^^^^^^^^^^^^^^^^^^^
Dictates the order in which shapes are distributed between pens, it also has the affect of changing the render order of the pens.

**Darkest First (Default)**: The shapes generated by the :ref:`Path Finding Module <pfms>` will be distributed to the pens from Darkest Pen to Lightest Pen. As the first lines drawn by Sketch PFMs are always the darkest lines in the image this will typically produces the best results and is therefore the default.

**Lightest First**: The reverse of Darkest First, maps the shapes from the Lightest Pen to Darkest Pen

**Displayed**: The shapes generated by the :ref:`Path Finding Module <pfms>` will be distributed to the pens from Top Pen to Bottom Pen in the displayed list. This can create some interesting results where the tonal ranges of the image are mixed.

**Reversed**: The reverse of Displayed, maps the shapes from Bottom Pen to Top Pen in the displayed list.

------

Colour Separation
^^^^^^^^^^^^^^^^^^^^
 - **Default**: The colour of each pen is chosen after processing by the user, and pens are selected based on Distribution Type and Distribution Order
 - **CMYK**: Runs the drawing multiple times for the 4 standard print colours Cyan, Magenta, Yellow & Black (Key), perfect for creating colour plots with few pens. Read more here :ref:`cmyk-separation`
 - **Colour Match**: Allows you to represent the colours in the original image using pens of your choice. Read more here :ref:`colour-match`

------

Blend Modes
^^^^^^^^^^^^^^^^^^^^
This is purely a rendering option and has no effect on the final plot, it can be used to create more accurate previews in specific circumstances or if you're art will only be digital this can be fun way to play with the colours and blending of lines.

Note: There is a known bug where some blend modes will not be exported properly when rendering an image file.
