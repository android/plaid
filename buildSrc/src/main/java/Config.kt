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

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

@Suppress("MayBeConstant") // Improve perf when changing values
object Config {

    fun RepositoryHandler.deps() {
        google().content {
            includeGroupByRegex("com.android.*")
            includeGroupByRegex("androidx.*")

            includeGroup("androidx.lifecycle")
            includeGroup("androidx.core")
            includeGroup("com.google.firebase")
            includeGroup("com.google.android.gms")
            includeGroup("com.google.android.material")
            includeGroup("com.google.gms")

            includeGroup("zipflinger")
        }

        jcenter().content {
            includeGroupByRegex("com.google.*")
            includeGroupByRegex("com.sun.*")
            includeGroupByRegex("com.squareup.*")
            includeGroupByRegex("com.jakewharton.*")
            includeGroupByRegex("com.googlecode.*")
            includeGroupByRegex("org.jetbrains.*")
            includeGroupByRegex("org.codehaus.*")
            includeGroupByRegex("org.apache.*")
            includeGroupByRegex("net.sf.*")
            includeGroupByRegex("javax.*")
            includeGroupByRegex("org.ow2.*")

            includeGroup("com.github.bumptech.glide")
            includeGroup("com.ibm.icu")
            includeGroup("com.nhaarman.mockitokotlin2")
            includeGroup("commons-io")
            includeGroup("commons-codec")
            includeGroup("commons-logging")
            includeGroup("it.unimi.dsi")
            includeGroup("junit")
            includeGroup("me.eugeniomarletti.kotlin.metadata")
            includeGroup("net.bytebuddy")
            includeGroup("net.java")
            includeGroup("org.abego.treelayout")
            includeGroup("org.antlr")
            includeGroup("org.bouncycastle")
            includeGroup("org.checkerframework")
            includeGroup("org.glassfish")
            includeGroup("org.glassfish.jaxb")
            includeGroup("org.hamcrest")
            includeGroup("org.jvnet.staxex")
            includeGroup("org.jsoup")
            includeGroup("org.mockito")
            includeGroup("org.objenesis")
            includeGroup("org.sonatype.oss")
            includeGroup("org.xerial")
            includeGroup("net.ltgt.gradle.incap")

            includeGroup("de.undercouch")
            includeGroup("org.jdom")

            excludeGroup("com.google.firebase")
            excludeGroup("com.google.android.gms")
            excludeGroup("com.google.android.material")
        }

        maven("https://dl.bintray.com/kotlin/kotlin-eap/").content {
            includeGroupByRegex("org.jetbrains.*")
        }

        maven("https://maven.fabric.io/public").content {
            includeGroupByRegex("io.fabric.*")
            includeGroupByRegex("com.crashlytics.*")
        }
    }
}

object Versions {
    val compileSdk = 29
    val targetSdk = 28
    val minSdk = 23
    val gradle_plugin = "4.0.0-alpha05"

    val appcompat = "1.1.0"
    val androidx = "1.0.0"
    val androidxCollection = "1.0.0"
    val androidxCoreRuntime = "2.1.0"
    val androidxArch = "2.0.0"
    val constraintLayout = "2.0.0-beta3"
    val coreKtx = "1.1.0"
    val coroutines = "1.3.2"
    val crashlytics = "2.10.1"
    val dagger = "2.23.2"
    val espresso = "3.1.0-beta02"
    val extJunit = "1.1.0"
    val fabric = "1.28.0"
    val firebase = "17.2.1"
    val glide = "4.9.0"
    val googleServices = "4.3.0"
    val gson = "2.8.5"
    val jsoup = "1.11.3"
    val junit = "4.12"
    val kotlin = "1.3.61"
    val ktlint = "0.24.0"
    val legacyCoreUtils = "1.0.0"
    val lifecycle = "2.2.0-rc03"
    val material = "1.1.0-alpha05"
    val mockito = "2.23.0"
    val mockito_kotlin = "2.0.0-RC3"
    val okhttp = "4.2.2"
    val retrofit = "2.6.0"
    val room = "2.2.2"
    val supportLibrary = "28.0.0"
    val test_rules = "1.1.0-beta02"
    val test_runner = "1.1.0-beta02"
    val ui_automator = "2.2.0-beta02"
    val viewPager2 = "1.0.0-beta02"
}

object Names {
    val applicationId = "io.plaidapp"
}
