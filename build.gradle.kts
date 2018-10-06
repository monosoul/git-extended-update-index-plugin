group = "com.github.monosoul"
version = "0.0.1"

plugins {
    id("org.jetbrains.intellij") version "0.3.11"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val ideaVersion = "182.4892.2"
intellij {
    version = System.getenv().getOrDefault("IDEA_VERSION", ideaVersion)
    pluginName = "Git extended update-index plugin"
    setPlugins("git4idea")
}

dependencies {
    val lombokDependency = "org.projectlombok:lombok:1.18.2"
    val junitVersion  = "5.3.1"

    annotationProcessor(lombokDependency)
    compileOnly(lombokDependency)
    testCompileOnly(lombokDependency)

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testCompile("org.assertj:assertj-core:3.11.1")
    testCompile("org.mockito:mockito-core:2.23.0")
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }
}

repositories {
    jcenter()
}