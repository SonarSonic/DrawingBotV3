# Drawing Bot V3
![Screenshot](https://github.com/SonarSonic/Drawbot_image_to_gcode_v3/blob/master/images/ScreenshotV3.JPG?raw=true)
![GitHub top language](https://img.shields.io/github/languages/top/SonarSonic/DrawingBotV3)
![GitHub](https://img.shields.io/github/license/SonarSonic/DrawingBotV3)

### About
Drawing Bot is a free, open source software for converting images to line drawings for Plotters / Drawing Machines / 3D printers. It also serves as an application for visual artists to create stylised line drawings from images / video.

### Features
- Advanced User Interface with live drawing preview
- GCode - configurable Drawing Area, XYZ Offsets / Auto Homing.
- Path Finding Modules - configurable to create different styles
- Pen Settings: configurable colours / distribution weight / blend modes
- Exports can be exported per/pen or per/drawing
- Batch Processing: Convert entire folders automatically.

#### Planned Features
- Pen Plugins (Support for multiple manufacturers)
- Path Finder Presets: user exportable / for sharing different styles with other users

##### Supported File Types
```text
Import Formats: 
    Images: [.tif, .tga, .png, .jpg, .gif, .bmp, .jpeg] 
       
Export Formats: 
    GCode: [.gcode, .txt],
    Vectors: [.svg, .pdf],
    Images: [.tif, .tga, .png, .jpg, .jpeg]
```

##### Dependencies

- [Processing](https://github.com/processing/processing) - for Rendering/Exporting
- [ImgScalr](https://github.com/rkalla/imgscalr) - for Optimised Image Scaling
- [Gson](https://github.com/google/gson) - for Configuration Files

### Original Version
DrawingBotV3 is an expansion of [Drawbot Image to GCode V2](https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2) originally written by **Scott Cooper**. Thanks to Scott for allowing me to publish this version!