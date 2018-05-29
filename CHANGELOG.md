# CHANGELOG
All notable changes to this project will be documented in this file.


## [3.75] - 2018-05-28
### Changed
- Fixed [Issue 11], Scrolling through Pen Sets

## [3.74] - 2018-05-28
### Added
- mouse_point(), displays corrdinates of mouse location in console.  Useful for locating vanishing points
- key binding to mouse_point()

### Changed
- grid() moved to Misc and cleaned up
- save_jpg() moved to Image_Tools
- CHANGELOG, changed extension to md

## [3.73] - 2018-02-03
### Added
- Dynamic "Path Finding Modules".  PFMs allow multiple artisic looks to be contained in its own file.
- PDF exports for individual pens
- Factor and bias support for convolution function
- Scaling and printing of convolution kernels
- Un-sharpen convolution kernel
- Links to latest released version and previous versions
- CHANGELOG

### Changed
- Boarders can now be stacked
- Allow for <ctrl> key combinations
- Remapped keyboard for displaying individual pens
- Most keyboard shortcuts now support up to 10 pens
- Log messages are now more verbose

## [3.72] - 2017-12-29
### Added
- Globals to overide the gcode decimal seperator
- Globals to set default digits to the right of the decimal point

## [3.71] - 2017-12-18
### Changed
- Fixed broken SVG code

## [3.7] - 2017-07-06
### Added
- SVG output
- Convolution kernels


[3.75]: https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2
[3.74]: https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2/commit/84f89ac1054614d241441854ea3942132c8431d0
[3.73]: https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2/commit/cea99bc4cd202536dc673f24f5344cc2b33f9265
[3.72]: https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2/commit/7741fda62995b3497900286f0296238262a57900
[3.71]: https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2/commit/a6339b3f1348de656c0e866cfe2e9a3ed121a58c
[3.7]: https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2/commit/6361bc68d49ddc13d31e605b74a9163f98086a66

[Issue 11]: https://github.com/Scott-Cooper/Drawbot_image_to_gcode_v2/issues/11