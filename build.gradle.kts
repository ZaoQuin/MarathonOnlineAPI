plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.jetbrains.kotlin.plugin.noarg") version "1.5.31"
}

group = "com.university"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("mysql:mysql-connector-java:8.0.33")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	//Mapper
	implementation("org.modelmapper:modelmapper:3.1.0")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.3.Final")

	//Mapper - Test
	testImplementation("org.mockito:mockito-core:5.0.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
	testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
	testImplementation("io.mockk:mockk:1.12.0")
	testImplementation ("org.modelmapper:modelmapper:2.4.4")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-config")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

	// WebClient
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// Jackson
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// Okhttp3
	implementation("com.squareup.okhttp3:okhttp:4.12.0")

	// json
	implementation("org.json:json:20250107")

	// Dotenv
	implementation("io.github.cdimascio:dotenv-java:2.3.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

noArg {
	annotation("javax.persistence.Entity")
}