# Tap, Tap

**Download**: [![Latest release](https://img.shields.io/github/release/KieronQuinn/TapTap.svg?maxAge=3600&label=download)](https://github.com/KieronQuinn/TapTap/releases)

---

Tap, Tap is a port of the [double tap on back of device gesture](https://www.xda-developers.com/google-pixel-android-11-double-tap-rear-gestures/) from Pixels running Android 12 to any Android 7.0+ device*. 

Tap, Tap provides over 50 actions that can be run from double or triple taps on the back of your device, including:

- Launching an app
- Launching a shortcut
- Toggling the flashlight
- Simulating button presses, such as home or back
- Running a Tasker task
- Quick Tap to Snap**

... and many more!

The gesture can also be fully controlled, to only run under certain conditions, or run different actions in different scenarios; such as only when an app is running, the screen is off, or when listening to music (many more options are available).

The sensitivity and response of the gesture can be controlled, picking from 8 "models", trained for devices ranging in size from 5.7" to 6.3" in height, as well as finer control of the sensitivity of gesture detection, meaning there is a configuration that will work well for most devices. 

Tap, Tap also tries to be easy on the battery, only running the gesture detection when required, and integrates with the "low power mode" available on select Pixel devices, otherwise using the lower-power machine learning capabilities of devices, where available.

\* Your device must have an accelerometer and gyroscope.

\** Requires a compatible Pixel device or root, see [this page](https://kieronquinn.co.uk/redirect/TapTap/qtts) for more info.

## Screenshots

[![Tap, Tap](https://i.imgur.com/oN3Iimol.png)](https://i.imgur.com/oN3Iimo.png)

[Example video, showing launching the camera on double tap](https://streamable.com/4jd1mu)

[XDA thread](https://kieronquinn.co.uk/redirect/TapTap/xda)

## Reporting issues and feature requests

If you have encountered a crash or error, please make an issue on the [Issues](https://github.com/KieronQuinn/TapTap/issues) page. For crashes, include a crash report, which Tap, Tap should create for you and show a notification after a hard crash (enable the option on the "More" page first if you have disabled it). Before making an issue, make sure it has not been reported before, and does not fall under the "service killed" pinned issue, as those will be closed and ignored.

To request a feature, either make an issue stating "Feature Request", or post a reply in the XDA thread. Note that only relatively simple features will be considered, anything more complex including toggling specific system features should be done using the Tasker capabilities of Tap, Tap, as the app is not meant to be a Tasker replacement.

## Building Tap, Tap

If you would prefer to build from source, Tap, Tap can be built like any other Android app, simply by opening the source in Android Studio and running the app.
