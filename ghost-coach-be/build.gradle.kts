plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.playmotech"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["jjwtVersion"] = "0.12.6"
extra["springdocVersion"] = "2.6.0"
extra["testcontainersVersion"] = "1.21.3"

dependencies {
    // Web & Core
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // DB
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-flyway")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwtVersion")}")

    // Utility
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")
    implementation("org.apache.tika:tika-core:2.9.2")

    // Dev
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:junit-jupiter:${property("testcontainersVersion")}")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    // Unit + slice tests only — keep this task fast.
    exclude("**/*IT.class")
    finalizedBy(tasks.jacocoTestReport)
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Integration tests using Testcontainers (Docker required)."
    group = "verification"
    useJUnitPlatform()
    include("**/*IT.class")
    shouldRunAfter(tasks.test)
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "com/playmotech/ghostcoach/GhostCoachApplication.class",
                    "com/playmotech/ghostcoach/**/dto/**",
                    "com/playmotech/ghostcoach/ai/dto/**",
                    "com/playmotech/ghostcoach/common/dto/**",
                    "com/playmotech/ghostcoach/**/Sport.class",
                    "com/playmotech/ghostcoach/**/ExperienceLevel.class",
                    "com/playmotech/ghostcoach/**/ChatRole.class",
                    "com/playmotech/ghostcoach/**/ConfidenceLevel.class",
                    "com/playmotech/ghostcoach/common/CommonController.class"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("ghost-coach-be.jar")
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    // Allow JDWP / extra JVM flags to be passed in from CLI: ./gradlew bootRun -PjvmArgs="-agentlib:..."
    val extra = (project.findProperty("jvmArgs") as String?)?.trim().orEmpty()
    if (extra.isNotEmpty()) {
        jvmArgs(extra.split(" ").filter { it.isNotBlank() })
    }
    // DevTools-friendly settings
    systemProperty("spring.devtools.restart.enabled", "true")
    systemProperty("spring.devtools.livereload.enabled", "false")
}
