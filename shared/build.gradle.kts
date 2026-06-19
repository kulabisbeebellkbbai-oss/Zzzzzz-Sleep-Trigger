plugins {
    id("com.android.library")
}

android {
    namespace = "com.zzzzzz.sleeptrigger.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
