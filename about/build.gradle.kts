/*
 * Copyright 2019 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
}

apply(from = "$rootDir/shared_dependencies.gradle.kts")
apply(from = "$rootDir/test_dependencies.gradle.kts")

android {
    compileSdkVersion(Versions.COMPILE_SDK)

    defaultConfig {
        minSdkVersion(Versions.MIN_SDK)
        targetSdkVersion(Versions.TARGET_SDK)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("dagger.gradle.incremental" to "true")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dataBinding.isEnabled = true
}

dependencies {
    api(platform(project(":depconstraints")))
    kapt(platform(project(":depconstraints")))

    implementation(project(":app"))
    implementation(project(":core"))
    implementation(project(":bypass"))

    implementation(Libs.AX_VIEWPAGER2)
    implementation(Libs.SUPPORT_CUSTOMTABS)
    implementation(Libs.GLIDE)

    kapt("com.google.dagger:dagger-compiler")
}

kapt {
    useBuildCache = true
}
