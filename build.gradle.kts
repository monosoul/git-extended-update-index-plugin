import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

group = "com.github.monosoul"
version = "0.0.2"

plugins {
    id("org.jetbrains.intellij") version "0.4.15"
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

intellij {
    pluginName = "Git extended update-index"
    updateSinceUntilBuild = false
    sameSinceUntilBuild = true
    setPlugins("git4idea")
}

dependencies {
    val lombokDependency = "org.projectlombok:lombok:1.18.10"
    val junitVersion  = "5.5.2"

    annotationProcessor(lombokDependency)
    testAnnotationProcessor(lombokDependency)
    compileOnly(lombokDependency)
    testCompileOnly(lombokDependency)

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.assertj:assertj-core:3.14.0")
    testImplementation("org.mockito:mockito-core:2.28.2")
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
}

repositories {
    jcenter()
}