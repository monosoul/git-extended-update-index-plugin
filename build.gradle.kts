group = "com.github.monosoul"
version = "0.0.1"

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
    val lombokDependency = "org.projectlombok:lombok:1.18.2"
    val junitVersion  = "5.3.1"

    annotationProcessor(lombokDependency)
    compileOnly(lombokDependency)
    testCompileOnly(lombokDependency)

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.mockito:mockito-core:2.23.0")
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
    }

    "check" {
        dependsOn(jacocoTestReport)
    }
}

repositories {
    jcenter()
}