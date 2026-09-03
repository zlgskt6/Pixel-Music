// Sanitize environment variables for AGP compatibility
try {
    val processEnv = Class.forName("java.lang.ProcessEnvironment")
    val fields = listOf("theEnvironment", "theCaseInsensitiveEnvironment", "theUnmodifiableEnvironment")
    for (fieldName in fields) {
        try {
            val field = processEnv.getDeclaredField(fieldName)
            field.isAccessible = true
            val map = field.get(null) as? MutableMap<Any, Any>
            map?.keys?.filter { it.toString().equals("ANDROID_PREFS_ROOT", ignoreCase = true) }?.forEach { map.remove(it) }
        } catch (_: Throwable) {}
    }
} catch (_: Throwable) {}
System.clearProperty("ANDROID_PREFS_ROOT")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral {
            mavenContent {
                releasesOnly()
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = "JitPack"
                    setUrl("https://jitpack.io")
                }
            }
            filter {
                includeGroup("com.github.therealbush")
                includeGroup("com.github.TeamNewPipe")
            }
        }
    }
}

// F-Droid doesn't support foojay-resolver plugin
// plugins {
//     id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
// }

rootProject.name = "Pixel Music"
include(":app")
include(":core")
include(":lyrics:kugou")
include(":lyrics:lrclib")
include(":lyrics:simpmusic")
include(":lyrics:paxsenix")
include(":lyrics:betterlyrics")
include(":lyrics:unison")
include(":lastfm")
include(":canvas")
include(":shazamkit")
include(":spotifycore")

// Use a local copy of NewPipe Extractor by uncommenting the lines below.
// We assume, that Pixel Music and NewPipe Extractor have the same parent directory.
// If this is not the case, please change the path in includeBuild().
//
// For this to work you also need to change the implementation in core/build.gradle.kts
// to one which does not specify a version.
// From:
//      implementation(libs.newpipe.extractor)
// To:
//      implementation("com.github.TeamNewPipe:NewPipeExtractor")
//includeBuild("../NewPipeExtractor") {
//    dependencySubstitution {
//        substitute(module("com.github.TeamNewPipe:NewPipeExtractor")).using(project(":extractor"))
//    }
//}
