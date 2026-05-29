plugins {
    id("com.android.application")
}

android {
    namespace = "com.zzzzzz.sleeptrigger"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zzzzzz.sleeptrigger"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.health.connect:connect-client:1.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("junit:junit:4.13.2")
}
