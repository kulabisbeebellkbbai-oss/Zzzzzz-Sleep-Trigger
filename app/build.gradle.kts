import java.util.Properties

plugins {
    id("com.android.application")
}

val uploadKeyPropertiesFile = file(
    providers.environmentVariable("ANDROID_UPLOAD_KEY_PROPERTIES").orNull
        ?: "${System.getProperty("user.home")}/.local/share/codex/android-upload-keys/com.zzzzzz.sleeptrigger.properties"
)
val uploadKeyProperties = Properties()
val hasUploadKey = uploadKeyPropertiesFile.isFile
if (hasUploadKey) {
    uploadKeyPropertiesFile.inputStream().use(uploadKeyProperties::load)
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

    signingConfigs {
        if (hasUploadKey) {
            create("upload") {
                storeFile = file(uploadKeyProperties.getProperty("storeFile"))
                storePassword = uploadKeyProperties.getProperty("storePassword")
                keyAlias = uploadKeyProperties.getProperty("keyAlias")
                keyPassword = uploadKeyProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (hasUploadKey) {
                signingConfig = signingConfigs.getByName("upload")
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("androidx.health.connect:connect-client:1.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("junit:junit:4.13.2")
}
