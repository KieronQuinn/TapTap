# Tap, Tap

**Download**: [![Latest release](https://img.shields.io/github/release/KieronQuinn/TapTap.svg?maxAge=3600&label=download)](https://github.com/KieronQuinn/TapTap/releases)

---

Tap, Tap is a port of the [double tap on back of device gesture](https://www.xda-developers.com/google-pixel-android-11-double-tap-rear-gestures/) from Android 11 to any ARM v8 device. It allows you to use the gesture to launch apps, control the device (including pressing the home, back and recents buttons), take a screenshot, toggle the flashlight, open your assistant and more. Using "gates", you're able to block the gesture from working in scenarios such as when the screen is off, when you're on a call, when an app is open and more. It uses an accessibility service to run these tasks and stay running in the background. Tap, Tap uses the same machine learning code and TensorFlow models from the Android 11 builds with the gesture, with code directly lifted from SystemUIGoogle where needed. You can pick from three models in the settings: Pixel 3 XL, Pixel 4 and Pixel 4 XL; allowing you to choose the one that fits your device best. Machine learning allows the gesture to be more accurately detected, and reduces the chance of accidental interactions. Sensitivity options are currently not available, but may be in a future release.

Tap, Tap is currently in alpha, and has been released at this time due to the interest around the feature, and its appearance in MIUI 13. Stay tuned for updates with new features, including gates for specific actions (eg. running an action only when a certain apps is open), and integration with apps such as Tasker. If you'd like to report a bug or suggest a feature, you can do so in this thread or on the GitHub.

---

Screenshots:

![Tap, Tap](https://i.imgur.com/n5jfNCN.png)

[Example video, showing launching the camera on double tap](https://streamable.com/4jd1mu)

[XDA thread](https://forum.xda-developers.com/android/apps-games/app-tap-tap-double-tap-device-gesture-t4140573)

