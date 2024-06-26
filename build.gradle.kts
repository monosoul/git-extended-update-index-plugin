import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

group = "com.github.monosoul"

plugins {
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.kotlinx.kover") version "0.8.1"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

intellij {
    version.set("223.7571.182")
    pluginName.set("Git extended update-index")
    updateSinceUntilBuild.set(true)
    sameSinceUntilBuild.set(false)
    plugins.set(listOf("vcs-git"))
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("io.strikt:strikt-jvm:0.34.1")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("org.apache.commons:commons-lang3:3.14.0")
}

tasks {
    publishPlugin {
        token.set(
            project.findProperty("intellij.publish.token") as String?
        )
        channels.set(listOf("stable"))
    }

    patchPluginXml {
        untilBuild.set("")
    }

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
}
