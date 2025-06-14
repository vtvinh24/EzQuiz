plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.vtvinh24.ezquiz"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.vtvinh24.ezquiz"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Enable Room schema export
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)
    implementation(libs.fragment)

    // UI Components
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)

    // Architecture Components
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Room Components
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)  // You can switch to Moshi if preferred
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // JSON Processing
    implementation(libs.gson)

    // HTML Parsing (for Quizlet import)
    implementation(libs.jsoup)

    // Image Loading
    implementation(libs.glide)

    // WorkManager (for spaced repetition)
    implementation(libs.workmanager)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}