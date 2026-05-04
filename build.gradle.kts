plugins {
    id("io.micronaut.application") version "4.4.4"
}

version = "0.1"
group = "com.example"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("io.micronaut.data:micronaut-data-processor")
    annotationProcessor("io.micronaut:micronaut-inject-java")

    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")

    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:jdbc")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

application {
    mainClass.set("com.example.Application")
}

micronaut {
    version("4.7.6")
    testRuntime("junit5")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    val dockerHost = providers.environmentVariable("DOCKER_HOST")
        .orElse("unix:///Users/robertsjodahl/.docker/run/docker.sock")
    environment("DOCKER_HOST", dockerHost.get())
    environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/Users/robertsjodahl/.docker/run/docker.sock")
    jvmArgs("-Duser.home=${System.getProperty("user.home")}")
}

