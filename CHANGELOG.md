### [v1.6.8-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.8-beta-free)
- Improved: Initial start speed of LBG PFMs
- Improved: MacOS - OpenGL Renderer Quality/Speed Improvements
- Improved: HPGL / GCode Export error reporting
- Fixed: PFMs Failing to run in some situations with "could not initialize class" errors
- Fixed: MacOS OpenGL Renderer not initializing on some operating systems.
- Fixed: OpenGL Renderer failing to initialize in Full Screen
- Fixed: Import/Export Dialogs not appearing when the previous location is unavailable.
- Fixed: HPGL / GCode Export failing to start in some situations on MacOS

### [v1.6.7-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.7-beta-free)
- Added: **Group** option for Masks, right-click a mask in the table with multiple selected to create a new grouped mask.
- Added: **Link Square Paths** & **Link Circle Paths** to Pen Calibration PFM, reducing pen lifts when running tests.
- Added: **Heart** as a new Shape and Mask Type.
- Added: Support for Proxies when running with a Corporate Firewall.
- Improved: UI Scaling on Pen Settings and Batch Processing panels.
- Fixed: Colour Match failing to finish with "*Out of range*".
- Fixed: Catmull-Roms PFM failing to finish with "*testData is null*".
- Fixed: Duplicating masks creating an additional invisible mask.
- Fixed: SVG Converter PFM handling empty paths incorrectly, resulting in export failures.
- Fixed: SVG Converter PFM ignoring the *Pen Limit* when using *Colour Match*

