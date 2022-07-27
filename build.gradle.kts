import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.monosoul"
version = "0.1.2"

plugins {
    id("org.jetbrains.intellij") version "1.5.3"
    kotlin("jvm") version "1.7.10"
    jacoco
}

intellij {
    version.set("222.3345.118")
    pluginName.set("Git extended update-index")
    updateSinceUntilBuild.set(true)
    sameSinceUntilBuild.set(false)
    plugins.set(listOf("git4idea"))
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("io.strikt:strikt-jvm:0.34.1")
    testImplementation("io.mockk:mockk:1.12.5")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks {
    jacocoTestReport  {
        reports {
            xml.required.set(true)
            html.required.set(false)
        }

        dependsOn(test)
    }

    patchPluginXml {
        untilBuild.set(null as String?)
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

    "check" {
        dependsOn(jacocoTestReport)
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}

repositories {
    mavenCentral()
}
