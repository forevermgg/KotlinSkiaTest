import org.apache.tools.ant.taskdefs.condition.Os
import org.apache.tools.ant.taskdefs.condition.Os.FAMILY_MAC
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    // id("org.jetbrains.compose") version "1.0.0"
}

group = "org.forever"
version = "1.0-SNAPSHOT"

val osName: String = System.getProperty("os.name")

val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val skikoVersion = "0.7.9" // or any more recent version
val skikoTarget = "${targetOs}-${targetArch}"
val lwjglVersion = "3.3.3"


val os = when {
    osName == "Mac OS X" -> "macos"
    osName == "Linux" -> "linux"
    osName.startsWith("Win") -> "windows"
    else -> throw Error("Unknown OS $osName")
}
println("osName = $osName targetOs = $targetOs os = $os")
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://packages.jetbrains.team/maven/p/skija/maven")
    google()
}


dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.skiko:skiko-awt-runtime-$skikoTarget:$skikoVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    implementation("org.lwjgl:lwjgl:${lwjglVersion}")
    implementation("org.lwjgl:lwjgl-glfw:${lwjglVersion}")
    implementation("org.lwjgl:lwjgl-opengl:${lwjglVersion}")
    implementation("org.lwjgl:lwjgl:${lwjglVersion}:natives-$os-$targetArch")
    implementation("org.lwjgl:lwjgl-glfw:${lwjglVersion}:natives-$os-$targetArch")
    implementation("org.lwjgl:lwjgl-opengl:${lwjglVersion}:natives-$os-$targetArch")
}

tasks.test {
    useJUnitPlatform()
}

java {
    // Java 8 is needed by jcommander's more recent versions
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaExec> {
    if (Os.isFamily(FAMILY_MAC)) {
        jvmArgs(
            // For macOS to run SWT under Cocoa.
            "-XstartOnFirstThread"
        )
    }
    // Assign all Java system properties from
    // the command line to the JavaExec task.
    systemProperties(System.getProperties().mapKeys { it.key as String })
}

tasks.withType<JavaExec> {
    if(DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-XstartOnFirstThread")
    }
}