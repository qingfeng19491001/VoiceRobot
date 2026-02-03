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
        viewBinding = true
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

        buildConfigField("String", "VOLC_ACCESS_KEY_ID", "\"${requireProp("VOLC_ACCESS_KEY_ID")}\"")
        buildConfigField("String", "VOLC_SECRET_ACCESS_KEY", "\"${requireProp("VOLC_SECRET_ACCESS_KEY")}\"")
        val sessionToken = props.getProperty("VOLC_SESSION_TOKEN") ?: ""
        buildConfigField("String", "VOLC_SESSION_TOKEN", "\"$sessionToken\"")

        // RTC demo params
        buildConfigField("String", "RTC_APP_ID", "\"${requireProp("rtc.appId")}\"")
        buildConfigField("String", "RTC_ROOM_ID", "\"${requireProp("rtc.roomId")}\"")
        buildConfigField("String", "RTC_USER_ID", "\"${requireProp("rtc.userId")}\"")
        buildConfigField("String", "RTC_TOKEN", "\"${requireProp("rtc.token")}\"")

        // Realtime AI demo params
        buildConfigField("String", "AI_TASK_ID", "\"${requireProp("ai.taskId")}\"")
        buildConfigField("String", "AI_AGENT_USER_ID", "\"${requireProp("ai.agent.userId")}\"")
        buildConfigField("String", "AI_WELCOME_MESSAGE", "\"${requireProp("ai.agent.welcomeMessage")}\"")
        buildConfigField("String", "AI_SYSTEM_MESSAGES", "\"${requireProp("ai.agent.systemMessages")}\"")

        buildConfigField("String", "AI_LLM_ENDPOINT_ID", "\"${requireProp("ai.llm.endPointId")}\"")
        buildConfigField("String", "AI_LLM_THINKING_TYPE", "\"${requireProp("ai.llm.thinkingType")}\"")
        buildConfigField("boolean", "AI_LLM_VISION_ENABLE", requireProp("ai.llm.visionEnable"))

        buildConfigField("String", "AI_ASR_API_RESOURCE_ID", "\"${requireProp("ai.asr.apiResourceId")}\"")
        buildConfigField("String", "AI_ASR_APP_ID", "\"${requireProp("ai.asr.appId")}\"")
        buildConfigField("String", "AI_ASR_ACCESS_TOKEN", "\"${requireProp("ai.asr.accessToken")}\"")

        buildConfigField("String", "AI_TTS_APP_ID", "\"${requireProp("ai.tts.appId")}\"")
        buildConfigField("String", "AI_TTS_ACCESS_TOKEN", "\"${requireProp("ai.tts.accessToken")}\"")
        buildConfigField("String", "AI_TTS_VOICE_TYPE", "\"${requireProp("ai.tts.voiceType")}\"")
        buildConfigField("int", "AI_TTS_SPEECH_RATE", requireProp("ai.tts.speechRate"))

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
    implementation(project(":rtc"))

    // AndroidX UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Support lib compatibility for RTC SDK transitive refs
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Lottie view used directly in app XML/code
    implementation("com.airbnb.android:lottie:6.4.0")

    // ViewModel + lifecycleScope/repeatOnLifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
