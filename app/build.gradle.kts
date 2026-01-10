import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.voicerobot"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.voicerobot"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val props = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) f.inputStream().use { load(it) }
        }

        fun requireProp(key: String): String {
            val v = props.getProperty(key)
            require(!v.isNullOrBlank()) { "Missing $key in local.properties" }
            return v
        }

        buildConfigField("String", "VOLC_APP_ID", "\"${requireProp("VOLC_APP_ID")}\"")
        buildConfigField("String", "VOLC_APP_KEY", "\"${requireProp("VOLC_APP_KEY")}\"")
        buildConfigField("String", "VOLC_ACCESS_TOKEN", "\"${requireProp("VOLC_ACCESS_TOKEN")}\"")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Feature modules
    implementation(project(":core"))
    implementation(project(":voiceengine"))
    implementation(project(":lottie"))

    // AndroidX UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lottie view used directly in app XML/code
    implementation("com.airbnb.android:lottie:6.4.0")

    // ViewModel + lifecycleScope/repeatOnLifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
