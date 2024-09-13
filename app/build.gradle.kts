plugins {
//    alias(libs.plugins.android.application)
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
}

android {
    namespace = "com.example.imageuploaderapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.imageuploaderapp"
        minSdk = 24
        targetSdk = 34
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

//    kotlinOptions {
//        jvmTarget = "17"
//    }
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.kotlinstdlib)

    testImplementation(libs.junit)

    androidTestImplementation(libs.extjunit)
    androidTestImplementation(libs.espressocore)
//    implementation(libs.appcompat.v161)
    implementation(libs.core.ktx)
//    implementation(libs.constraintlayout)
//    implementation(libs.material.v190)
    implementation(libs.glide)
//    implementation("com.github.bumptech.glide:compiler:4.15.1")
}
