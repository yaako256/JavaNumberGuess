plugins {
    kotlin("jvm") version "2.2.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
}

application {
    mainClass.set("main.AppKt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "main.AppKt"
        )
    }
}