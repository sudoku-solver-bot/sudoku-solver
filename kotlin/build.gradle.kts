import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("me.champeau.jmh") version "0.7.3"
    jacoco
}

group "will"
version "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.slf4j:slf4j-api:2.0.9")
    
    // Use junit-jupiter aggregator to fix Gradle test framework auto-loading deprecation
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.assertj:assertj-core:3.26.3")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to "will.sudoku.solver.Solver")
    }
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Sudoku solver with a sample puzzle"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("will.sudoku.solver.Solver")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // Report is always generated after tests run
    
    // Limit memory usage on constrained environments
    maxParallelForks = 1
    jvmArgs = listOf("-Xmx384m")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Tests are required to run before generating the report
    
    reports {
        xml.required.set(true) // XML report for SonarCloud
        csv.required.set(false)
        html.required.set(true) // HTML report for local viewing
    }
}

jacoco {
    toolVersion = "0.8.11"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}
