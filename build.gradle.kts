import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

group = "com.github.monosoul"

plugins {
    id("org.jetbrains.intellij.platform") version "2.0.1"
    kotlin("jvm") version "1.9.25"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

intellijPlatform {
    buildSearchableOptions = true
    pluginConfiguration {
        name = "Git extended update-index"
        ideaVersion {
            untilBuild = ""
        }
    }

    publishing {
        token.set(
            project.findProperty("intellij.publish.token") as String?
        )
        channels.set(listOf("stable"))
    }
}

dependencies {
    intellijPlatform {
        create(type = "IC", version = "2024.1", useInstaller = false)
        bundledPlugin("Git4Idea")

        pluginVerifier()
        instrumentationTools()

        testFramework(TestFrameworkType.Platform)
    }

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("io.strikt:strikt-jvm:0.35.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.apache.commons:commons-lang3:3.17.0")
}

tasks {
    test {
        useJUnitPlatform()
        jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
        )

        testLogging {
            events = setOf(PASSED, SKIPPED, FAILED)
            exceptionFormat = FULL
        }
    }
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}
