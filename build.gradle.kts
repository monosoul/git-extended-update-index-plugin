import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.monosoul"
version = "0.0.3"

plugins {
    id("org.jetbrains.intellij") version "0.7.2"
    kotlin("jvm") version "1.4.32"
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

    val junitVersion = "5.7.1"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.14.0")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
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