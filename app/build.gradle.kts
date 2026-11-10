import java.net.URLEncoder
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHiltAndroid)
    alias(libs.plugins.kover)

    alias(libs.plugins.jetbrainsKotlinSerialization)
}

val localProperties =
    Properties().apply {
        rootProject
            .file("local.properties")
            .takeIf { it.exists() }
            ?.reader()
            ?.use(::load)
    }

fun mongoProperty(name: String): String = localProperties.getProperty(name, "")

fun mongoUri(): String {
    val user = URLEncoder.encode(mongoProperty("MONGODB_USER"), "UTF-8")
    val pass = URLEncoder.encode(mongoProperty("MONGODB_PASS"), "UTF-8")
    return "mongodb+srv://$user:$pass@${mongoProperty("MONGODB_HOST")}/"
}

android {
    namespace = "ar.edu.unlam.scaffoldingandroid3"
    compileSdk = 37

    defaultConfig {
        applicationId = "ar.edu.unlam.scaffoldingandroid3"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MONGODB_URI", "\"${mongoUri()}\"")
        buildConfigField("String", "MONGODB_DATABASE", "\"${mongoProperty("MONGODB_DATABASE")}\"")
        buildConfigField("String", "MONGODB_COLLECTION", "\"${mongoProperty("MONGODB_COLLECTION")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources.excludes += "META-INF/native-image/**"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Dagger + Hilt
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compiler)
    implementation(libs.google.dagger.hilt.android.testing)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil
    implementation(libs.coil)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.mongodb.driver.kotlin.coroutine)
}
