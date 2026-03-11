import java.util.Date
import java.text.SimpleDateFormat

plugins {
    //alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    /*
    * use for only project as build library
    * */
    alias(libs.plugins.android.library)
    `maven-publish`

}

android {
    namespace = "com.commlink.citl_nid_sdk"
    compileSdk = 36

    defaultConfig {
        //applicationId = "com.commlink.citl_nid_sdk"
        minSdk = 24
        testOptions.targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility =  JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }


    buildTypes {
        debug {
            buildConfigField("String", "NID_API_KEY", "\"your_debug_api_key_here\"")
            buildConfigField("String", "BASE_URL", "\"https://esign.digitalsignature.com.bd:7000/nidverify/\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "NID_API_KEY", "\"your_release_api_key_here\"")
            buildConfigField("String", "BASE_URL", "\"https://esign.digitalsignature.com.bd:7000/nidverify/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }

}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.commlink"
            artifactId = "citl-nid-sdk"
            version = project.findProperty("sdk_version") as? String ?: "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}


// ⚡ Put it HERE, outside android { } block
// ⚡ Kotlin DSL way to rename AAR
// Rename the AAR output with version and dynamic date
//#./gradlew :nid_sdk:assembleRelease
val sdkVersion = project.findProperty("sdk_version") as? String ?: "1.0.0"

val buildDate = SimpleDateFormat("yyyy_MM_dd").format(Date())

tasks.withType<com.android.build.gradle.tasks.BundleAar>().configureEach {
    if (name == "bundleReleaseAar") {
        archiveFileName.set("citl-nid-sdk-v$sdkVersion-$buildDate-release.aar")
    } else if (name == "bundleDebugAar") {
        archiveFileName.set("citl-nid-sdk-v$sdkVersion-$buildDate-debug.aar")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.face.detection)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.ucrop)
    implementation(libs.text.recognition.devanagari)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}