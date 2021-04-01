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


Path Optimisation
^^^^^^^^^^^^^^^^^^^

When enabled vector outputs (e.g. svg, pdf, gcode) will be optimised before being exported.

**Line Simplifying**: Simplifies lines using the `Douglas Peucker Algorithm <https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm>`_ which will find a similar curve/polyline with fewer points to reduce file size / plotting time within the given tolerance.

**Line Merging**: Merges start/end points within the given tolerance, this reduces the amount of pen lifts required and decreases plotting time.

**Line Filtering**: Removes lines shorter than the given tolerance

**Line Sorting**: Sorts lines to minimise air time, the algorithm will sort lines by finding the first line which starts at the same point as the current line or the first line within the given tolerance, otherwise it will use the nearest line of those which haven't already been connected.


SVG Settings
^^^^^^^^^^^^^^^^^^^

**Inkscape SVG**

**Rename layers (Pen1, Pen2...)**: by default Inkscape SVGs will use the name of the pen as the name of the layer, this will repalce those names with "Pen1", "Pen2" etc. This is especially useful if you are using the "Plot" extension with Inkscape as this requires the pen layers to be named in this format.

.. _gcode-settings:

GCode Settings
^^^^^^^^^^^^^^^^^^

GCode Settings can be saved as :ref:`presets`

- Subject to change...