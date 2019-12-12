import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

fun RepositoryHandler.addCommonRepositories() {
    google {
        content {
            includeGroupByRegex("com\\.android.*")
            includeGroupByRegex("androidx.*")
            includeGroup("android.arch.lifecycle")
            includeGroup("android.arch.core")
            includeGroup("com.google.firebase")
            includeGroup("com.google.android.gms")
            includeGroup("com.google.android.material")
            includeGroup("com.google.gms")
            includeGroup("zipflinger")
        }
    }

    jcenter {
        content {
            includeGroupByRegex("com\\.google.*")
            includeGroupByRegex("com\\.sun.*")
            includeGroupByRegex("com\\.squareup.*")
            includeGroupByRegex("com\\.jakewharton.*")
            includeGroupByRegex("com\\.googlecode.*")
            includeGroupByRegex("org\\.jetbrains.*")
            includeGroupByRegex("org\\.codehaus.*")
            includeGroupByRegex("org\\.apache.*")
            includeGroupByRegex("net\\.sf.*")
            includeGroupByRegex("javax.*")
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
            includeGroup("org.ow2.asm")
            includeGroup("org.sonatype.oss")
            includeGroup("org.xerial")
            includeGroup("net.ltgt.gradle.incap")

            excludeGroup("com.google.firebase")
            excludeGroup("com.google.android.gms")
            excludeGroup("com.google.android.material")
        }
    }

    maven("https://maven.fabric.io/public") {
        content {
            includeGroupByRegex("io\\.fabric.*")
            includeGroupByRegex("com\\.crashlytics.*")
        }
    }

    maven("https://dl.bintray.com/kotlin/kotlin-eap") {
        content {
            includeGroupByRegex("org\\.jetbrains.*")
        }
    }
}