plugins {
    id("com.android.application") version "8.3.2"
    id("org.jetbrains.kotlin.android") version "2.0.21"
}

android {
    namespace = "com.javanumberguess"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.javanumberguess"
        minSdk = 24
        targetSdk = 34
    }

    compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}