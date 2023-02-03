plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")

}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.sonsofcrypto.web3wallet.android"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    namespace = "com.sonsofcrypto.web3wallet.android"
}

dependencies {
    implementation(project(":web3lib"))
    implementation(project(":web3walletcore"))
    implementation(files("$rootDir/coreCrypto/build/android/coreCrypto.aar"))
    implementation(files("$rootDir/coreCrypto/build/android/coreCrypto-sources.jar"))
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha06")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${rootProject.ext["serialization_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${rootProject.ext["serialization_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${rootProject.ext["serialization_version"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.ext["coroutines_version"]}") {
        version { strictly("${rootProject.ext["coroutines_version"]}") }
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.ext["coroutines_version"]}") {
        version { strictly("${rootProject.ext["coroutines_version"]}") }
    }

    val composeVersion = "1.3.3"
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:1.4.0-alpha04")
    // implementation("androidx.activity:activity-compose:1.5.1")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.28.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("io.coil-kt:coil-compose:2.2.2")
}
