.. _export-settings:

======================
Export Settings
======================

Export Modes
^^^^^^^^^^^^^^^^^

**per/drawing**: The standard option, exports the drawing as a single file.
**per/pen**: Exports a separate file for each active pen in the drawing.
**per/group**: Exports a separate file for each group in the drawing, when using Layers PFM or any Mosaic PFM
**per/n pen**: Allows drawings with multiple pens to be split into a certain number of pens. Useful when exporting for Vintage Plotters which have automatic pen changes.


Export Formats
^^^^^^^^^^^^^^^^^

**Export SVG**: Export a standard SVG which will be compatible with any software which supports SVG import, each pen will be in a separate group.

**Export Inkscape SVG**: Exports a special SVG which can be imported into Inkscape with the layers preserved.

**Export Image File**: Exports a bitmap image of the drawing, the exported image will be the scaled depending on the current "Export Resolution" PPI which is defined in Export Settings/Image & Animation, if you are using "Original Sizing" this will be the same resolution as the source image.

**Export PDF**: Exports the drawing a vector PDF file.

**Export GCode**: Exports the drawing as a GCode file, see `GCode Settings`_

**Export GCode Test Drawing**: Exports a GCode test file which draws the extremes of the drawing area.

**Export HPGL**: Exports a HP-GL (Hewlett-Packard Graphics Language) file, which can be sent to Vintage Plotters manufactured by HP, Roland and many others.

**Export Animation - Image Sequence**: Exports a image sequence animation of the drawing being created, using the same scaling as export created with *Export Image File*, see `Image Sequence Settings`_

**Export Animation - H.264**: Similar to *Export Animation - Image Sequence*, used for creating a H.264 MP4 animation of the drawing being created.

**Export Animation - ProRes 422**: Similar to *Export Animation - Image Sequence*, used for creating a ProRes MOV animation of the drawing being created.

**Export to vpype**: :ref:`vpype-settings`

-----

.. _path-optimisation:

Path Optimisation
^^^^^^^^^^^^^^^^^^^

When enabled vector outputs (e.g. svg, pdf, gcode, hpgl) will be optimised before being exported. :ref:`Path Finding Modules <pfms>` which utilise curves will bypass Path Optimization by default to avoid curve flattening.

**Line Simplifying**: Simplifies lines using the `Douglas Peucker Algorithm <https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm>`_ which will find a similar curve/polyline with fewer points to reduce file size / plotting time within the given tolerance.

**Line Merging**: Merges start/end points within the given tolerance, this reduces the amount of pen lifts required and decreases plotting time.

**Line Filtering**: Removes lines shorter than the given tolerance

**Line Sorting**: Sorts lines to minimise air time, the algorithm will sort lines by finding the first line which starts at the same point as the current line or the first line within the given tolerance, otherwise it will use the nearest line of those which haven't already been connected.

-----

SVG Settings
^^^^^^^^^^^^^^^^^^^

**Inkscape SVG**

**Rename layers (Pen1, Pen2...)**: by default Inkscape SVGs will use the name of the pen as the name of the layer, this will repalce those names with "Pen1", "Pen2" etc. This is especially useful if you are using the "Plot" extension with Inkscape as this requires the pen layers to be named in this format.

HPGL Settings
^^^^^^^^^^^^^^^^^^^

HPGL Settings can be saved as :ref:`presets`, this only includes the "Paper Size" i.e. Min, Max, X Axis Mirror and Y Axis Mirror.

All units are defined in HPGL units, which are 40 Units = 1 mm.
When adding support for your plotter you should use the ``OH;`` to get the HPGL hard-clip limits.

**Min**: The minimum X and Y hard-clip limits for the current paper size.

**Max**: The maximum X and Y hard-clip limits for the current paper size.

**X Axis Mirror**: Flips all values on the X Axis.

**Y Axis Mirror**: Flips all values on the Y Axis.

----

**X Align / Y Align**: If the drawing to be exported doesn't fill the Hard-Clip limits it will be aligned to the page using these settings.

**Rotation**: Allows you to change the orientation of your drawing within the Hard-Clip limits, it's best to leave this on AUTO which will automatically rotate the image if it extends beyound the hard-clip limits.

**Curve Flatness**: All curves are converted to lines when generating HPGL files, this value specifies the maximum distance (in MM) that the generated lines can deviate from the original curves.

**Pen Velocity**: Defines the Pen Velocity in mm/s that the plotter should be set too, a value of 0 will use the maximum speed of the plotter.

**Pen Number**: Define the first pen to use, subsequent pens will increment this number, a value of 0 will be ignored and use Pen 1.

-----

.. _gcode-settings:

GCode Settings
^^^^^^^^^^^^^^^^^^

GCode Settings can be saved as :ref:`presets`

You must specify the Drawing Area size when using GCode export.

**X/Y Offset**: The offset from HOME on each axis, in the speciied input units.

**Curve Flatness**: When enabled all curves in the drawing are converted to lines. This value specifies the maximum distance (in MM) that the generated lines can deviate from the original curves.

**Center Zero Point**: Should be enabled when using machines which treat 0,0 as the centre of the paper

**Comment Type**: DBV3 adds some comments to the generated GCode file to help differentiate outputs, however some machine require comments in a certain format to you can choose between **Brackets ()**, **Semi-Colons** or **None** to disable these comments.

**GCode - Start**: A user customisable start command

**GCode - End**: A user customisable end command

**GCode - Pen Down**: A user customisable pen down command

**GCode - Pen Up**: A user customisable pen up command

**GCode - Start Layer**: A user customisable pen down command, you can use the wildcard %LAYER_NAME% which will be replaced with the current pens name, useful if the machine can prompt the user to switch to a new pen.

**GCode - End Layer**: A user customisable pen up command, you can use the wildcard %LAYER_NAME% which will be replaced with the current pens name.



The outputted file will also include useful info such distance moved, points plotted, pen lifts.

Note: GCode export is not as reliable as SVG export and has only been added for convenience. It is not a replacement for more advanced SVG to GCode converters.

-----

.. _Image Sequence Settings:

Image Sequence Settings
^^^^^^^^^^^^^^^^^^^^^^^^^

**FPS (Frames Per Second)**: Used when calculating the amount of frames to export.

**Duration**: The length in time of the animation, can be set in Seconds, Minutes or Hours.

**Frame Count**: Always *FPS x Duration* in seconds, unless the frame count is higher than the Plotted Vertices, in which case the frame count will be the same as the number of vertices.

**Geometries per frame**: *Plotted Shapes / Frame Count*

**Vertices per frame**: *Plotted Vertices / Frame Count*

Note: The image sequence exporter using an Vertex Iterator which splits continous paths across frames, which can result in missing lines depending on the nature of the source curve.
