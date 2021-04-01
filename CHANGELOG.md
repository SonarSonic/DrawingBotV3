# Change Log

### [v1.0.10-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.10-beta)
- Added: DrawingBotV3 now has [documentation](https://drawingbotv3.readthedocs.io)!
- Added: The name of the current image file will be displayed in the title of the window.
- Changed: Line density's maximum value will now result in target brightness of 253.5, not 250
- Fixed: Inaccuracies in plotting image sections smaller than the line length.
- Fixed: Progress updates when exporting files

### [v1.0.9-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.9-beta)
- Added: New Presets for "Sketch Squares" - "Waves" & "Triangles"
- Added: CMYK Seperation is now multi-threaded & and gives an accurate live preview
- Added: If the "CMYK Drawing Set" is selected & CMYK isn't activated a dialog will appear to apply the settings automatically.
- Changed: CMYK Seperation will now use opaque pens on some PFMs.
- Fixed "Sketch Squares" PFM it will now work as expected and have different styles to "Sketch Lines PFM"
- Fixed "Circle Size" not working as expected in "Voronoi Circles" + prevented small circles being invisible.

### [v1.0.8-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.8-beta)
- Added: Five New Path Finding Modules!
    - "Sketch Sobel Edges PFM" - Works like the "Sketch Lines PFM" but uses Edge Detection as well as Brightness Sampling, which can be adjusted to create stylised drawings with emphasised contours.
    - "Voronoi Triangulation" - Draws triangles between the centre points of a Weighted Voronoi Diagram.
    - "Voronoi Stippling" - Draws filled circles at the centre points of a Weighted Voronoi Diagram, each circle is scaled to the average brightness of the containing cell
    - "Voronoi Circles" - Draws inscribed circles into the cells of a Weighted Voronoi Diagram
    - "Voronoi Diagram" - Draws the Voronoi Diagram by itself.
- Added: New "Rescale to Pen Width" option in "Drawing Area" - which helps in optimising the line density of your plots, you must enter the "width" & "height" of the drawing area for this to be applied. 
- Added: New Special Pens for use for screen only outputs "Original Red" , "Original Green", "Original Blue"
- Added: New Pen Distribution Type Setting, options include "Even", "Even Weighted (Default)", "Random", "Random Weighted", "Preconfigured", "Single Pen" - Path Finding Modules will select there recommended distribution type the first time they are run.
- Added: New "Rotate" button in Drawing Area to quickly swap Width / Height.
- Added: "Centre X", "Centre Y" & "Spiral Size" to "Spiral PFM", which allows you to move the start position of the spiral and change it's size.
- Changed: The rendering quality of lines created from low quality images has been improved massively, the images will look the same, exports will still be lower res / match the input resolution.
- Changed: The default preset for "Sketch Lines PFM" now has shading disabled by default, there is a new preset called "Simple Shading" which performs like the original.
- Changed: "Lock" has been renamed to "Randomise Exclude" and hidden by default, as it was misleading for new users and is rarely needed.
- Changed: Renamed setting "Desired brightness" to "Line density", instead of being an arbitary number, the images brightness/density is a percentage where any value is valid the default is 75%
- Changed: Renamed setting "Enable Shading" to "Shading", it behaves the same
- Changed: Renamed setting "Squiggle to shading" to "Shading Threshold", instead of being an arbitary number shading now kicks in when the processing has passed the specified percentage default is 50%.
- Changed: Renamed setting "Distance between rings" to "Ring Spacing", it behaves the same
- Changed: The settings for "Spiral PFM" now have more logical maximums.
- Fixed: Spiral PFM rendering the spiral off-screen.
- Fixed: "Min Line Length" would not always be respected and shorter lines would still be created, this has been fixed and so the defaults of some presets have also changed.
- Fixed: Rounding errors resulting in a less than uniform distribution of random angles, affected most path finding modules.
- Fixed: Line merging in Path Optimisation will now work more like expected.
- Fixed: Issue where completely black would not show up in the SVG output.
- Fixed: Image Rendering not updating when "Width" was changed.
- Note: If you have created any presets in older versions, they may break in this version.

### [v1.0.7-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.7-beta)
-  Added: New Path Finding Module! "Sketch Curves PFM"
   - Performs the same as the default, but without shading and with Catmull Rom Splines instead of lines, you can configure the tension of the curves.
-   Added: Automatic SVG Optimisation, Line Simplification / Line Merging / Line Sorting
    - Can be configured in File / Export Settings / Path Optimisation
-   Added: Export Settings tab, to configure Path Optimisation / SVG Settings / GCode Settings
-   Added "Export Inkscape SVG" Option which supports Inkscape layers but might not work in other applications.
-  Added: New option "Rename layers (Pen1, Pen2...)" for Inkscape SVGs (compatible with the "Plot" function in Inkscape)
-  Added: "Filters" to the Menu Bar to allow for adding filters quickly.
-  Changed: GCode Export Settings have moved into the new "Export Settings tab"
-  Changed: Re-ordered export settings to better reflect file types used by plotters.
-  Fixed: Changing the Plotting Resolution will not affect the visual quality of the render and line size will be consistent
-  Fixed: Path Finding Modules will no longer gravitate to the corners of the image and will trace drawings more accurately.

### [v1.0.6-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.6-beta)
-  Added: CMYK Colour Seperation / Special Drawing Sets - Print results will vary!
-  Added: Multi-Layer SVGs, SVGs now have seperate groups for each pen
-  Added: "Max Line Limit" option to PFM Sketch & PFM Squares
-  Added: Import / Export will now store the last used locations.
-  Added: "Open Configs Folder" option in the help menu.
-  Fixed: Massive Lag Spikes / the program becoming unresponsive
-  Fixed: Lag Spikes when changing to Special Drawing Sets / Changing Drawing Mode
-  Fixed: Pen stoke sizes will match custom values properly on exported SVGs
-  Fixed: Custom pen colours not rendering with the new colour / not saving when pressing "use"
-  Fixed: Logs not outputting correctly

### [v1.0.5-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.5-beta)
- Added: New Image Filters, there are now over 60+ Image Filters!
- Added: Presets for Drawing Areas (A4 Paper / A3 Paper etc.) & GCode Settings
- Added: "Image" Display Mode, to view the imported image.
- Changed: Filters / Cropping will now update live in the "Image" display mode.
- Changed: Image exports now have Anti-Aliasing so will match the viewport more closely
- Changed: The viewport now has a max resolution of 4096 x 4096, exceeding this size can prevent some GPUs from working, image exports will still match the resolution of the input.
- Changed: Increased the maximum vram usage from 512MB to 1024 MB
- Fixed: SVG Outputs will now match the specified dimensions and will use a DPI of 96
- Fixed: The application hanging when importing large images
- Fixed: Issue with high resolution images plotting endlessly

### [v1.0.4-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.4-beta)
- Added: Presets for Drawing Sets + Drawing Pens
  - Presets created for Drawing Sets / Pens will be saved to the "User" type.
- Added: Special Drawing Sets/Pens Original Colour / Original Grayscale
  - These will sample the average colour of the image and colour each line using these samples.
  - These samples are taken when the plot is being run and cached, this will slightly impact plotting performance, this caching can be disabled however by setting PFM setting "Cache Colour Samples" to "false".
- Added: "Padding" option to Drawing Area, with "Gang" check box
- Added: "Scaling Mode" option to Drawing Area: "Crop to fit", "Scale to fit" and "Stretch to fit"
- Added: "Lock" option for PFM Settings
  - Locking a setting will prevent it from being randomised, this will allow more creativity when using "randomise" allowing you to keep track of values you like.
  - Some settings are locked by default to prevent the plot failing (e.g. Plotting Resolution, Drawing/Shading Delta Angle)
  - Pressing "Reset" will still return the setting to default
- Added: Right-Clicking PFM Settings will now allow you to "Randomise" / "Reset" a single setting.
- Added: Check boxes to Drawing Pen selections to enable quicker configuration
- Added: Extra Buttons in Pen Settings for "Add", "Remove", "Duplicate", "Move Up" and "Move Down", you can still right click pens instead.
- Added: "Stroke" options for Drawing Pens to change the thickness of the
- Changed: Pressing ENTER will not start plotting anymore, avoiding accidentally overwriting your current plot.
- Changed: Pre-Processing will now be disabled by default
- Changed: Blend Modes won't lag so much when rendering
- Changed: Preset files have been overhauled to allow for the easy addition of even more preset types.
- Changed: GCode Settings has been split from "Drawing Area" and now appear at the bottom
- Changed: GCode Exporting has been re-written.
- Changed: GCode "Test Drawings" will now respect the enabled pens / allow seperate pen tests
- Changed: Removed "Import URL" option
- Changed: When exporting the default will now be the original files folder and name with the suffix "_plotted"
- Fixed: JPG Export not outputting a file
- Fixed: SVG exporting with individual line segments and consequently creating large output files.
- Fixed: Potential Memory Leaks with JavaFX
- Fixed: Blend Modes will render as expected again & also appear properly in exports.
- Note: Old user presets / presets will be broken, presets will need to transferred to the new json format (this won't happen again)

### [v1.0.3-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.3-beta)
- Changed: The "Reset" / "Help" button has been swapped to help avoid accidental usage
- Fixed: JavaFX Native Bindings have been added, so Mac + Linux users should now be able to run the app as expected.

### [v1.0.2-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.2-beta)
- Changed: DrawingBotV3 is now for Java 11+
- Removed: Dependency on Processing
- Fixed: Alpha channels being ignored by Path Finding Modules

### [v1.0.1-alpha](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.1-alpha)
- Added: User Configurable Presets which can be imported/exported and shared with other users
- Added: Default Path Finding Module Presets
  - inc. "Sketchy", "Glitchy Vertical, "Glitchy Horizontal" & "Messy Lines"
- Added: Default Pre-Processing Presets
  - inc. "Original Dirty Border"
- Added: Pre-Processing Settings, for adding/removing image filters
- Added: Large amounts of work on the API
- Changed: Improved application log output
- Changed: By default images will no longer have a border, use the pre-processing preset "Original Dirty Border" to replicate the original defaults.
- Changed: Combined the two Path Finding Control tabs into one smaller one
- Changed: Moved the Elapsed Time & Plotted Lines values to above the viewport
- Fixed: The application appearing to big on small screens.
- Fixed: The console log not being outputted properly
- Fixed: "Reset Plotting" button, this will now work more consistently and will try to force plotting to stop regardless of where the process is, the plotting will have to be restarted.
- Note: The "Pre-Processing" tab is still W.I.P.

### [v1.0.0-alpha](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.0-alpha)
- The first public alpha!
