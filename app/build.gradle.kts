plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // Add Plugins
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "org.delcom.pam_p5_ifs23051"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "org.delcom.pam_p5_ifs23051"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"https://pam-2026-p5-ifs23051-be.yuriii.fun:8080/\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Add Library
    // ================================================
    // Material 3
    implementation(libs.androidx.compose.material3)
    // > Google Font
    implementation(libs.androidx.ui.text.google.fonts)
    // > Navhost
    implementation(libs.androidx.navigation.compose)
    // > Icon
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)

    // > Kotlin serialization
    implementation(libs.kotlinx.serialization.json)
    // > Coil
    implementation(libs.coil.compose)
    // > Retrofit
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    // > Okhttp3
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    // > Dagger
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // ================================================
}