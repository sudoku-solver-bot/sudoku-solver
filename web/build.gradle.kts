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
    implementation("io.ktor:ktor-server-core:3.4.3")
    implementation("io.ktor:ktor-server-netty:3.4.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.3")
    implementation("io.ktor:ktor-server-cors:3.4.3")
    implementation("io.ktor:ktor-server-rate-limit:3.4.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation(project(":board"))
    implementation(project(":solver"))
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
        var totalErrors = 0

        fun error(msg: String) {
            println("  ERROR: $msg")
            totalErrors++
        }

        fun validatePuzzleString(puzzle: String, context: String) {
            if (puzzle.length != 81) {
                error("$context: Puzzle length ${puzzle.length} != 81")
            }
            if (puzzle.any { it != '.' && !it.isDigit() }) {
                error("$context: Puzzle contains invalid characters")
            }
        }

        fun hasRowColBoxDuplicate(puzzle: String): Boolean {
            if (puzzle.length != 81) return false
            for (r in 0..8) {
                val seen = mutableSetOf<Char>()
                for (c in 0..8) {
                    val ch = puzzle[r * 9 + c]
                    if (ch != '.' && ch != '0') {
                        if (!seen.add(ch)) return true
                    }
                }
            }
            for (c in 0..8) {
                val seen = mutableSetOf<Char>()
                for (r in 0..8) {
                    val ch = puzzle[r * 9 + c]
                    if (ch != '.' && ch != '0') {
                        if (!seen.add(ch)) return true
                    }
                }
            }
            for (br in 0..2) {
                for (bc in 0..2) {
                    val seen = mutableSetOf<Char>()
                    for (r in br * 3 until br * 3 + 3) {
                        for (c in bc * 3 until bc * 3 + 3) {
                            val ch = puzzle[r * 9 + c]
                            if (ch != '.' && ch != '0') {
                                if (!seen.add(ch)) return true
                            }
                        }
                    }
                }
            }
            return false
        }

        // ---- 1. Validate lessons.json ----
        println("Validating lessons from lessons.json...")
        val lessonsFile = File(resourcesDir, "lessons.json")
        if (!lessonsFile.exists()) throw GradleException("lessons.json not found")
        val lessonsContent = lessonsFile.readText().trim()

        val lessonIdPattern = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"")
        val lessonIds = lessonIdPattern.findAll(lessonsContent)
            .map { it.groupValues[1] }.toSet()
        println("  Found ${lessonIds.size} lesson entries")

        for (field in listOf("id", "title", "technique", "examplePuzzle")) {
            if (!lessonsContent.contains("\"$field\"")) {
                error("lessons.json: Required field '$field' not found")
            }
        }

        val lessonPuzzlePattern = Regex("\"examplePuzzle\"\\s*:\\s*\"([^\"]+)\"")
        for (match in lessonPuzzlePattern.findAll(lessonsContent)) {
            validatePuzzleString(match.groupValues[1], "lessons.json examplePuzzle")
        }

        val lessonDuplicates = lessonIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (lessonDuplicates.isNotEmpty()) {
            error("lessons.json: Duplicate IDs: $lessonDuplicates")
        }

        // ---- 2. Validate quizzes.json ----
        println("Validating quizzes from quizzes.json...")
        val quizzesFile = File(resourcesDir, "quizzes.json")
        if (!quizzesFile.exists()) throw GradleException("quizzes.json not found")
        val quizzesContent = quizzesFile.readText().trim()

        for (field in listOf("id", "belt", "questions")) {
            if (!quizzesContent.contains("\"$field\"")) {
                error("quizzes.json: Required field '$field' not found")
            }
        }

        val quizIdPattern = Regex("\"id\"\\s*:\\s*\"(quiz-[^\"]+)\"")
        val quizIds = quizIdPattern.findAll(quizzesContent)
            .map { it.groupValues[1] }.toList()
        println("  Found ${quizIds.size} quiz entries")

        val quizDuplicates = quizIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (quizDuplicates.isNotEmpty()) {
            error("quizzes.json: Duplicate quiz IDs: $quizDuplicates")
        }

        // Parse questions by splitting on question id pattern
        val questionSplitPattern = Regex("\"id\"\\s*:\\s*\"([^\"]*-q\\d+)\"")
        val questionStarts = questionSplitPattern.findAll(quizzesContent).map { it.range.first }.toList()

        var totalQuestions = 0
        for (i in questionStarts.indices) {
            val start = questionStarts[i]
            val end = if (i + 1 < questionStarts.size) questionStarts[i + 1] else quizzesContent.length
            val qJson = quizzesContent.substring(start, end)
            totalQuestions++

            val qIdMatch = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(qJson)
            val qId = qIdMatch?.groupValues?.get(1) ?: "unknown-q$totalQuestions"

            val acMatch = Regex("\"answerCell\"\\s*:\\s*(\\d+)").find(qJson)
            if (acMatch == null) {
                error("$qId: missing answerCell"); continue
            }
            val ac = acMatch.groupValues[1].toIntOrNull()
            if (ac == null || ac !in 0..80) {
                error("$qId: answerCell=$ac out of range 0-80"); continue
            }

            val puzzleMatch = Regex("\"puzzle\"\\s*:\\s*\"([^\"]+)\"").find(qJson)
            if (puzzleMatch == null) {
                error("$qId: missing puzzle"); continue
            }
            val puzzle = puzzleMatch.groupValues[1]
            validatePuzzleString(puzzle, "$qId puzzle")
            if (puzzle.length == 81 && puzzle[ac] != '.' && puzzle[ac] != '0') {
                error("$qId: answerCell $ac points to filled cell '${puzzle[ac]}'")
            }

            val avMatch = Regex("\"answerValue\"\\s*:\\s*\"([^\"]+)\"").find(qJson)
            if (avMatch == null) {
                error("$qId: missing answerValue")
            } else {
                val av = avMatch.groupValues[1]
                if (av.length != 1 || av[0] !in '1'..'9') {
                    error("$qId: answerValue='$av' is not a digit 1-9")
                }
            }

            val optMatch = Regex("\"options\"\\s*:\\s*\\[([^\\]]*)\\]").find(qJson)
            if (optMatch == null) {
                error("$qId: missing options array")
            } else {
                val optContent = optMatch.groupValues[1].trim()
                if (optContent.isEmpty()) {
                    error("$qId: options array is empty")
                } else {
                    val optCount = optContent.split(",").size
                    if (optCount < 2) {
                        error("$qId: options has only $optCount entry, need >= 2")
                    }
                }
            }

            val caMatch = Regex("\"correctAnswer\"\\s*:\\s*(\\d+)").find(qJson)
            if (caMatch == null) {
                error("$qId: missing correctAnswer")
            } else {
                val ca = caMatch.groupValues[1].toIntOrNull()
                if (ca == null) {
                    error("$qId: correctAnswer is not a valid integer")
                } else if (optMatch != null) {
                    val optContent = optMatch.groupValues[1].trim()
                    val optCount = if (optContent.isEmpty()) 0 else optContent.split(",").size
                    if (ca < 0 || ca >= optCount) {
                        error("$qId: correctAnswer=$ca out of range for $optCount options")
                    }
                }
            }

            val expMatch = Regex("\"explanation\"\\s*:\\s*\"([^\"]*)\"").find(qJson)
            if (expMatch == null || expMatch.groupValues[1].isBlank()) {
                error("$qId: missing or empty explanation")
            }

            val qtMatch = Regex("\"question\"\\s*:\\s*\"([^\"]*)\"").find(qJson)
            if (qtMatch == null || qtMatch.groupValues[1].isBlank()) {
                error("$qId: missing or empty question")
            }
        }
        println("  Validated $totalQuestions quiz questions")

        // ---- 3. Validate practice-puzzles.json ----
        println("Validating practice puzzles from practice-puzzles.json...")
        val practiceFile = File(resourcesDir, "practice-puzzles.json")
        if (!practiceFile.exists()) throw GradleException("practice-puzzles.json not found")
        val practiceContent = practiceFile.readText().trim()

        for (field in listOf("id", "technique", "puzzles")) {
            if (!practiceContent.contains("\"$field\"")) {
                error("practice-puzzles.json: Required field '$field' not found")
            }
        }

        val practiceIdPattern = Regex("\"id\"\\s*:\\s*\"(practice-[^\"]+)\"")
        val practiceIds = practiceIdPattern.findAll(practiceContent)
            .map { it.groupValues[1] }.toList()
        println("  Found ${practiceIds.size} practice puzzle sets")

        val practiceDuplicates = practiceIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (practiceDuplicates.isNotEmpty()) {
            error("practice-puzzles.json: Duplicate IDs: $practiceDuplicates")
        }

        val tutorialIdPattern = Regex("\"tutorialId\"\\s*:\\s*\"([^\"]+)\"")
        for (match in tutorialIdPattern.findAll(practiceContent)) {
            val tid = match.groupValues[1]
            if (tid !in lessonIds) {
                error("practice-puzzles.json: tutorialId '$tid' does not reference an existing lesson")
            }
        }

        val ppPattern = Regex("\"puzzle\"\\s*:\\s*\"([^\"]+)\"")
        var totalPP = 0
        for (match in ppPattern.findAll(practiceContent)) {
            val puzzle = match.groupValues[1]
            totalPP++
            validatePuzzleString(puzzle, "practice puzzle #$totalPP")
            if (hasRowColBoxDuplicate(puzzle)) {
                error("practice puzzle #$totalPP: has duplicate values in row/col/box")
            }
        }
        println("  Validated $totalPP practice puzzles")

        // ---- Summary ----
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
