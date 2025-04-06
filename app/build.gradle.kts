import java.util.regex.Pattern.compile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.quickchat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.quickchat"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)

    // Google Play Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.fido)

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // UI & Material Components
    implementation(libs.material)
    implementation(libs.material.v190)
    implementation(libs.circleimageview)
    implementation (libs.motiontoast)

    // Media & Image Processing
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.android.image.cropper)
    implementation(libs.glide)
    implementation(libs.coil)

    // Networking & JSON
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)

    // JWT Authentication
    implementation(libs.jwtdecode.v202)  // Decoding JWT
    implementation(libs.java.jwt)        // Generating JWT

    // Stream Chat SDK
    implementation(libs.stream.chat.android.ui.components) // UI components
    implementation(libs.stream.chat.android.offline)       // Offline support

    // Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.stream.chat.android.ui.components) // or latest version
    implementation (libs.stream.chat.android.offline)

    // Core SDK (required)
    implementation (libs.stream.chat.android.core)

    // UI Components (required for XML attributes)
    implementation (libs.stream.chat.android.ui.components)




}
