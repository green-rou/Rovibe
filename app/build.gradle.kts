import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps: Properties? = if (keystorePropsFile.exists()) {
    Properties().apply { load(keystorePropsFile.inputStream()) }
} else {
    null
}

val storePasswordVal: String? = keystoreProps?.getProperty("storePassword") ?: System.getenv("STORE_PASSWORD")
val keyAliasVal: String? = keystoreProps?.getProperty("alias") ?: System.getenv("KEY_ALIAS")
val keyPasswordVal: String? = keystoreProps?.getProperty("aliasPassword") ?: System.getenv("KEY_PASSWORD")

val keystoreStorePath: String? = keystoreProps?.getProperty("store")
val keystoreFile: File = if (keystoreStorePath != null) rootProject.file(keystoreStorePath) else file("rovibe.jks")
val canSign: Boolean = keystoreFile.exists() && storePasswordVal != null && keyAliasVal != null && keyPasswordVal != null

android {
    namespace = "com.greenrou.rovibe"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.greenrou.rovibe"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (canSign) {
        signingConfigs {
            create("release") {
                storeFile = keystoreFile
                storePassword = storePasswordVal
                keyAlias = keyAliasVal
                keyPassword = keyPasswordVal
            }
        }
    }

    buildTypes {
        release {
            if (canSign) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
