# Dependencies


## RELEASES

*/build.gradle*
```gradle
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

*app/build.gradle*
```gradle
dependencies {
     implementation("com.gorisse.thomas:realitycore:1.0.0")
}
```


## SNAPSHOT

*/build.gradle*
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

*app/build.gradle*
```gradle
dependencies {
    implementation 'com.github.ThomasGorisse.realitycore:realitycore:-SNAPSHOT'
}
```