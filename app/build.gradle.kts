import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.hotwalletappv2"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.hotwalletapp.v2"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "PRIVATE_KEY", "\"${localProperties.getProperty("PRIVATE_KEY") ?: ""}\"")
        buildConfigField("String", "WALLET_ADDRESS", "\"${localProperties.getProperty("WALLET_ADDRESS") ?: ""}\"")
        buildConfigField("String", "WALLET_PRIVATE_KEY2", "\"${localProperties.getProperty("WALLET_PRIVATE_KEY2") ?: ""}\"")
        buildConfigField("String", "WALLET_ADDRESS2", "\"${localProperties.getProperty("WALLET_ADDRESS2") ?: ""}\"")
        buildConfigField("String", "SERVER_ADDRESS", "\"${localProperties.getProperty("SERVER_ADDRESS") ?: ""}\"")
        buildConfigField("String", "SERVER_API_URL", "\"${localProperties.getProperty("SERVER_API_URL") ?: ""}\"")
        buildConfigField("String", "CONTRACT_ADDRESS", "\"${localProperties.getProperty("CONTRACT_ADDRESS") ?: ""}\"")
        buildConfigField("String", "INFURA_API_KEY", "\"${localProperties.getProperty("INFURA_API_KEY") ?: ""}\"")
    }

    packaging {
        resources {
            excludes += "META-INF/**"
        }
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
    val navVersion = "2.9.7"
    val lifecycleVersion = "2.10.0"
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // added
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("org.web3j:core:4.14.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.lightspark:compose-qr-code:1.0.1")
}
