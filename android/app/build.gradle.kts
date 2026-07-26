plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
}

android {
    namespace = "com.ytdwn.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ytdwn.app"
        minSdk = 24
        targetSdk = 34
        versionCode = project.findProperty("VERSION_CODE")?.toString()?.toInt() ?: 1
        versionName = project.findProperty("VERSION_NAME")?.toString() ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }


    signingConfigs {
        create("release") {
            // Support passing signing config via project properties
            storeFile = project.findProperty("RELEASE_STORE_FILE")?.let { file(it) }
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config if available, fallback to debug if not set
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile?.exists() == true } ?: signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

chaquopy {
    defaultConfig {
        version = "3.12"
        buildPython("python")
        pip {
            install("pytubefix")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // FFmpegKit (Maintained Community Fork)
    implementation("dev.ffmpegkit-maintained:ffmpeg-kit-full:8.1.7")
    
    // Storage
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Image Loading (Coil)
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Networking (Prepared for future API calls)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
