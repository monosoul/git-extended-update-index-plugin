group = "com.github.monosoul"
version = "1.0"

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
    pluginName = "GIT skip-worktree"
    setPlugins("git4idea")
}