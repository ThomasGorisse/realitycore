# RealityCore for Android


### Render 3D content in your Augmented Reality Android apps.

[![Maven Central](https://img.shields.io/maven-central/v/com.gorisse.thomas/realitycore.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.gorisse.thomas%22%20AND%20a:%22realitycore%22)


### INITIAL PUSH IN PROGRESS...
### DON'T TRY TO USE THE REPO YET.
### THIS MESSAGE WILL AUTO-DESTRUCT WHEN EVERYTHING IS READY FOR TEST.

Use the RealityCore SDK to implement high-performance 3D simulation and rendering.

RealityCore leverages information provided by the [ARCore SDK](https://github.com/google-ar/arcore-android-sdk) to seamlessly integrate virtual objects rendered by [Filament Engine](https://github.com/google/filament) into the real world.

![Logo](https://thomasgorisse.github.io/realitycore/images/logos/realitycore_logo.png)



## Usage benefits

* **Continuous compatibility** with the latests versions of [ARCore SDK](https://github.com/google-ar/arcore-android-sdk) and [Filament Engine](https://github.com/google/filament) 
* **Easy to use**: No OpenGL or Unity knowledges are needed. Simply use the SDK as any other Android depency requiring nothing more than standard Android app development knowledges.
* **Depth Mode**, **Augmented Images**, **Video Textures**, **HDR Lighting**, **Cloud Anchors** and **Augmented Face** supported.
* **AR or Non AR** (3D model only displaying) modes.
* **<a href="https://www.khronos.org/gltf/">glTF</a> stantdard format** is natively supported and can be loaded direcly from assets folder. glTF files can be loaded as an environment resource (including models, lights, cameras,...) and interpreted as a RealityCore scene with enties.
* **Animations** are standardized and made easy to use by the common Animator implementation.
* Available as gradle **Maven Central** dependency
* **Kotlin** based



## Dependencies
RealityCore is available on `mavenCentral()`.
*app/build.gradle*
```gradle
dependencies {
     implementation("com.gorisse.thomas:realitycore:1.0.0")
}
```

**[more...](https://thomasgorisse.github.io/realitycore/dependencies)**



## Basic Usage (Simple model viewer)


### Update your `AndroidManifest.xml`

*AndroidManifest.xml*
```xml
<uses-permission android:name="android.permission.CAMERA" />

<application>
    …
    <meta-data android:name="com.google.ar.core" android:value="optional" />
</application>
```

**[more...](https://thomasgorisse.github.io/realitycore/manifest)**


### Add the `View` to your `layout`
*res/layout/main_activity.xml*
```xml
<RealityView
    android:id="@+id/realityView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

**[sample...](samples/modelviewer/src/main/res/layout/activity_main.xml)**


### Edit your `Activity` or `Fragment`
*src/main/java/…/MainActivity.java*
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    …
    realityView = findViewById(R.id.realityView)
    
}
```

**[sample...](samples/gltf/src/main/java/com/google/ar/sceneform/samples/gltf/MainActivity.java)**


