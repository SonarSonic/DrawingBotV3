# Change Log

### [v1.0.4-beta](https://github.com/SonarSonic/DrawingBotV3/releases/tag/v1.0.4-beta)
- Added: Presets for Drawing Sets + Drawing Pens
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
- Changed: GCode Settings has been split from "Drawing Area"
- Changed: Pressing ENTER will not start plotting anymore, avoiding accidentally overwriting your current plot.
- Changed: Pre-Processing will now be disabled by default
- Changed: Blend Modes won't lag so much when rendering
- Fixed: JPG Export not outputting a file
- Fixed: Potential Memory Leaks with JavaFX
- Fixed: Blend Modes will render as expected again & also appear properly in exports.


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
