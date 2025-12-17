plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.parthenios.risemate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.parthenios.risemate"
        minSdk = 24
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    
    // ViewModel ve LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Swipe ve Touch için gerekli kütüphaneler
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // ViewPager2 for smooth swiping
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // Gesture detection
    implementation("androidx.core:core-ktx:1.12.0")

    // PhotoView for zoomable images
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Tink for crypto (AES-GCM, key management helpers)
    implementation("com.google.crypto.tink:tink-android:1.13.0")
    
    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // EXIF interface for image orientation
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    
    // UCrop for image cropping
    implementation("com.github.yalantis:ucrop:2.2.9")
    
    // PhotoView for zoomable images
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    
    // FlexboxLayout for flexible grid layout
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Animated bottom navigation
    implementation("nl.joery.animatedbottombar:library:1.1.0")
    
    // Google Play Billing for in-app purchases
    implementation("com.android.billingclient:billing-ktx:6.1.0")
    
    // Agora SDK for voice and video calls
    implementation("io.agora.rtc:full-sdk:4.2.6")
}