plugins {
    id("java")
//    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "io.github.codegrits"
version = "0.3.0"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/com.opencsv/opencsv
    implementation("com.opencsv:opencsv:5.7.1")
    // implementation("org.bytedeco:javacv-platform:1.5.9") // NOTE: This is too large
    implementation("org.bytedeco:javacv:1.5.9")
    implementation("org.bytedeco:ffmpeg:6.0-1.5.9")
    implementation("org.bytedeco:ffmpeg-platform:6.0-1.5.9")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    localPath.set( // Example paths
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            "D:/Program Files/JetBrains/IntelliJ IDEA 2024.3.4.1"
        } else {
            "/Applications/PyCharm.app/Contents"
        }
    )
//    version.set("2023.1.4")

//    type.set("IC") // IntelliJ Community Edition
//    type.set("IU") // IntelliJ Ultimate Edition
//    plugins.set(listOf("com.intellij.java"))

//    type.set("PC") // PyCharm Community Edition
//    type.set("PY") // PyCharm Professional Edition
//    plugins.set(listOf("PythonCore"))

//    type.set("CL") // CLion

//    type.set("PS") // PhpStorm
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
//    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = "17"
//    }

    patchPluginXml {
        sinceBuild.set("222") // 2022.2 NOTE Java 17 is now required
        untilBuild.set("252.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    val createOpenApiSourceJar by registering(Jar::class) {
        from(sourceSets.main.get().java) {
            include("**/api/**/*.java")
        }
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveClassifier.set("src")
    }

    buildPlugin {
        dependsOn(createOpenApiSourceJar)
        from(createOpenApiSourceJar) { into("lib/src") }
    }
}
