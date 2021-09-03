.. _export-settings:

======================
Export Settings
======================

Export Formats
^^^^^^^^^^^^^^^^^

All formats can be exported **per/drawing** or **per/pen**

**Export SVG**: Export a standard SVG which will be compatible with any software which supports SVG import, each pen will be in a separate group.

**Export Inkscape SVG**: Exports a special SVG which can be imported into Inkscape with the layers preserved.

**Export Image File**: Exports a bitmap image of the drawing, the same resolution as the source image.

**Export PDF**: Exports the drawing a vector PDF file.

**Export GCode**: Exports the drawing as a GCode file, see `GCode Settings`_

**Export GCode Test Drawing**: Exports a GCode test file which draws the extremes of the drawing area.

**Export Image Sequence**: Exports a bitmap image sequence of the drawing, with the same resolution as the source image, see `Image Sequence Settings`_

**Export to vpype**: :ref:`vpype-settings`

-----

.. _path-optimisation:

Path Optimisation
^^^^^^^^^^^^^^^^^^^

When enabled vector outputs (e.g. svg, pdf, gcode) will be optimised before being exported. :ref:`Path Finding Modules <pfms>` which utilise curves will bypass Path Optimization by default to avoid curve flattening.

**Line Simplifying**: Simplifies lines using the `Douglas Peucker Algorithm <https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm>`_ which will find a similar curve/polyline with fewer points to reduce file size / plotting time within the given tolerance.

**Line Merging**: Merges start/end points within the given tolerance, this reduces the amount of pen lifts required and decreases plotting time.

**Line Filtering**: Removes lines shorter than the given tolerance

**Line Sorting**: Sorts lines to minimise air time, the algorithm will sort lines by finding the first line which starts at the same point as the current line or the first line within the given tolerance, otherwise it will use the nearest line of those which haven't already been connected.

-----

SVG Settings
^^^^^^^^^^^^^^^^^^^

**Inkscape SVG**

**Rename layers (Pen1, Pen2...)**: by default Inkscape SVGs will use the name of the pen as the name of the layer, this will repalce those names with "Pen1", "Pen2" etc. This is especially useful if you are using the "Plot" extension with Inkscape as this requires the pen layers to be named in this format.

-----

.. _gcode-settings:

GCode Settings
^^^^^^^^^^^^^^^^^^

GCode Settings can be saved as :ref:`presets`

You must specify the Drawing Area size when using GCode export.

**X/Y Offset**: The offset from HOME on each axis, in mm

**GCode - Start**: A user customisable start command

**GCode - End**: A user customisable end command

**GCode - Pen Down**: A user customisable pen down command

**GCode - Pen Up**: A user customisable pen up command

**GCode - Start Layer**: A user customisable pen down command

**GCode - End Layer**: A user customisable pen up command



GCode outputs use brackets for comments. The outputted file will also include useful info such distance moved, points plotted, pen lifts.

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
