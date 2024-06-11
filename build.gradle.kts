
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"

    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
    kotlin("plugin.jpa") version "1.8.22"
}

group = "net.polix"
version = "1.0.4"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

val generatedVersionDir = "${buildDir}/resources/main"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}


repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}


tasks.getByName("processResources") {
    dependsOn("generateVersionProperties")
}


dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.session:spring-session-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation("org.springframework.boot:spring-boot-starter-log4j2") {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter") {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }

    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")

    implementation("io.netty:netty-all:4.1.84.Final")

    implementation("io.github.whilein.wcommons:wcommons-eventbus:1.5.4")
    implementation("io.github.whilein.wcommons:wcommons-config:1.5.4")

    implementation("net.minecrell:terminalconsoleappender:1.3.0")

    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
    implementation("org.apache.httpcomponents:httpmime:4.5.13")
    implementation("org.apache.commons:commons-lang3:3.13.0")
    implementation("commons-io:commons-io:2.14.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("dnsjava:dnsjava:3.2.2")

    implementation("com.vk.api:sdk:1.0.14")

}
tasks.getByName("processResources").dependsOn("generateVersionProperties")

tasks.register("generateVersionProperties") {
    doLast {
        val propertiesFile = file("$generatedVersionDir/info.properties")
        propertiesFile.parentFile.mkdirs()
        val properties = Properties()
        properties["version"] = rootProject.version
        propertiesFile.writer().use { properties.store(it, null) }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}


subprojects {
    apply("plugin" to "org.springframework.boot")
    apply("plugin" to "io.spring.dependency-management")
    apply("plugin" to "org.jetbrains.kotlin.jvm")
    apply("plugin" to "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "java")

    repositories {
        mavenCentral()

        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }

    }

    dependencies {
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        //implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.springframework.session:spring-session-core")

        implementation("dnsjava:dnsjava:3.2.2")

        developmentOnly("org.springframework.boot:spring-boot-devtools")
        //developmentOnly("org.springframework.boot:spring-boot-docker-compose")

        implementation("org.springframework.boot:spring-boot-starter-log4j2") {
            exclude("org.springframework.boot", "spring-boot-starter-logging")
        }
        implementation("org.springframework.boot:spring-boot-starter") {
            exclude("org.springframework.boot", "spring-boot-starter-logging")
        }

        implementation("io.netty:netty-all:4.1.84.Final")

        implementation("io.github.whilein.wcommons:wcommons-eventbus:1.5.4")
        implementation("io.github.whilein.wcommons:wcommons-config:1.5.4")

        implementation("org.apache.commons:commons-text:1.11.0")
        implementation("net.minecrell:terminalconsoleappender:1.3.0")


        implementation("com.google.code.gson:gson:2.9.0")
        implementation("com.google.api-client:google-api-client:2.2.0")

        // Integration
        implementation("org.apache.httpcomponents:httpclient:4.5.14")
        implementation("org.apache.httpcomponents:httpmime:4.5.14")
        implementation("org.apache.commons:commons-lang3:3.13.0")
        implementation("commons-io:commons-io:2.14.0")

        implementation("com.vk.api:sdk:1.0.14")
    }


    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "17"
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
        all {
            exclude("org.springframework.boot", "spring-boot-starter-logging")
        }
    }
}