### [v1.6.6-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.6-beta-free)
- Added: **2 New PFMs**: 
  - **Sketch Flow Fields** - Uses a Flow Field to direct the generated paths.
  - **Sketch Superformula** - Uses [Superformula](https://en.wikipedia.org/wiki/Superformula) to produce sketch plots which are driven by radial patterns, such as stars, vortexes and ellipses. You can use examples shown on the Superformula wiki by using the examples **m**, **n1**, **n2** and **n3** for  **Frequency**, **Curvature**, **Sine Factor** and **Cos Factor** respectively
- Added: **CMYK (Extended Gamut)** - A new color separation option which extends the stand CMYK range to include Orange, Green, Violet, Red and Blue. It can create plots with even more vibrant colors and more colour accurate result than standard CMYK. You can even add your own pens to the color separation, allowing for even more accurate results, it is best to still keep the main CMYK pens as part of the plot.  
- Added: **2 New Distribution Types** 
  - **Random Squiggles** - Unlike the classic *Random* distribution, random squiggles will only change pen when a *Squiggle* (continuous path without pen lifts) is finished.
  - **Luminance Weighted** - Distributes pens according to each shape's luminance in the original image, works especially well with non-Sketch PFMs by reintroducing multiple pens to the plot in a more aesthetically pleasing way than alternative distribution types. Try it out with Adaptive Shapes!
- Added: **New CMYK Settings**
  - **Clean Black** - Prevents areas of complete black being drawn over by non-black pens, resulting in a cleaner result with less ink wasted.
  - **Clean White** - Works in a similar way to Clean Black, and protects white areas from being drawn over by off-white pens.
  - **Vibrancy** - Controls the overall intensity for the Extended Gamut colors (Orange, Green, Violet, Red and Blue)
  - **Reduce Overlay** - Controls the amount pens are allowed to overlap in the final result.
- Improved: New CMYK Separation configuration UI, to allow more advanced configurations.  
- Improved: CMYK Settings can now be saved along with Drawing Set presets.
- Improved: Serial Connection progress will now show high values in hours/min/sec and not just min/sec
- Fixed: Notifications not appearing
- Fixed: Anchor point positioning when rotating mask shapes with shift held down.
- Fixed: Elapsed time flickering when hours / minutes reached 0

### [v1.6.5-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.5-stable-free)
- Improved: RAM Usage has been reduced in many situations and the general responsiveness of the application improved.
- Improved: Sketch PFMs have been further optimised, with speed improvements up to 2x.
- Improved: Sketch Sweeping Curves especially now runs considerably faster.
- Improved: Optimised the Hardware Accelerated renderer and improved curve quality.
- Improved: Updated Bic Pens and Drawing Set Definitions & Colours -led
- Improved: Moved baked GCode into default GCode preset, to allow more custom GCode.
- Improved: Drawings which take less than a second will now show elapsed time in milliseconds.  
- Fixed: Memory Leak when "Auto Run PFM" is enabled, resulting in slow down of the application.
- Fixed: "Plotting Resolution" breaking on multiple PFMs
- Fixed: "Plotting Size" not showing the correct dimensions when using padding -led
- Fixed: "Preferences" config not saving properly on shutdown in some situations

### [v1.6.4-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.4-beta-free)
- Added: **3 New PFMs**: **Grid Shapes**, **Grid Dashes** and **Grid Letters**, more details [here](https://docs.drawingbotv3.com/en/latest/pfms.html#grid-pfms)
- Added: Documentation Integration
  - Go to **Help / Open Documentation** - to open the documentation in-app
  - In the **Path Finding Controls** window, select **Help**, to display a specific PFM's settings.
  - Select **Any PFM Setting** while the documentation window is open, to bring up the specific setting's definition.
- Added: You can now check for new updates in software by going to **Help / Check For Updates**, if an update is available a link will be provided.
- Added: Option to specify a default Import / Export folder for new projects.
- Added: Invert Colour Option too Colour Match.  
- Added: Keyboard Shortcut *Shift + V* - Creates a new version.
- Improved: Saved Projects will save the last used Import / Export Folder.
- Improved: Layout / Appearance of *Export Stats* and moves/pen lifts will now be placed in order making the shapes slider more useful in Exported Drawing mode, shown when exporting vector files  
- Improved: Visual appearance of *Notifications*
- Improved: "Reset" & "Stop" responsiveness on Voronoi PFMs
- Improved: Mosaic PFMs will now render their progress in a more useful way, without restarting after each tile is processed.
- Improved: Performance of transformations on SVG masks
- Improved: *Sketch Shapes* PFM now supports all Style parameters.
- Fixed: Mosaic + Layers PFM - Drawing Styles not loading/saving properly.  
- Fixed: CMYK Pens not exporting with the correct Opacity value.
- Fixed: Export Stats displaying the wrong units.
- Fixed: Image Cropping not loading properly when re-opening a project / switching to another open project.  
- Fixed: Drawings / Images not being refreshed properly when switching between two open projects.  
- Fixed: Voronoi PFMs continuing to run the background after being reset.  
- Fixed: Log Files being incomplete.
- Fixed: Project files saving with the wrong extension in some situations.
- Fixed: Stipple Size having no effect on Stipple PFMs

### [v1.6.3-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.3-beta-free)
- Added: New Presets "Messy TSP", "Hatchy Squiggles" &  "Glitchy Stars" - for Adaptive/LBG Circular Scribbles & "Light Crosshatch" for Layers PFM
- Improved: 'Default' & 'Detailed Scribbles' & 'Sketch' - Circular Scribbles Presets
- Improved: More improvements to the quality of Circular Scribbles, transitions between different radii will be smoother.  
- Changed: The default "Angular Velocity" for Adaptive Circular Scribbles & LBG Circular Scribbles is 60 instead of 20.
- Fixed: Using CMYK with Mosaic PFMS.
- Fixed: CMYK Processing failing to finish properly with "Coordinate out of bounds"
- Fixed: SVG Converter PFM not scaling properly and sometimes moving elements of the SVG.
- Fixed: Drawings created with the SVG Converter PFM not keeping the SVGs original size when exported.
- Fixed: Loading Projects when the original image can't be located would not load the chosen replacement image.
- Fixed: "Star" and "Triangle" shapes missing rotation when using Shapes PFMs
- Fixed: Loading Projects ignored the Drawing Area orientation.
- Fixed: Drawings being re-rendered when exporting non-vector files.
- Fixed: Drawings being re-generated when creating new preset files.

### [v1.6.2-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.2-beta-free)
- Added: New presets "Single Pen Preset V1" for Sketch Sweeping Curves and "Distorted Waves" for Sketch Waves
- Added: "Cache Result" option to all LBG PFMs, which results in a significant increase in speed for subsequent runs on the same image if the "LBG Sampling" settings are still the same. Any changes to image filtering / drawing size will invalidate this cache.
- Added: New "Exporting Drawing" stats, "Page Size" & "Drawing Size" (which shows the size of the area occupied by the drawing only) to make the size of the exported SVG more clear.
- Added: SVG exports now contain the drawing stats, e.g. Shapes / Total Travel etc, enabled by default. Disable it by going to File/Preferences/SVG/Save Drawing Stats to SVG
- Added: SVG exports can now contain the PFM Settings as a JSON preset, disabled by default. Enable it by going to File/Preferences/SVG/Save PFM Settings to SVG
- Improved: Circular Scribbles Performance, now creates much smoother scribbles with fewer artifacts.
- Improved: Simplified Batch Processing Notifications to reduce clutter.
- Improved: Performance of Video Processing, frames are loaded when needed, rather than buffered at the start.  
- Improved: Path Optimisation performance, up to 2x reduction in 'pen up' / 'air time' with some PFMs
- Improved: User Created Presets will now have a coloured label _(User)_ indicating they can be edited/renamed/deleted etc.
- Fixed: Curves becoming flattened to lines in some instances when exporting SVGs.
- Fixed: Animation / Image Sequence Exporting not starting when selected.
- Fixed: Video Exporting stopping unexpectedly or causing "Java Heap Issues"
- Fixed: 'Image & Animation' settings category not displaying when selected in the preferences menu
- Fixed: 'Exported Drawing' stats, in previous versions the reported distances were inaccurate.
- Fixed: Pen Calibration PFM, crashing/hanging when the Test Count was set to 1.
- Fixed: LBG Circular Scribbles not rendering a preview while the sampling is running.
- Fixed: Export File Notifications glitching when using Batch Processing.
- Fixed: Interactions with the "Export to vPype" interface

### [v1.6.1-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.1-beta-free)
- Added: Pencil Sketch V1 preset from Discord.
- Improved: Renamed + Re-organised some ECS Drawing settings to make them easier to use.
- Fixed: Masking Settings not appearing in the user interface

### [v1.6.0-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.6.0-beta-free)
- **IMPORTANT: Premium Versions now have License Keys**, if you already own the Premium Version you'll find your license key [here](https://drawingbotv3.com/my-account/license_keys/). If you purchase DBV3 Premium in the future you'll also receive this License Key with your Order Confirmation. More details [here](https://drawingbotv3.com/license-keys/).
- **IMPORTANT:** .jar versions of DBV3 will no longer be distributed, please use the dedicated Windows/Mac/Linux installers. This is to allow more advanced features which require specific Java versions, DBV3 now uses JAVA 19.
- Added: **13 NEW PFMS** -
  - **Sketch Sweeping Curves**: A new Sketch PFM focused on creating long sweeping curves which are more natural / human-like, focused on creating longer curves. It shares most setting with other Sketch PFMS.
    - 'Curvature': Controls the appearance of "knots" in the curve. It translates to spline types: 0 = Uniform Catmull-Rom, 0.5 = Centripetal Catmull-Rom (Default), 1.0 Chordal Catmull-Rom
  - **LBG Shapes, Triangulation, Tree, Stippling, Dashes, Letters, Diagram, TSP, Circular Scribbles** - LBS (Linde Buzo Gray) PFMs combine the speed of Adaptive PFMs with the Quality of Voronoi PFMs and in most cases will outperform both in detail retention. They also respond better to large variations in stipple spacing.
    - 'Stipple Radius Min/Max' - Controls the spacing of the stipple points, smaller stipple radii will result in more detailed results.-
    - 'Density' - Controls the tone of the resulting image, a low density will be low contrast and may lose detail when the min/max stipple radius are far apart, a high density will be high contrast and retain more detail but will result in longer plotting times.
    - 'Threshold' - Controls the luminance at which stipples will stop being placed, similar to "Ignore White" but with finer control.
    - 'Max Iteration' - The number of times the stipple points will be refined, far fewer iterations are required to get good results than with Voronoi PFMs
  - **LBG Quad Tiles**: An LBG PFM which currently has no Voronoi / Adaptive counterpart, this PFM draws a Quad Tree from the LBS Stippling points.
  - **ECS Drawing:** This PFM has three separate components **E**dge Detection,  **C**ontour Detection & **S**hading. Each component can be controlled independently, you can also run them individually and then combine ECS with other PFMs instead using PFM Layers.
    - 'Simplify' - Simplifies the Edges & Contours generated.
    - 'Draw Edges' - Disables/Enables edge detection.
    - 'Edge Blur' - Affects the pre-filter blur applied before edges are detected, higher edge blur will result in less edges but the edges will be smoother.
    - 'Edge Threshold' - Controls the amount of edges which are generated: lower threshold -> more edges : higher threshold -> less edges
    - 'Draw Contours' - Disables/Enables contour detection.
    - 'Contour Blur' - Similar to Edge Blur but for contours, has a greater impact on the amount of contours and quality of the contours.
    - 'Contour Detail' - Controls the amount of contours to be generated.
    - 'Draw Shading' - Disables/Enables shading. The shading component uses a simplified version of the Sketch Lines shading feature, you may choose to disable the shading entirely and overlay another Sketch PFM using PFM Layers.
    - 'Shading Threshold' - Controls the luminance at which shading will be stopped: lower -> less shading : higher -> more shading
    - 'Shading Detail' - Controls the accuracy of the shading: lower -> less accurate, more stylised : higher -> more accurate, less stylised
    - 'Shading Length' - Controls the maximum length of the lines which make up the shading.
  - **Pen Calibration:** This PFM is used for generating Pen Calibration tests which can be used to select the most accurate nib size for "Rescale to Pen Width". Choose a suitable Nib Size range for the pen you wish to test and then plot the test. Then set your Pen Width for future plots with that pen to the highest nib size which has no gaps between the lines.
    - 'Nib Size Min' - The smallest nib size to test
    - 'Nib Size Max' - The largest nib size to test
    - 'Test Count' - The number of tests to run between the Min/Max nib size
    - 'Rotation' - Controls the angle of the lines in the generated line tests
    - 'Line Tests' - Generates simple hatches line tests
    - 'Circle Tests' - Generates concentric circle tests
    - 'Test Size' - The width/height in mm of each nib size test
    - 'Spacing X/Y' - The grid spacing in mm between each test in the grid
    - 'SVG Font' - The SVG Font to use to draw the labels for each nib size test
    - 'Title' - An optional title to draw at the top of page, you can leave this blank to have no title.
    - 'Font Size' - The height in mm of the label/title text.
- Added: **New Image Filter "Custom Overlay"** - import Black & White image overlays to create new unique styles!
- Added: **New PFM Setting - 'Draw Outlines'** - Draws the outlines of the shapes generated by Mosaic PFMS, great for creating more stylised results!
- Added: **CMYK Offset Settings** & **CMYK Display Settings** - Controlled in the "Configure" panel, Offset settings allow you to slightly offset each colour channel, display settings allow you to control the opacity of the pens, it will automatically update the colour of the pens and re-draw the drawing (this is a on-screen change only).
- Added: **LP Space** - A new shape type available in Voronoi Shapes / Adaptive Shapes / LBG Shapes - it roughly imitates the LP Space/p-norm function to create shapes which vary from Diamond -> Circle -> Rounded Square. This shape type can better represent the tonality of the original image by adjusting its shape to match. It also produces more variety in the final drawing.
- Added: **New Special Pens: Original Colour (Inverted),  Original Grayscale (Inverted)** - Samples the original colour of the source image and inverts it, for use with Digital Only outputs. Great for creating stylised Digital Drawings on a black background.
- Added: "Distortion" setting to all Dashes PFMs
- Added: "Clarity" setting to Sketch PFMs, which increases sharpness and detail in the final output, behind the scenes this effectively adds a Unsharp Mask Filter too the image before processing.
- Improved: Drawing Pen presets now be created easily with "New Preset" - you can select from existing categories and choose a colour
- Improved: Setting pop up windows for Colour Separation / Image Filters / Presets have been improved and standardised
- Improved: Console/Error Logging - log files will now be exported to the Config Folder / Logs, the last 10 logs will be retained
- Improved: Circular Scribbles curve quality
- Fixed: Projects with multiple versions creating massive project files which may fail to load.
- Fixed: Mosaic PFMs not responding in some situations
- Fixed: Batch Processing now supports SVG Files and all Image File types.
- Fixed: UI State Reloading

### [v1.5.3-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.5.3-stable-free)
- Added: "Create Curves" option to Voronoi TSP, matching Adaptive TSP.
- Improved: Stars & Triangles in Adaptive Shapes are now symmetrical
- Improved: Selecting values outside of the "safe" range of the slider with the text box now works as expected.
- Improved: Spiral centres can not be placed outside of the drawing area.
- Improved: When exporting files the last used extension will be kept.
- Improved: File name increments now also check for files with different extensions within the same file type e.g. image, svg
- Improved: Support for importing more image file extensions ".tiff", ".jif", ".jiff", ".wbmp"
- Improved: Clicking on the slider trigger Auto Run PFM as expected
- Improved: The CMYK Multipliers will be saved with the application as well as the project so will maintain their last state. 
- Fixed: "Create Curves" in Adaptive TSP not working properly
- Fixed: "Ignore White" in Spiral Circular Scribbles not working properly
- Fixed: Spiral PFMs crashing with Colour Match enabled
- Fixed: Checkboxes in the Drawing Pen dropdown not updating properly when pens are removed/added.

### [v1.5.2-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.5.2-beta-free)
- Added: **7 NEW PFMS**
  - **Spiral Circular Scribbles:** creates a spiral made of one continuous circular scribble
  - **Hatch Sawtooth:** creates a series of parallel wavy lines to represent an image, the lines are connected at each end, to form one continous line.
  - **Hatch Circular Scribbles:** creates a series of parallel circular scribbles to represent an image, the lines are connected at each end, to form one continous line.
  - **Adaptive Letters:** similar to Adaptive Shapes but instead uses letters from included SVG Fonts (it can also use regular fonts)
  - **Voronoi Letters:** similar to Adaptive Letters but distributes the letters with a Weighted Voronoi Diagram instead
  - **Mosaic Segments:** Generates a mosaic out of detailed segments using Simple Linear Iterative Clustering, the shapes it generates represent the shapes present in the original drawing much more accurately that Mosaic Voronoi.
  - **Mosaic Triangulation:** Generates a mosaics out of triangulation generated via Delaunay Triangulation.
- Added: **"Hatch Fill" options to SVG Converter** - which will allow DrawingBotV3 to convert many more SVG types, and generate fills for solid shapes which can easily be drawn by a pen plotter.
  - "Shape Clipping" - When enabled Solid Shapes will "clip" the shapes below, meaning if a shape is covered by another in the SVG only the visible part will be drawn, this only occurs for solid shapes, individual lines won't clip the shapes below.
  - "Shape Filling" - When enabled Hatch Fills will be generated
  - "Spacing" - The distance between the hatch lines, it is relative to the pen width so a spacing of 1.0 will draw over the entire shape with no gaps, a spacing of 2.0 will leave spaces the same width as the pen.
  - "Min/Max Rotation" - Controls the rotation of the generated hatch lines
  - "Link Ends" - Links the resulting hatch lines to create one continous line (for some shapes this may not always be possible)
  - "Crosshatch" - Creates an additional set of perpendicular Hatch Lines
- Added: **New Erasing Settings for Sketch PFMS**, these allow the creation of new styles, specifically ones which can be drawn more easily with a single pen. The following settings have been added.
  These settings replace "Adjust Brightness", you can think of "Erase Min" and "Erase Max" as the "Adjust Brightness range", using the same erase min & max is the same as setting "Adjust Brightness" in previous versions.
  Using a Erase Radius Min & Erase Radius Max of 1.0 will produce the same results as previous versions. The radius allows you to control the spacing of lines in a way which wasn't possible before.
  - Erase Min: the minimum intensity of the erase process
  - Erase Max: the maximum intensity of the erase process
  - Erase Radius Min: the minimum radius of the erased shape
  - Erase Radius Max: the maximum radius of the erased shape
  - Tone: controls the contrast of the erase processs, using a higher Tone will result in a image with a stronger contrast in the spacing of lines. The tone slider has no effect when the Erase Range values are identical.
- Added: **New PFM Presets**
  - Sketch Lines ("Digital", "Sharp Lines", "Micro Detail")
  - Adaptive Dashes ("Vertical Lines", "Horizontal Lines", "Needles")
  - Adaptive Circular Scribbles ("Sketchy")
  - Adaptive Shapes ("Overlapping Squares", "Overlapping Circles")
- Added: **"Exported Drawing" display mode**, this will show the last set of files exported, you can select a recent exported file.
  - It shows the difference in the following stats after path optimization, "Shapes", "Total Travel", "Distance Down", "Distance Up", "Pen Lifts" and also each pen it's respective distance travelled.
  - By default when you export a vector file the "Exported Drawing" display mode will be shown, you can disable this in File / Preferences / Export Settings / General
- Added: Splash Screen, which displays while DBV3 is loading.
- Added: New "Contrast" & "Brightness" settings to Adaptive PFMs, Adaptive PFMS rely on good contrast in the original image, so now a initial level of contrast is applied, the default is currently "1.25", setting this too "1.00" will result in the same results as previous versions
- Added: "Align Rotation" setting to Adaptive Shapes, this will cause the shapes generated to follow the natural contours of the image, when enabled you won't be able to set "Min" & "Max" rotation
- Added: "Point Density" to Voronoi PFMs - this new options allows you to specify a density of points which is relative to the drawings dimensions, instead of matching a arbitary point count ( you can still use a point count with the "Point Limit" option) i.e. a A4 and A3 drawing with the same density would have a similar about of points in a given area
- Added: "Ignore White" option to Voronoi PFMs, when enabled the PFM will try to avoid creating shapes in white ares of the image.
- Added: 'Spiral Type' setting to Spiral Sawtooth (previously named Spiral PFM)
  - "Archimedean" - The default / original Spiral.
  - "Parabolic" - Two connected spirals / Fermat Spiral
- Added: "Invert" button to Mask Settings.
- Added: "Auto Run PFM" - this will automatically re-run the PFM when you change any settings or when you change the current PFM, you can disable this in File/Preferences/General/Auto Run PFM
- Added: "Select", "Edit", and "Draw" Modes too Masking Settings to allow the customisation of Masking and drawing custom masks with lines / bezier curves
- Added: "Soft Clip" option to Masking Settings - when this is enabled PFMs will naturally overlap over the edges of the mask rather than clipping the shape exactly at the border.
- Added: "Square Tiles" option to Mosaic Rectangles, when enabled the PFM will only create square tiles, when enabled "row count" and "row padding" will be disabled
- Improvement: **Speed Improvements for Adaptive PFMs**, up to 3x faster on larger images.
- Improvement: **Speed Improvements for Sketch PFMs**
- Improvements: The quality of the output from Adaptive Circular Scribbles has been greatly improved and the default preset will now perform much better and result in a more consistent circular scribble quality.
- Improvement: Increased the rendering speed of the viewport, changes to pen colours and shape ranges will be much more reponsive.
- Improvement: Renamed mask buttons from "Bypass Mask" & "Show Masks" too "Enable Masking" and "Display Masks"
- Improvement: Masking - when zooming in the masks edges will now remain 1px wide to allow more precise positioning.
- Improvement: Masking - you can now more masks using the arrow keys.
- Improvement: Using the "Reset" and "Randomise" options on category headings in PFM Settings will now affect all the settings in the category.
- Improvement: The shape / vertice count will now update with the shapes slider
- Fixed: Original Sampling Pens (Original Colour, Original Grayscale etc.)
- Fixed: Installation issues on MacOS, all MacOS installers are now digitally signed and notarized with Apple.
- Fixed: Installation issues on Windows, all Windows installers are now digitally signed and time stamped.
- Fixed: Adaptive PFMs creating unexpected overlapping shapes in certain circumstances.
- Fixed: Spiral PFM producing inconsistent line spacing
- Fixed: CMYK Seperation sometimes producing pixellated areas in Dark areas of the image.
- Fixed: "Reset" button in the Configure Styles menu.
- Fixed: Mosaic PFMs randomly crashing on some runs.
- Fixed: Mosaic PFMs weight not having any effect on number of specific styles
- Fixed: Masking Settings not working in some situations
- Fixed: Old projects not opening properly in new versions.
- Fixed: Issue where sometimes the Pen Distribution would not update properly.
- Fixed: Issue where Sketch Waves would freeze and not complete in some situations.
- Fixed: Adaptive TSP path linking accuracy (results in less overlapping lines)
- Fixed: Reduced the impact of several background processes

### [v1.5.1-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.5.1-beta-free)
- Added: 'Voronoi Style' option to Adaptive Voronoi, Voronoi Diagram & Mosaic Voronoi.
  - "Classic" - The default and original Voronoi Diagram
  - "Smooth" - A smoother version of the original
  - "Sharp" - A more jagged / stylised version
  - "Offset A, B, C" - Less detailed / stylised version using the different offsets of adjacent triangles
- Added: "Smooth Distribution" option to Colour Match, this option will result in a more even usage of different pen colours which will rely less on Black & the canvas colour.
- Added: Reset UI button to the "View" menu
- Improved: Low Quality mode will now run faster and produce similar results to version of DB before v1.5.0  
- Improved: Speed improvements for Sketch PFMs
- Improved: Speed improvements  for Colour Match  
- Improved: Sketch Shapes paths will now link properly when in Rectangle mode, significantly reducing plotting time.
- Improved: Path optimisation for all PFMS, significant reduction in plotting times.  
- Improved: Mosaic PFMs now have better Multi-Threading so will process much faster.
- Fixed: Sketch Sobel crashing in some situtations.
- Fixed: Sketch PFMs having an uneven distribution when compared to previous versions of DB before v1.5.0
- Fixed: SVG Converter PFM will now retain the original units / scale of the imported SVG.
- Fixed: Voronoi Diagram not updating on each iteration
- Fixed: Mosaic PFMs using the wrong scale when High Quality mode is being used.   
- Fixed: Mosaic Voronoi having excessively long processing times.
- Fixed: Colour Match crashing in some situations due to excessive memory usage.
- Fixed: Minor Issue where the final shape might be not drawn or drawn with the wrong pen.

### [v1.5.0-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.5.0-beta-free)
- Added **High Quality Mode** for image rescaling, produces more consistent results across different paper / pen sizes and results in better quality plots.
- Added **Preferences Menu** - Much more finer control over settings in DBV3, with more settings on the way!
  - General Panel: Here you can change some DrawingBotV3 default settings, like default pfms, canvas colour, pen width, pfm presets etc.
  - User Interface: In this panel you can enable / disable rulers, drawing borders and notifications.
  - Export Settings: This is the new home for all your usual Export Settings.
  - NOTE: Search Function is currently limited, but will search settings individually too.
- Added: **'Directionality', 'Distortion', 'Angularity', 'Edge Power', 'Sobel Power', 'Luminance Power' and 'Squiggle Min Length'** to All Sketch PFMs
  - Directionality - forces the lines to follow the natural contours of the image
  - Distortion - adds some noise to the generated lines, creating more stylised images.
  - Angularity - higher angularity results in lines which don't change direction as frequently, resulting in more sweeping curves in curve pfms
  - Edge Power - used to exaggerate key edges in the image
  - Sobel Power - used to exaggerate a cartoonish quality for the plot
  - Luminance Power - typically PFMs will follow dark areas in the image when creating lines, this slider can be used to decrease the influence of brightness which in turn will favour other style options like Directionarity or Edge Power etc.
  - Squiggle Min Length - prevents incredibly short squiggles from being created, shortening plotting times - thanks to HanzPetrov
- Added: "Connected Lines" - Option to Spiral PFM - creating one continuous polyline for the spiral, massively reducing the amount of pen lifts required.
- Added: "Pen Force" to HPGL Presets for use with the HP 7550
- Added: 'Curve Offset' options to Cubic Beziers and Quad Beziers - These options allow you to control the 'wiggle' of the curve
- Added: Notifications (W.I.P) - Pop-Ups to show you additional information when images are imported / files are exported.
- Added "Rating" and "Notes" columns to Version Control
- Added: "Multipass" - Path Optimisation option to draw over each geometry in a drawing multiple times.
- Added: Ability to have multiple projects open simultaneously in the same window
- Added: **Quick Export** - Added the option to Export Drawings with a single click, or by pressing CTRL + E - you can customize the type of file exported in File/Preferences/General/Quick Export
- Added: Option to disable/enable Transparent PNG export.
- Improved: **Colour Match Support for all PFMs** - All PFMs now support the colour match seperation option!
- Improved: **Huge Speed Improvements** for all Adaptive PFMs, up too **x4 quicker.**
- Improved: **Massive Speed Improvements** for the Adaptive Tree PFM, up too **x20 quicker** for large images.
- Improved: **Cubic Beziers Rewrite** - the cubic beziers PFM has been re-written and now can create much higher quality curves
- Improved: Speed Improvements to Sketch PFMs especially up to **x3 quicker** for larger images
- Improved: Sketch PFMs now use a more accurate erasing method, this results in a slower processing time, but better quality lines.
- Improved: Exporting files of the same name, will now have automatically incremented names e.g. name_1, name_2 etc.
- Improved: The pens in the drawing will now be re-distributed when the shapes range slider is adjusted.
- Improved: PFM Controls now have category headings which can be collapsed, this helps to show the relationship between settings and makes it easier to find them again.
- Improved: Settings panels can now be dragged and dropped, you can drag them to be on the right hand side of the viewport, or drag them over the viewport to undock them, you can also drag them over each other to swap them.
- Improved: The user interface layout will now be saved on exit & restored with projects (this can be configured in preferences)
- Improved: The main settings panel can now be resized horizontally.
- Improved: When undocked individual settings tabs can be resized horizontally & vertically.
- Improved: When changing Input Units in the Drawing Area panel, the values will automatically update
- Improved: New icons and options in the File Menu.
- Improved: Added Keyboard Shortcuts for common actions
   - New Project = Ctrl + N
   - Open Project = Ctrl + O
   - Save Project = Ctrl + S
   - Import Image = Ctrl + I
   - Quick Export = Ctrl + E
   - Switch Display Modes; Drawing = Image + 1, Drawing = Shift + 2... etc.
- Fixed: Batch Processing Options not being saved with the project file  
- Fixed: HPGL Presets not saving pen velocity
- Fixed: Issue with Adaptive PFMs sometimes creating one-off spots in otherwise consistent areas.
- Fixed: Open GL Renderer having glitched splits on curve pfms.
- Fixed: Issue preventing versions being renamed
- Fixed: Some in-built drawing area presets having the wrong Input Units.
- Fixed: Default Drawing Sets not reloading
  
- Please consider supporting the development of DrawingBotV3 by donating [here](https://drawingbotv3.ollielansdell.co.uk/product/donation/). Thank you!

### [v1.4.2-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.4.2-stable-free)
- Added: New improved button controls for Image Processing, Pen Settings, Version Control and Mask Settings
- Added: Support for different Plotting Resolution on individual drawing styles in Layers PFM and Mosaic PFMS  
- Added: Option to specify the naming convention for Inkscape Layers, go to File / Export Settings / SVG.   
- Added: Option to enable/disable background layer export for SVGs, it will now be disabled by default  
- Added: Option to disable Grid Snapping when moving masks, found at the top of Mask Settings
- Added: Controls to manually edit the size of the selection when editing masks, found at the bottom of Mask Settings
- Improved: When performing actions on items in tables selections will now be more intuitive e.g. when moving items up and down, they will stay selected
- Improved: Drawing Set render previews will now live update with pen changes    
- Improved: Increased the size of the text boxes when editing GCode in File / Export Settings / GCode  
- Improved: When opening projects they will always open in the Drawing display mode, rather than Image.
- Improved: Standardised the sizing of tables in the UI
- Fixed: SVG Exports having the wrong scaling when using Inches or Centimeters
- Fixed: Plotting Resolution not working correctly when in CMYK Colour Separation
- Fixed: Completely Black Pens not being shown in SVG Exports
- Fixed: SVG Exports failing in rare cases where closed paths would be merged  
- Fixed: Export per/n pens being too large on some screens and made the window resizable.
- Fixed: Removed erroneous pen 222 from Staedtler 36 pen pack - led 

### [v1.4.1-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.4.1-beta-free)
- Added: **Colour Match - Premium Only** - A new Colour Seperation mode for Sketch PFMs only, it will compare the pens in your drawing set to the colours in the image and when drawing will try to use the best matched pen.
  You also have a few options to configure colour match further, found to the right of the Colour Seperation drop-down.
  
  - "Colour Accuracy" - Decreasing the Colour Accuracy will lower the quality of the colour match and allows pens to draw over areas where they have a higher colour difference. Colour Accuracy relates to a Delta-E colour measurement in the following way
    - 100: No colour difference
    - 99: Slight colour difference     
    - 98: Small colour difference     
    - 97: Medium colour difference
    - 96: Large colour difference
    - 95: Very large colour difference
  
  - "Brightness Multiplier" - Decreases the overlap of the pens in the image
  - "Pen Limit" - Limits the number of pens which can be used when matching, if this value is set to 18, only the 18 best matched pens will be used. If this value is set to 0, there is no limit.
  - "Use Canvas Colour" - Adds an invisible pen which matches the colour of the Canvas, helps to avoid drawing unnecessary lines.    
  - "Line Density - from Sketch PFMs" - the line density control found in the Sketch PFMs can also be used to control the Colour Match output.
  - **For the best results**
    - Use all the pens of a specific manufacturer, i.e. Use the presets for the set of 60 Staedtler Fineliners, then use the Pen Limit feature, to limit selection to approx 18+ pens allowing colour match to choose your best matched pens for each plot.
    - Use a bright, saturated image, murky images or ones with a limited colour palette will perform poorly.    
- Added: **SVG Import & SVG Converter PFM - Premium Only**
  - You can now import SVGs generated in other softwares or in DrawingBotV3, this opens up many possibilities, such as SVG cropping, rotation, flipping, masking, pen/layer reassignment, layering multiple SVGs and recovering old DBV3 projects. Note: Any text in the SVGs will be blank, you should use "Object to Path" in Inkscape before importing SVGs with text.
  - SVGs can be also be treated like any other image allowing them to be run through any PFM, they will be rasterised at the highest quality possible before plotting (the image tab will show a low quality preview of the SVG).
  - You can control the conversion process in the new PFM SVG Converter, it has a few settings.
      - SVG Path: The path to the SVG to be used, if this isn't set it will use the current imported SVG, if there isn't one the PFM will produce no output. You can however use this PFM as part of a Layers PFM, to layer SVGs together, when you run the PFM like this you can set the SVG Path to use an SVG which hasn't been imported.
      - Derive Drawing Set: When enabled DrawingBotV3 will generate a new Drawing Pen for each colour in the SVG, allowing you to access them each as layers. This opens up possibilities for splitting SVGs by colour using a per/layer export, or even re-assigning colours to your SVG, once the SVG Converter PFM has been run you'll have access to change the colour of lines with all the tools available with a default drawing.
  - You can also import an SVG to run through a standard PFM
 
- Added: **Masking - Premium Only** - A new useful settings tab, to allow you to mask areas of the image, you can add shapes masks such as Rectangles and Circles, you can move, resize, rotate, skew with the controls in the viewport area. You can also import SVGs as masks, they will keep their original sizing so you can create detailed masks by importing an SVG with the same dimensions as in drawing area controls.
- Added: **Image Cropping** - You can now crop images in the Image Processing tab, this allows you to enter values in pixels for cropping the image. You can also hit the "Edit" button and this will display a resizable box in the viewport area which you can then move to create the desired crop. You can also access this cropping feature via the new Display Mode "Image Cropping".
- Added: **Rulers Overlays** - There are now rulers on the borders of the viewport area to show you an accurate scale of your drawing / image.
- Added: Lock 1:1 button to the viewport toolbar, to allow viewing drawings at the correct scale relative to the screen, useful for evaluating the density of your plot.
- Added: Reference Image exports - You can now export the edited reference image used in your plots, via Export / Export Reference Image File.
- Added: Winsor & Newton ProMarkers, Pens/Drawing Sets - led
- Added: Bic Cristal and Intensity, Pens/Drawing Sets - led
- Added: Staedtler Fineliner 305 Sky Blue - Pen Definition - led  
- Added: Support for Googles WebP Image Files (.webp)
- Added: Fullscreen Mode, you can enable it be going to View/Fullscreen Mode
- Added: Frame Hold Start and Frame Hold End to Animation Settings
- Changed: Increased default render quality in the viewport to x4 the previous quality
- Changed: You can now send any file type via the Serial Connection, allowing for sending GCode files to compatible plotters.
- Changed: Image Rotation / Flipping is now Image Specific, when you load a new image these values will be reset.
- Fixed: Drawings/Images flickering when switching Display Modes.
- Fixed: Serial Connection commands will now be executed properly on non-HPGL devices.
- Fixed: Staedtler Fineliner 63 Delft Blue - Pen Definition - led
- Fixed: Export Directory being used instead of Import Directory when importing files - led
- Fixed: Removed duplicate Copic Black Pens - led
- Fixed: "Original Sizing" preset not activating properly  
- Fixed: GCode Settings not saving properly
- Fixed: HP-GL Padding/Offsets creating inaccurate drawings outside of the HP-GL bounds
- Fixed: When exporting with N/Pens the render order will now be used rather than the export order, meaning the order will now match the generated HPGL files correctly.

### [v1.4.0-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.4.0-beta-free)

Note: This update has kept growing and growing, and it’s time it was released, this is a Public Beta, although it has already been through an alpha testing phase.
- Added: New 9 new PFMS
    - **Adaptive Circular Scribbles (Beta)** – this is an implementation of Chiu Et Al 2015, “Tone‐ and Feature‐Aware Circular Scribble Art” – it generates one continuous circular scribble to represent the image. If you wish to achieve results similar to Chiu Et Al’s implementation use a size of paper, pen width which gives you a plotting size of 4000px on the largest edge then use the “Chiu Et Al – 4000px” preset
    - **Adaptive Shapes, Triangulation, Tree (Beta), Stippling, Dashes, Diagram, Adaptive TSP**, are all very similar to their Voronoi counterparts but give a much better representation of the tonality of the input image, they are also typically faster to run.
    - **Voronoi Dashes**, this variation of the new Adaptive Dashes PFM, but using the original Voronoi style.

- **Using Adaptive PFMS**
    - They love high resolution, high contrast images.
    - They are called "Adaptive" because they adapt to match the tone of the input image. This means the reproductions of tones is way more accurate then other PFMs, this means they have an additional processing stage "Tone Mapping". This process only needs to be performed once per configuration of settings, if you change a setting which could alter the tone map it will run again.
    - You can view the output of the tone mapping stage by selecting "Display:" and then "Tone Map", this shows you three outputs the Reference Tone Map, the drawing created by the PFM with the current settings and the blurred version of this output. If your blurred output reassembles the reference tone map v closely that's very good and if you find one which matches better then the current Adaptive Circular Scribbles settings let me know. If the tone map doesn't reassembly the image very closely don't worry DrawingBotV3 will account for this variance to create an image which better matches the tone map anyway.
- Added: Ability to have multiple Drawing Sets, you can then use different sets for each layer/tile in Layers PFM / Mosaic PFM.
    - Drawings Sets can be created and edited from the Pen Settings tab.
- Added: Colour Picker option when right clicking a Drawing Pen, then right-click anywhere in the viewport to set the pen’s colour.
- Added: Settings panels can now be undocked, and moved around independently, they can be re-docked by closing the window it hitting the link button again.
- Added: “Per/Group” export option, when using PFM Layers or any Mosaic PFM this option will export the individual layers/tiles individually
- Added: “Per N/Pens” export option, to allow drawings with multiple pens to be split into a certain number of pens. Useful when exporting for Vintage Plotters which have automatic pen changes.
- Added: “Layer Distribution” option to PFM Layers, which allows finer control over the distribution of pens between layers
    - NONE: Layers will be distributed seperately.
    - ORDERED PER PFM: Layers which use the same PFM and Drawing Set will be distributed together, treating the first layer as the darkest and the last layer as the brightest.
    - ORDERED: Same as the above, but only matches Drawing Sets
- Added: “Clipping Mode”, available in the Drawing Area, allows you to choose if shapes should overflow the edges of the drawing/page or not, this will only have an effect on some PFMS, as some PFMS only work within the image provided anyway. You have three options 
    - “Drawing” – Clip the geometries to the drawings edges
    - “Page” – Clip the geometries to the page’s edges
    - “None” – Allow geometries to overflow the page and drawing
- Added: Portrait/Landscape toggle to the Drawing Area to replace the “Rotate” button, this will remember the orientation you have chosen and keep this orientation when you select a new paper size.
- Improved: Voronoi PFMs are now multi-threaded and also have a new slider “Voronoi Accuracy”, an accuracy of 100% is the equivalent to the previous version 
- Improved: The OpenGL Renderer will now render while the drawing is being generated, the UI will also be much more responsive when it’s updating.
- Changed: When editing Drawing Sets while using Layers PFM / Mosaic PFM they will now live update without having to run the PFM again.
- Changed: Moved “Blend Mode” from Pen Settings to be above the Viewport
- Changed: Moved “Colour Seperation” to be part of Pen Settings
- Changed: Pen settings buttons Add/Remove/etc. now use symbols instead of text and have tool tips when hovered, to try and re-clutter the UI.
- Changed: If you open a project and DrawingBotV3 is unable to locate the original image used you will now be prompted to locate the image.
- Changed: The recommended Distribution Type will now be change based on the current settings.
- Changed: DrawingBotV3 will now remember the PFMs “preconfigured” distribution, so when creating drawings with CMYK seperation the distribution will not be forgotten when you switch back.
- Fixed: Some pop-up windows not closing when the main window is closed.
- Fixed: Mosaic PFMs and Layers PFM will now show their current progress properly.
- Fixed: Voronoi PFMs will now show their current progress properly.
- Fixed: HPGL Exports not having the correct offset
- Fixed: GCode Settings not loading correctly when switching presets
- Fixed: Custom version names not being saved with the project


### [v1.3.5-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.3.5-stable-free)
- Note: The recommended JAVA version for DBV3 is now Java 17.
- Note: All packaged installers now use a Java 17 Runtime instead of Java 11
- Added: Sketch Waves PFM, another Sketch PFM variation which uses Sin/Cos/Tan waves to direct the sketched lines, it's in Beta (Premium Only)
- Added: Sketch Layers, allows you to layer the results on multiple PFMS on top of each other and optionally pass the lightned image between them to create new unique styles (Premium Only), thanks to HanzPetrov for all his hardwork on this!
- Added: Projects saved will now store the current drawing state, allowing you to reload projects exactly where you left off. Versions will also store the drawing state, so you can switch between variations instantly. (Premium Only)
- Added: New GCode Options, "Curve Flatness", "Center Zero Point" and "Comment Type"
- Added: US Paper Sizes to Drawing Area presets. ANSI, ARCH and US Letter/Legal/Executive.
- Fixed: OpenGLRenderer will now work as intended on MacOS!
- Fixed: Images randomly failing to load, fixes issue where images might need to be imported twice.
- Fixed: Viewport scrolling on some hardware configurations.
- Fixed: Serial Connection control panel not opening
- Fixed: MacOS .pkg Installers not running
- Changed: Delayed OpenGL Initialization to speed up load times and prevent crashes at start up.
- Changed: Simplified logging, removing redundant information
- Changed: The first time you run OpenGL after restarting DBV3 it will take a moment to activate.
- Changed: Improved OpenGLRenderer compatibility to favour faster implementations.

### [v1.3.4-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.3.4-stable-free)
- Fixed: Windows .zip and .exe Installers not running for some configurations.

### [v1.3.3-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.3.3-stable-free)
- Added: New Installer Type: .pkg for MacOS Users, this is considered Beta!
- Added: "Set As Default" option to all preset menus, the selected default preset will load when DBV3 is started.
- Added: Export / Import Preset Dialogs, the export dialog gives you the option of opening the folder you saved the preset too.
- Changed: Versions will now be saved in project files.
- Fixed: For now by default the OpenGLRenderer will be disabled on MacOS to prevent issues at startup, if you think your MAC is compatible you can change "enableOpenGLMacOS" to "true" in the config but this may cause DBV3 to not start properly.
- Fixed: Voronoi Circles will now produce a useful output if it's stopped early.
- Fixed: Half-pixel offset showing up on the following Display Modes: "Image", "Reference", "Lightened"

### [v1.3.2-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.3.2-stable-free)
- Hot-Fix: Exports missing some geometries, in particular Voronoi Triangulation.
- Fixed: Imported Presets not saving properly.

### [v1.3.1-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.3.1-stable-free)
- Added: New option in config "disableOpenGLRenderer", some systems are incompatible with the Hardware Accelerated Renderer, if DrawingBotV3 fails to start, try changing this value to true.
- Fixed: Batch Processing on MAC and Linux will now use the correct File Seperator.
- Fixed: SVG Exporting
- Fixed: Drawing Pen / Drawing Set preset loading.

### [v1.3.0-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.3.0-stable-free)
- DrawingBotV3 now comes in two version **Free** and **Premium**, find out more [here](https://drawingbotv3.ollielansdell.co.uk/premium-version/)
- Added: New Hardware Accelerated Renderer, to activate go to Display Mode : "Drawing (Hardware Accelerated)"
- Added: "Squiggle Deviation" Slider to Sketch PFMs, this allows you to decide how far a squiggle is allowed to deviate in brightness before it is ended prematurely, this has the result of making shorter squiggles which are more accurate and less likely to cross over brighter areas of the image. 
- Added: "Unlimited Tests" Option to a few Sketch PFMs, this will run as many tests as required to find the "best" line possible, resulting in more accurate drawings with longer processing times.
- Added: "Curve Refinement" Option to Catmull-Roms, this will add an additional pass after the curves have been found to see if minor adjustments to the curves points will improve the accuracy.
- Added: Blend Mode compatibility to PDF export - HanzPetrov
- Added: Live updating "Position", above the viewport you'll be able to see where you mouse is relative to your drawings size!
- Changed: Batch Processing will now display the current drawing, and give more useful progress messages and updates
- Changed: Sketch Curves/Beziers/Catmull-Roms now render as individual curves rather than grouped shapes, allowing finer control over the curves rendered, and resulting in a more accurate mapping of the colour values to the pens enabled.
- Changed: Shading Options are now also available on Sketch Shapes and Sketch Sobel Edges
- Changed: Improved the accuracy of Sketch PFMs
- Changed: Sketch PFMs now use the Bresenham Midpoint Circle Algorithm to reduce the number of neighbour tests required.
- Changed: Allow Customizable Drawing Pen & Drawing Set Categories rather than just "User"
- Changed: Voronoi Default Settings: Point Count now defaults to 50000 instead of 10000 and Luminance Power / Density Power default to 3 instead of 5
- Fixed: When creating new Pen Presets, the type name will be used instead of defaulting to "User".
- Fixed: Image Filters losing their settings when using "Edit Settings" and not hitting apply.
- Fixed: Image Filters not updating when an earlier filter in the chain is disabled.
- Fixed: Sobel Edges PFM not working properly on images with Alpha Channels
- Fixed: Dirty Border Filter not working properly on images with Alpha Channels
- Fixed: CMYK Separation not working properly on images with Alpha Channels
- Fixed: View menu will now work as expected and not glitch out the settings panels
- Fixed: Export settings panels disappearing after being displayed while exporting for HPGL, Image Sequences and GCode
- Fixed: "Special" Pens can now be saved in Drawing Set Presets without losing their functionality.

### [v1.2.3-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.2.2-stable)
- Fixed: Drawings randomly failing to export when Path Optimization is enabled.
- Fixed: GCode Bezier Curves not matching the drawing properly. -Triod-project
- Changed: Geometries will now start from the middle of the pixel and not the top left - HanzPetrov

### [v1.2.2-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.2.2-stable)
- Fixed: Fixes UI Hanging after re-processing image

### [v1.2.1-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.2.1-stable)
- Added: Option to specify the PPI to render Images/Animations at higher resolutions, in the Export Settings menu  - HanzPetrov
- Added: Replaced the Displayed Shapes slider with a Range Slider
- Added: "Apply to export" checkbox to enable the Ranges selected on the exported files
- Added: A "Task Monitor" to monitor background tasks, accessed via File/Open Task Monitor
- Changed: Removed the "Zoom In" and "Zoom Out", the viewport can now be zoomed with the scroll wheel.
- Changed: Line Merging/Sorting Algorithms now use an STRTree which results in better optimisation in 1/10th of the time, particularly on Voronoi Triangulation PFMS.
- Changed: The progress of Optimization Algorithms will now be shown in the progress bar.
- Changed: The default "Curve Flatness" for HPGL from 6 to 0.1
- Changed: The default "target pen width" from 0.5 to 0.3
- Changed: The default rotation for HPGL from 90 to 270, when using AUTO which requires rotation
- Changed: Improved the monitoring of progress during Geometry Optimization
- Fixed: Bug where the Serial Connection Menu would fail to load when less than 2 Serial Ports were available.
- Fixed: The viewport will now behave as expected when zooming in and out.

### [v1.2.0-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.2.0-stable)
- Added: HPGL Export - Export your images to HPGL for sending to plotters.
    - You can configure your HPGL files with configurable Hard-Clip limits, X-Axis Mirror, Y-Axis Mirror, X-Axis Alignment, Y-Axis Alignment, Rotation, Curve Flatness, Pen Velocity and the initial Pen Number.
    - If you're inputted the Hard-Clip limits you can use "Auto" rotation, to position your image correctly for you so you don't need to worry about if it's landscape or portrait!
    - Choose from presets for different plotter models and paper sizes
    - When the export is complete you'll see a dialog indicating where pens should be loaded and the size of the HPGL File.
    - If the drawing exceeds the Plotter's Hard-Clip limits you'll be notified.
- Added: Serial Port Connection for Plotters! Send HPGL Files and commands directly from DrawingBotV3.
    - Allows you to set the Serial Port, Baud Rate, Data Bits, Stop Bits, Parity and Flow Control
    - Also including monitoring of the Progress, Bytes Sent, Elapsed Time, Remaining Time and the Plotters own buffer.
    - Sending can also be paused when needed and resumed.
    - Ability to detect the plotter being used and apply the recommended settings!
    - The serial port connection runs in a seperate thread so you can continue using DrawingBot while your Drawing is plotting.
    - It can also be used to send files not generated with DrawingBot and in fact will stream any .txt file over the serial port!
- Added: Video Import - You can now import videos and process every frame automatically
    - When you choose your export option this will apply to every frame in the video
- Added: Configurable Canvas Colour - HanzPetrov
- Added: Right-Click menu for Drawing Styles
- Changed: Stroke end-caps will now default to ROUND - HanzPetrov
- Changed: PDF exports will now match the print resolution not the image resolution - HanzPetrov
- Fixed: Disabled pens will not output a file when exporting "per/pen"
- Fixed: Mosaic Custom being uneditable when loaded from a preset
- Fixed: Pen Width not being loaded with projects, or preventing them loading entirely.

### [v1.1.1-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.1.1-stable)
- Fixed: Previous iterations of Voronoi Diagrams showing up in SVG exports. (Random dots on the export)

### [v1.1.0-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.1.0-stable)
- Added: Three New Path Finding Modules Mosaic Custom, Voronoi Tree and Voronoi TSP.
  - "Mosaic Custom" - Allows you to create your own Custom Mosaic, using an image mask. 
    This opens up many creative possibilities combining the effects of multiple PFMs to create more complex pieces. 
    This is very easy to do, you first need to create an image mask in an image editor which has the same resolution as the original image which contains a unique block colour for each drawing style you plan to use.
    Then you can import this mask as one of the settings, and for each Drawing Style define a "Mask Colour" which matches the one you used in the mask image.
    You can also feather the mask in the settings.
  - "Voronoi Tree" - Creates a "Minimum Spanning Tree" using Prim's algorithm, using the points generated from a Weighted Voronoi Diagram, it creates artwork similar to the classic TSP art but in a fraction of the time.
  - "Voronoi TSP" - Creates a solution to the "Travelling Salesman Problem" for the points generated from a Weighted Vornoi Diagram, if you're serious about using this PFM with a high point count expect to wait many hours or days to get the final result, optimisations are very welcome! 
    There are currently 3 algorithms, a Lazy 2-Opt Approach which is very fast and inaccurate, then an implementation of the classic Lin-Kernighan, and an attempt to implement the Lin-Kernighan-Helsgaun algorithm. 2-Opt is the most stable and the one I recommend.
- Added: CMYK Configuration, next to the colour seperation drop-down you now have the option to configure the weighting of the CMYK plot to allow you to fine tune the density to your specific pens, as many users reported their K layer being too strong, the default for K is now x0.75. This option works with every PFM.
- Added: Video Exporting for Animations in both H.264 and ProRes 422.
- Changed: "Randomise" will now produce more stable results and should be able to run in most instances
- Changed: The min/max of a few key variables have been changed.
- Changed: Values can now be set outside of the range of the min/max for more advanced users, in some instances this may cause the plot to crash or not start at all.
- Changed: Some PFMs which had optimisation disabled by default will now have as a minimum geometry sorting enabled to reduce plotting times
- Changed: Mosaics now optimise the outputs of each tile individually to speed up optimisation times.
- Changed: Raster export dimensions will now be multiples of 2
- Fixed: Padding not being saved properly with Drawing Area presets.
- Fixed: De-activated pens being re-activated in presets
- Fixed: Voronoi Circles and Voronoi Diagram PFMs will now stay within the bounds of the image.
- Fixed: A bug where videos would not export if either of the image dimensions are odd.


### [v1.0.16-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.16-stable)
- Added: Text to display the current images size, and the size it's being plotted at.
- Added: Rotate / Flip Options for images in Image Processing
- Fixed: Issue where the viewer would zoom into the top left corner of the image after pre-longed use.
- Fixed: Drawing Pens / CMYK Settings not loading correctly with project files.
  - Changed: Renamed the "Pre-Processing" tab to "Image Processing"
  - Fixed: Image Rendering - Blend Modes will now create a more accurate representation on exports.
- Fixed: PNG export accuracy, they will now match the JPG exports and only be transparent if the blend mode is "Normal".
- Fixed: Image Rendering / Image Sequence Rendering - Rendering will now be more accurate when using blend modes or CMYK seperation
- Fixed: Image Sequence Rendering - Fixes a rare glitch where random lines may appear on an animation.
- Fixed: Rare bug where Colour Sampling in curve modes would result in the final line being completely transparent.

### [v1.0.15-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.15-stable)
- Added: Two New Path Finding Modules for creating Mosaics!
    - "Mosaic Rectangle" - Creates a mosaic of rectangles out of a set of user configurable styles.
    - "Mosaic Voronoi" - Creates a mosaic based on the output of a weighted Voronoi Diagram from a set of user configurable styles.
    - The "User Configurable styles" can have a "weighting" to affect their distribution, there is no limit on the number of different styles per mosaic.
    - Please note: Using Voronoi PFMs for drawing styles can be tempremental.
- Added: Version Control! You can now save your favourite versions of your project as you go and experiment with settings without losing your favourites.
    - There is a "Save Version" button next to the plotting controls, click this when you create something you like, you can then access and reload / save these versions from the "Version Control" panel, which will show a preview of the version the date it was created and the name of the path finding module used.
- Added: Project Loading & Saving! 
    - You can now save your projects and reload them as ".drawingbotv3" files from the "File Menu" - you can also save versions in this way.
    - This saves all elements of the project, like Drawing Size, PFM Settings, Pen Settings & also all of the versions you've created!
    - It doesn't however save the image used to keep the files small but it will save the path to the image used and reload it if it's still available. 
- Fixed: Default presets become replicated when saving new presets.
- Fixed: Broken Sketch PFM Presets, they will now behave as intended again.

### [v1.0.14-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.14-stable)
- Added: Image Sequences for creating animations of your creations! The duration can be change in Export Settings / Image Sequences
- Added: GCode "Start Layer" & "End Layer" custom commands
- Fixed: The new curve PFMs will now bypass plot optimization automatically to avoid curve flattening.
- Fixed: GCode exports will now include bezier curves, though this requires more testing.

### [v1.0.13-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.13-stable)
- Added: Four New Path Finding Modules! All of which use much more accurate "Bresenham" calculations this results in longer processing times.
    - "Sketch Quad Beziers" - Builds the image out of the darkest quadratic curves. By first finding the darkest line and finding the darkest control point.
    - "Sketch Cubic Beziers" - Builds the image out of the darkest cubic curves. By first finding the darkest line, and then finding the darkest combination of control both points.
    - "Sketch Catmull-Roms" - Builds the image out of the darkest catmull-rom splines. This works by finding the best possible curve over the next two segments.
    - "Sketch Shapes" - Builds the image out of either Squares of Ellipses.
  
- Changed: The original "Sketch Curves PFM" has had a major revamp and now works almost as well as Catmull Roms, however it uses more basic Bresenham calculations so has a reduced accuracy, but does result in a much faster processing time.
- Fixed: An issue where when hitting "Reset View", it sometimes had to be hit again.
- Fixed: An issue where zooming would behave in strange ways.
- Fixed: Issue where either the last or first shape wasn't drawn, primarily in curve modes.

### [v1.0.12-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.12-stable)
- Added: Text fields for Image Filter options
- Fixed: GCode Export, files will now have the correct offsets and orientation.

### [v1.0.11-stable](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.11-stable)
- Added: "vpype" integration, automatically open your drawing in vpype + run commands from presets with "File / Export to vpype"
- Added: New GCode Settings completely customisable GCode Commands / Orientation to support many more types of plotter.
- Added: You can now drag + drop images into the viewer
- Changed: Plotting task will now give more status updates than before.
- Changed: Export options will be greyed out until an image is imported.

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
