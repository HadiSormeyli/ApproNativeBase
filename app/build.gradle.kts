import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

android {
    namespace = "com.approagency.base"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "store"

    productFlavors {
        create("bazar") {
            dimension = "store"
        }

        create("myket") {
            dimension = "store"
        }

        create("googlePlay") {
            dimension = "store"
        }
    }

    publishing {
        singleVariant("bazarRelease") {
            withSourcesJar()
        }

        singleVariant("myketRelease") {
            withSourcesJar()
        }

        singleVariant("googlePlayRelease") {
            withSourcesJar()
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
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
    api(libs.androidx.room.runtime)
    api(libs.play.services.auth.api.phone)
    api(libs.androidx.navigation.common.ktx)
    ksp(libs.androidx.room.compiler)
    api(platform(libs.koin.bom))
    api(libs.koin.android)
    api(libs.androidx.datastore.preferences)
    api(libs.retrofit)
    api(libs.retrofit.converter.gson)
    api(libs.okhttp)
    api(libs.okhttp.logging.interceptor)
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
    api(libs.androidx.navigation.compose)
    api(libs.koin.androidx.compose)
    api(libs.coil.compose)
    "bazarImplementation"(libs.poolakey)
    "myketImplementation"(libs.myket.billing)
    api(platform(libs.firebase.bom))
    api(libs.firebase.messaging)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("bazarPublication") {
                from(components["bazarRelease"])

                groupId = "com.github.HadiSormeyli"
                artifactId = "ApproNativeBase-bazar"
                version = System.getenv("VERSION")
                    ?: "local"

                pom {
                    name.set("ApproNativeBase Bazaar")
                    description.set(
                        "ApproNativeBase with Cafe Bazaar billing support"
                    )
                }
            }

            create<MavenPublication>("myketPublication") {
                from(components["myketRelease"])

                groupId = "com.github.HadiSormeyli"
                artifactId = "ApproNativeBase-myket"
                version = System.getenv("VERSION")
                    ?: "local"

                pom {
                    name.set("ApproNativeBase Myket")
                    description.set(
                        "ApproNativeBase with Myket billing support"
                    )
                }
            }

            create<MavenPublication>("googlePlayPublication") {
                from(components["googlePlayRelease"])

                groupId = "com.github.HadiSormeyli"
                artifactId = "ApproNativeBase-googleplay"
                version = System.getenv("VERSION")
                    ?: "local"

                pom {
                    name.set("ApproNativeBase Google Play")
                    description.set(
                        "ApproNativeBase for Google Play"
                    )
                }
            }
        }
    }
}