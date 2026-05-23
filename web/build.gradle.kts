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
    
    // Core board data types
    implementation(project(":board"))
    // Core solver module
    implementation(project(":kotlin"))
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host:3.4.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ============================================================
// Build-time JSON data validation
// Validates tutorial data files (lessons, quizzes, practice-puzzles)
// ============================================================
val validateJsonData by tasks.registering {
    group = "verification"
    description = "Validate JSON data files for required fields and correct structure"

    val resourcesDir = file("src/main/resources/tutorials")

    doLast {
        val dataFiles = listOf(
            Triple("lessons.json", "lesson", listOf("id", "title", "technique", "examplePuzzle")),
            Triple("quizzes.json", "quiz", listOf("id", "belt", "questions")),
            Triple("practice-puzzles.json", "practice puzzle", listOf("id", "technique", "puzzle"))
        )

        var totalErrors = 0
        for ((fileName, dataType, requiredFields) in dataFiles) {
            val jsonFile = File(resourcesDir, fileName)
            println("Validating $dataType data from $fileName...")

            if (!jsonFile.exists()) {
                throw GradleException("$dataType file not found: ${jsonFile.absolutePath}")
            }

            val content = jsonFile.readText().trim()
            if (!content.startsWith("[") || !content.endsWith("]")) {
                throw GradleException("Expected JSON array in $fileName")
            }

            // Count objects and check fields
            val entryPattern = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"")
            val ids = entryPattern.findAll(content).map { it.groupValues[1] }.toList()
            println("  Found ${ids.size} $dataType entries")

            // Check for duplicate IDs
            val duplicates = ids.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            if (duplicates.isNotEmpty()) {
                println("  ERROR: Duplicate IDs: $duplicates")
                totalErrors += duplicates.size
            }

            // Check required fields exist in file
            for (field in requiredFields) {
                if (!content.contains("\"$field\"")) {
                    println("  ERROR: Required field '$field' not found in $fileName")
                    totalErrors++
                }
            }

            // Validate puzzle strings (81 chars, digits and 0s only)
            val puzzlePattern = Regex("\"(?:examplePuzzle|puzzle)\"\\s*:\\s*\"([0-9]+)\"")
            for (match in puzzlePattern.findAll(content)) {
                val puzzle = match.groupValues[1]
                if (puzzle.length != 81) {
                    println("  ERROR: Puzzle string length ${puzzle.length} != 81")
                    totalErrors++
                }
                if (puzzle.any { !it.isDigit() }) {
                    println("  ERROR: Puzzle contains non-digit characters")
                    totalErrors++
                }
            }
        }

        if (totalErrors > 0) {
            throw GradleException("JSON validation failed: $totalErrors error(s) found")
        }
        println("✅ All JSON data files valid")
    }
}

tasks.check {
    dependsOn(validateJsonData)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(25)
}
