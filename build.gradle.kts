plugins {
    kotlin("multiplatform") version "1.5.20"
    //use apply false as workaround
    kotlin("jupyter.api") version "0.10.0-94-1" apply false
}

group = "ru.mipt.npm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR){
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

// workaround
plugins.apply("org.jetbrains.kotlin.jupyter.api")