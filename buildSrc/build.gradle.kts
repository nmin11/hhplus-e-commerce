plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:mysql:1.19.3")
}
