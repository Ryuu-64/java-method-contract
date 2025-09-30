plugins {
    `java-library`
}

group = "top.ryuu64"
version = "0.2.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/com.google.auto.service/auto-service-annotations
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    // https://mvnrepository.com/artifact/com.google.auto.service/auto-service
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    // https://mvnrepository.com/artifact/com.google.testing.compile/compile-testing
    testImplementation("com.google.testing.compile:compile-testing:0.23.0")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    // https://mvnrepository.com/artifact/org.junit.platform/junit-platform-launcher
    testImplementation("org.junit.platform:junit-platform-launcher:1.13.4")
}

tasks.test {
    useJUnitPlatform()
}
