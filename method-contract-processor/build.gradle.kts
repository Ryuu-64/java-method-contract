plugins {
    `java-library`
}

group = "top.ryuu64.contract"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.github.Ryuu-64:java-method-contract-annotations:0.4.0")
    // https://mvnrepository.com/artifact/com.google.auto.service/auto-service-annotations
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    // https://mvnrepository.com/artifact/com.google.auto.service/auto-service
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

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
