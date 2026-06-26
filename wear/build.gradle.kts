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
    namespace = "com.zzzzzz.sleeptrigger.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zzzzzz.sleeptrigger"
        minSdk = 30
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
    implementation("androidx.core:core:1.17.0")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("androidx.health:health-services-client:1.1.0-rc02")
    implementation("com.google.guava:guava:33.4.8-android")
    testImplementation("junit:junit:4.13.2")
}
