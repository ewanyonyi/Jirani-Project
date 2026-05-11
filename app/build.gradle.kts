plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val jiraniRemoteGatewayUrl = providers.gradleProperty("JIRANI_REMOTE_GATEWAY_URL")
    .orElse(providers.environmentVariable("JIRANI_REMOTE_GATEWAY_URL"))
    .orElse("http://10.0.2.2:8080")
    .get()
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
val jiraniRemoteGatewayToken = providers.gradleProperty("JIRANI_REMOTE_GATEWAY_TOKEN")
    .orElse(providers.environmentVariable("JIRANI_REMOTE_GATEWAY_TOKEN"))
    .orElse("")
    .get()
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.jirani.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jirani.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "JIRANI_REMOTE_GATEWAY_URL", "\"$jiraniRemoteGatewayUrl\"")
        buildConfigField("String", "JIRANI_REMOTE_GATEWAY_TOKEN", "\"$jiraniRemoteGatewayToken\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.nearby)
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
}
