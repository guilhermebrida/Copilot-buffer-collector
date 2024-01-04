import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    kotlin("plugin.jpa") version "1.5.31"
}

group = "br.com.crearesistemas"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    runtimeOnly("org.postgresql:postgresql")

    /** database versioning **/
    implementation("org.flywaydb:flyway-core")

    /** JWT Token **/
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2") // or for gson runtimeOnly("io.jsonwebtoken:jjwt-gson:0.11.2")

    /** OpenApi 3.0 **/
    implementation("org.springdoc:springdoc-openapi-ui:1.5.11")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.11")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.integration:spring-integration-mqtt:5.5.6")
    implementation("com.google.code.gson:gson:2.8.6")

//	implementation( "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//	implementation( "org.jetbrains.exposed:exposed:0.32.1")
//	implementation ("org.postgresql:postgresql:42.2.23")
    implementation("org.xerial:sqlite-jdbc:3.34.0")
    api(fileTree("libs") { include("*.jar") })
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

springBoot {
    buildInfo()
}