import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kfc.application)
}
group = "com.jder"
repositories {
    google()
    mavenCentral()
}
kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
            freeCompilerArgs.add("-Xjdk-release=21")
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.compose)
            implementation(libs.kotlinx.serialization.json)
        }
        jvmMain.dependencies {
            implementation(libs.bundles.compose.desktop)
        }
    }
}
compose.desktop.application {
    mainClass = "com.jder.MainKt"
    nativeDistributions {
        val icon = file("src/web/jder_icon.png")
        macOS.iconFile = icon
        windows.iconFile = icon
        linux.iconFile = icon
    }
}
spotless {
    kotlin {
        target("src/*/kotlin/**/*.kt")
        ktfmt().kotlinlangStyle()
    }
}
tasks.named<ProcessResources>("jvmProcessResources") {
    from("src/web/jder_icon.png")
    from("src/web/screenshots") { into("screenshots") }
    from("src/web/fonts") { into("fonts") }
}
tasks.named<Jar>("jvmJar") {
    archiveFileName = "JDER.jar"
    manifest.attributes["Main-Class"] = "com.jder.MainKt"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({ configurations["jvmRuntimeClasspath"].map { if (it.isDirectory) it else zipTree(it) } })
}
listOf("wasmJsBrowserDevelopmentVite", "wasmJsBrowserProductionVite").forEach {
    tasks.named(it) {
        inputs
            .files(fileTree("vite"))
            .withPropertyName("viteSources")
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }
}
tasks.register<Sync>("webApp") {
    from(tasks.named("wasmJsBrowserProductionVite"))
    into("src/web/app")
}
