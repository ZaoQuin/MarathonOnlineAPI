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
	runtimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	//JWT
	implementation("io.jsonwebtoken:jjwt:0.9.1")

	//Mapper
	implementation("org.modelmapper:modelmapper:3.1.0")

	//Mapper - Test

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core:5.0.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
	testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
	testImplementation("io.mockk:mockk:1.12.0")
	testImplementation ("org.modelmapper:modelmapper:2.4.4")
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
