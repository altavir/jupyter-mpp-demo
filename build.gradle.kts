plugins {
    kotlin("multiplatform") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
    //use apply false as workaround
    kotlin("jupyter.api") version "0.10.0-53" apply false
    `maven-publish`
}

group = "ru.mipt.npm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion by project.extra("1.6.0")

kotlin {
    jvm()
    js(IR) {
        useCommonJs()
        browser {
            webpackTask {
                this.outputFileName = "js/jupyter-mpp.js"
            }
        }
        binaries.executable()
    }

    val jsBrowserDistribution by tasks.getting

    tasks.getByName<ProcessResources>("jvmProcessResources") {
        dependsOn(jsBrowserDistribution)
        from(jsBrowserDistribution)
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
            }
        }
        val jsMain by getting {

        }
    }
}

// workaround
plugins.apply("org.jetbrains.kotlin.jupyter.api")