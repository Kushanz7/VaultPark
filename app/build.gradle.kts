plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.kushan.vaultpark"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kushan.vaultpark"
        minSdk = 23
        targetSdk = 36
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
    
    lint {
        abortOnError = false
        disable.addAll(listOf(
            "MissingTranslation",
            "ExtraTranslation",
            "DefaultLocale",
            "UnusedResources",
            "UseOfNonLambdaOffsetOverload",
            "ModifierParameter"
        ))
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Compose dependencies
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.google.zxing.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // DataStore for authentication persistence
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Firebase with explicit versions
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("com.google.firebase:firebase-common-ktx:21.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    
    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("com.google.guava:guava:33.0.0-android")
    
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    
    // Accompanist for permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Vico Charts for Compose
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")
    
    // Image loading for profile pictures
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Firebase Cloud Messaging for notifications
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    
    // Firebase Storage for profile picture uploads
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    
    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Pager for Onboarding
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    
    // SwipeRefresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Location Services (Needed for FusedLocationProviderClient)
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Retrofit (Keep for other uses if any, or remove if only used for Directions which we are deleting. Keeping for now as it might be used elsewhere or useful)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}