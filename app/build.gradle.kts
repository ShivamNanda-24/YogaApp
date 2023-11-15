plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.cvtest"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.cvtest"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.fragment:fragment:1.2.5")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("com.google.mediapipe:tasks-vision:0.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-core:1.2.0-alpha02")
    implementation("androidx.camera:camera-camera2:1.2.0-alpha02")
    implementation("androidx.camera:camera-lifecycle:1.2.0-alpha02")
    implementation("androidx.camera:camera-view:1.2.0-alpha02")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}