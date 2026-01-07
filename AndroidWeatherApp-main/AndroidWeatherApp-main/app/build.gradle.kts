import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")

}

android {
    namespace = "com.example.firstapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.firstapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val localProperties = Properties().apply {
        load(rootProject.file("local.properties").inputStream())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            buildConfigField(
                "String",
                "API_KEY",
                "\"${localProperties.getProperty("API_KEY")}\""
            )
        }
        debug {
            buildConfigField(
                "String",
                "API_KEY",
                "\"${localProperties.getProperty("API_KEY")}\""
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
    buildFeatures{
        viewBinding = true
        buildConfig = true
        resValues = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.generativeai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // GSON
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    //life cycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // Gson
    implementation("com.google.code.gson:gson:2.9.1")

    // View model
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.fragment:fragment-ktx:1.8.2") // Thêm dependency này để dùng viewModels() trong Fragment
    
    implementation("com.github.Dimezis:BlurView:version-2.0.5")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // MPAndroidChart cho biểu đồ nhiệt độ
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
kapt{
    correctErrorTypes = true
}
