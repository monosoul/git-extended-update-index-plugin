import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.monosoul"
version = "0.0.2"

plugins {
    id("org.jetbrains.intellij") version "0.4.15"
    kotlin("jvm") version "1.3.61"
    jacoco
}

intellij {
    pluginName = "Git extended update-index"
    updateSinceUntilBuild = false
    sameSinceUntilBuild = true
    setPlugins("git4idea")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.apache.commons:commons-lang3:3.9")
}

tasks {
    val jacocoTestReport = "jacocoTestReport"(JacocoReport::class) {
        reports {
            xml.isEnabled = true
            html.isEnabled = false
        }
    }

    "test"(Test::class) {
        useJUnitPlatform()

        testLogging {
            events = setOf(PASSED, SKIPPED, FAILED)
            exceptionFormat = FULL
        }
    }

    "check" {
        dependsOn(jacocoTestReport)
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}

repositories {
    jcenter()
}