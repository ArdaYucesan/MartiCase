import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlinx-serialization")
    id("kotlin-kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}


android {
    namespace = "com.ardayucesan.marticase"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ardayucesan.marticase"
        minSdk = 29
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

        debug {
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    secrets {
        propertiesFileName = "secrets.properties"
        defaultPropertiesFileName = "secrets.defaults.properties"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.material.v190)

    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.bundles.koin)

    implementation(libs.play.services.maps)
    implementation(libs.play.services.location) // Lokasyon için

    implementation(libs.bundles.ktor)

    implementation(libs.android.maps.utils)

}