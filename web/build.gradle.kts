plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application
}

application {
    mainClass.set("will.sudoku.web.ApplicationKt")
}

repositories {
    mavenCentral()
}

// ============================================================
// Build-time version injection
// Generates version.properties with git commit hash and build
// timestamp, which is then bundled into the classpath.
// ============================================================
val generateVersionProperties by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/version")
    val versionProperties = outputDir.map { it.file("version.properties") }

    inputs.property("buildTime", System.currentTimeMillis())
    outputs.file(versionProperties)

    doLast {
        val gitCommit = try {
            val proc = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
                .directory(project.rootDir)
                .redirectErrorStream(true)
                .start()
            proc.inputStream.bufferedReader().readText().trim().takeIf { it.isNotEmpty() && !it.contains("fatal:") }
        } catch (_: Exception) {
            null
        }
        val buildTime = System.currentTimeMillis()

        val outputFile = versionProperties.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText("""
            gitCommit=${gitCommit ?: "unknown"}
            buildTime=${buildTime}
        """.trimIndent())
    }
}

// Wire generated properties into processResources
sourceSets {
    main {
        resources {
            srcDir(layout.buildDirectory.dir("generated/version"))
        }
    }
}

tasks.processResources {
    dependsOn(generateVersionProperties)
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core:3.4.3")
    implementation("io.ktor:ktor-server-netty:3.4.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.3")
    implementation("io.ktor:ktor-server-cors:3.4.3")
    implementation("io.ktor:ktor-server-rate-limit:3.4.3")
    
    // Kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Core solver module
    implementation(project(":kotlin"))
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host:3.4.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}
