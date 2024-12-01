import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Database support with Room
    id("kotlin-kapt")
}

android {
    namespace = "com.example.broke_no_more"
    compileSdk = 35

    // Load API key from api_key.properties
    val apiKeyPropertiesFile = rootProject.file("api_key.properties")
    val apiKeyProperties = Properties()
    if (apiKeyPropertiesFile.exists()) {
        apiKeyProperties.load(apiKeyPropertiesFile.inputStream())
    }

    defaultConfig {
        applicationId = "com.example.broke_no_more"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build Config Field for OpenAI API Key
        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"${apiKeyProperties["OPENAI_API_KEY"]}\""
        )
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "OPENAI_API_KEY",
                "\"${apiKeyProperties["OPENAI_API_KEY"]}\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "OPENAI_API_KEY",
                "\"${apiKeyProperties["OPENAI_API_KEY"]}\""
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.vision.common)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation (libs.androidx.viewpager2)
    implementation (libs.glide)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation (libs.easypermissions)



    // MPAndroidChart Library for graphing and charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.firebase.firestore.ktx)

    // Room for database
    val room_version = "2.6.0"
    val lifecycle_version = "2.6.2"
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ML Kit dependencies
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Retrofit for API communication
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp for handling HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}



