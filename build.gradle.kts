import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.21" apply false
    id("org.sonarqube") version "4.4.1.3373"
}

allprojects {
    group = "will.sudoku"
    version = "1.0.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    
    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "sudoku-solver-bot_sudoku-solver")
        property("sonar.organization", "sudoku-solver-bot")
        property("sonar.host.url", "https://sonarcloud.io")
        
        // Code coverage
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.buildDir}/reports/jacoco/test/jacocoTestReport.xml")
        
        // Source directories
        property("sonar.sources", "kotlin/src/main/java,web/src/main/kotlin")
        property("sonar.tests", "kotlin/src/test/java,web/src/test/kotlin")
        
        // Exclusions
        property("sonar.exclusions", "**/generated/**,**/resources/**,**/*.html,**/*.css")
        property("sonar.test.exclusions", "**/generated/**,**/resources/**")
        
        // Kotlin specific
        property("sonar.kotlin.detekt.reportPaths", "${project.buildDir}/reports/detekt/detekt.xml")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
