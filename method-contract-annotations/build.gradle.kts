plugins {
    `java-library`
}

group = "top.ryuu64.contract"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
}

tasks.test {
    useJUnitPlatform()
}
