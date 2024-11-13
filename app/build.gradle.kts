plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.callrecording"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.callrecording"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
android {
    // existing configurations

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/DEPENDENCIES.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}


dependencies {
    // AndroidX and Material Design dependencies
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.material:material:1.12.0")

    // AndroidX Lifecycle and Fragment
    implementation ("androidx.activity:activity-ktx:1.7.2")
    implementation ("androidx.fragment:fragment-ktx:1.6.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Testing dependencies
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
    dependencies {
        // Google Drive API and Google Play Services dependencies
        implementation ("com.google.android.gms:play-services-auth:20.5.0")
        implementation ("com.google.api-client:google-api-client:1.32.2")  // Older, stable version
        implementation ("com.google.api-client:google-api-client-android:1.32.2")  // Older, stable version
        implementation ("com.google.api-client:google-api-client-gson:1.32.2")  // Older, stable version
        implementation ("com.google.http-client:google-http-client-gson:1.41.5")
        implementation ("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")


    }

}
