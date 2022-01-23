# Changes

This file details changes to the Columbus code from the original in SystemUI. Code changes to fit the Kotlin style are not included.

The aim of keeping the Columbus module separate is to make as few changes to it as possible, ideally only visibility changes. That way, code can easily be updated if Columbus itself changes.

### Refactoring

The package of all classes has been changed to (start with) com.google.android.columbus, rather than com.google.android.systemui.columbus, to differentiate it from the original code. The only exceptions to this are the AIDL classes, which are left as-is for compatibility.

The permission for Columbus access from other apps (currently unused) has been changed to `com.google.android.columbus.taptap.permission.CONFIGURE_COLUMBUS_GESTURE`

### Removals

`Dumpable` has been removed, all classes have had calls removed and no longer implement the interface.

### Visibility

**CHREGestureSensor**

Modifications allow for passing `contexthub` calls through Shizuku and root, as well as triple tap.

`CHREGestureSensor` -> open

`initializeContextHubClientIfNull` -> open

`sendMessageToNanoApp` -> open

`contextHubClientCallback` -> protected

`handleNanoappEvents` -> open

`handleGestureDetection` -> open

`screenOn` -> open

`updateScreenState` -> protected

**Columbus Service**

Modifications to allow triple tap

`ColumbusService` -> open

`GestureListener` -> open

`updateSensorListener` -> open

`actionListener` -> open

`updateActiveAction` -> protected

`deactivateGates` -> protected

`stopListening` -> open

`activateGates` -> protected

`blockingGate` -> protected

`blockingGate` -> protected

`startListening` -> protected

**Action**

Modifications to allow for triple tap and stopping service

`onGestureDetected` -> open

`listeners` -> protected

**Gate**

Modifications to allow for stopping service

`listeners` -> protected

**ColumbusServiceWrapper**

Modifications to allow for stopping service when killed, and reverting some code for Doze to work.

`ColumbusServiceWrapper` -> open 

**TapRT**

Modifications to allow for triple tap

`TapRT` -> open

`checkDoubleTapTiming` -> open

`mTimestampsBackTap` -> protected 

`mMinTimeGapNs` - public

`reset` -> open

`getModelFileName` -> open

`recognizeTapML` -> open

`mResampleAcc` -> protected 

`mPeakDetector` -> protected 

`mWasPeakApproaching` -> protected 

`mResampleGyro` -> protected 

`mAccZs` -> protected 

`mAccXs` -> protected 

`mAccYs` -> protected 

`mGyroXs` -> protected 

`mGyroYs` -> protected 

`mGyroZs` -> protected 

`mSizeFeatureWindow` -> protected 

`addToFeatureVector` -> protected 

**GestureSensorImpl**

Modifications to allow for triple tap

`GestureSensorImpl` -> open

`GestureSensorEventListener` -> open

`sensorEventListener` -> open

`tap` -> open

`samplingIntervalNs` -> protected

`isRunningInLowSamplingRate` -> protected

**GestureController** 

Modifications to allow shutting down service

`gestureSensor` -> public
