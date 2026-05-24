import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    jacoco
}

group "will"
version "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":board"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.slf4j:slf4j-api:2.0.9")
    
    // Use junit-jupiter aggregator to fix Gradle test framework auto-loading deprecation
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes("Main-Class" to "will.sudoku.solver.Solver")
    }
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Sudoku puzzle cataloger"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("will.sudoku.solver.PuzzleCatalogerCliKt")
    // Pass all CLI args through
    args = (project.findProperty("cliArgs") as? String)?.split(" ") ?: emptyList()
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
    toolVersion = "0.8.14"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
}
