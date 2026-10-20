import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
}

// Supabase URL and publishable key, from the git-ignored local.properties
// (≈ the web app's .env). CI can pass them as environment variables instead.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun supabaseConfig(name: String): String = localProperties.getProperty(name)
    ?: System.getenv(name)
    ?: throw GradleException(
        "Add $name to local.properties, with the same value as the web app's NUXT_PUBLIC_$name.",
    )

android {
    namespace = "com.leireomadina.travelplanner"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.leireomadina.travelplanner"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${supabaseConfig("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${supabaseConfig("SUPABASE_KEY")}\"")
    }

    buildTypes {
        release {
            // R8: removes unused code and resources and shortens names, like a web
            // bundler's minifier and tree-shaking. Judge the app's speed on release builds.
            optimization {
                enable = true
            }
            // Signed with the debug key so release builds can be installed and tested
            // locally. Publishing to Google Play will need a real upload key instead.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    testOptions {
        // Robolectric needs the app's resources (strings, themes) to draw the screens
        unitTests.isIncludeAndroidResources = true
        // Robolectric's simulation of recent Android versions (36 included) reaches into a
        // part of Java that Java locks by default; this unlocks it for tests only
        unitTests.all {
            it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.ktor.client.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // Compose UI and navigation tests, run on the computer with Robolectric's simulated
    // Android instead of an emulator
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
    // Lets navigation tests swap the Supabase repository for FakeAuthRepository
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
