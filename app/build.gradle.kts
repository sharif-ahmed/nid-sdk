plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    /*
    * use for only project as build library
    * */
    //alias(libs.plugins.android.library)
}

android {
    namespace = "com.commlink.nid_sdk_demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.commlink.nid_sdk_demo"
        minSdk = 24
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //consumerProguardFiles("consumer-rules.pro")
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
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }

}

// ⚡ Put it HERE, outside android { } block
// ⚡ Kotlin DSL way to rename AAR
// Rename the AAR output
/*tasks.named<com.android.build.gradle.tasks.BundleAar>("assembleRelease") {
    archiveFileName.set("citl-nid-sdk-release.aar")
}
tasks.named<com.android.build.gradle.tasks.BundleAar>("ssembleDebugAar") {
    archiveFileName.set("citl-nid-sdk-debug.aar")
}*/

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
    implementation(project(":nid_sdk"))
    //implementation(files("libs/citl-nid-sdk-v1.0.0-release.aar"))
}