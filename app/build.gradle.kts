import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
}

android {
    namespace = "com.approagency.base"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

room {
    schemaDirectory(
        "$projectDir/schemas"
    )
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.play.services.auth.api.phone)
    implementation(libs.androidx.navigation.common.ktx)
    ksp(libs.androidx.room.compiler)
    api(platform(libs.koin.bom))
    api(libs.koin.android)
    testImplementation(libs.androidx.room.testing)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.activity.compose)
    api(libs.koin.compose)
    api(libs.androidx.compose.runtime)
    api(libs.splash)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.androidx.compose)
    implementation(libs.coil.compose)
    implementation(libs.poolakey)
    implementation(libs.myket.billing)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}

afterEvaluate {
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                from(project.components["release"])

                groupId =
                    System.getenv("GROUP")
                        ?: "com.github.HadiSormeyli"

                artifactId =
                    System.getenv("ARTIFACT")
                        ?: "ApproNativeBase"

                version =
                    System.getenv("VERSION")
                        ?: "local"
            }
        }
    }
}