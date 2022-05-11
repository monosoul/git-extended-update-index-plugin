import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.jvm.toolchain.JvmVendorSpec.ADOPTIUM
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.github.monosoul"
version = "0.1.1-SNAPSHOT"

plugins {
    id("org.jetbrains.intellij") version "1.5.3"
    kotlin("jvm") version "1.6.21"
    jacoco
}

intellij {
    version.set("211.6693.111")
    pluginName.set("Git extended update-index")
    updateSinceUntilBuild.set(true)
    sameSinceUntilBuild.set(false)
    plugins.set(listOf("git4idea"))
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.strikt:strikt-jvm:0.34.1")
    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(ADOPTIUM)
    }
}

tasks {
    val jacocoTestReport = "jacocoTestReport"(JacocoReport::class) {
        reports {
            xml.required.set(true)
            html.required.set(false)
        }
    }

    patchPluginXml {
        untilBuild.set(null as String?)
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
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}

repositories {
    mavenCentral()
}
