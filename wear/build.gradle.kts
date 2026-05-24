plugins {
    id("com.android.application")
}

android {
    namespace = "com.zzzzzz.sleeptrigger.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zzzzzz.sleeptrigger"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
}
