import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("kapt") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	kotlin("plugin.jpa") version "2.1.0"
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("nu.studer.jooq") version "8.2"
}

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmToolchain(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.retry:spring-retry:2.0.11")

	// DB
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.jooq:jooq")
	implementation("org.redisson:redisson-spring-boot-starter:3.45.1")

	jooqGenerator("com.mysql:mysql-connector-j")

	runtimeOnly("com.mysql:mysql-connector-j")

	// Cache
	implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

	// Kafka
	implementation("org.springframework.kafka:spring-kafka")

	// Docs
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:kafka:1.19.3")
	testImplementation("org.testcontainers:mysql")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets["main"].java {
    srcDir("build/generated/jooq/main")
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
}

val mySqlContainer: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
    .withDatabaseName("hhplus")
    .withUsername("root")
    .withPassword("root")
    .withReuse(true)
extra["mySqlContainer"] = mySqlContainer

tasks.register("startMysqlContainer") {
    doFirst {
        mySqlContainer.start()

        val connection = DriverManager.getConnection(
            mySqlContainer.jdbcUrl,
            mySqlContainer.username,
            mySqlContainer.password
        )
        val ddl = file("src/main/resources/database/schema.sql").readText()
        connection.use { conn ->
            conn.createStatement().use { stmt -> stmt.execute(ddl) }
        }
    }
}

jooq {
    version.set("3.19.2")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
        }
    }
}

tasks.named("generateJooq") {
    dependsOn("startMysqlContainer")

    doFirst {
        val container = extra["mySqlContainer"] as MySQLContainer<*>
        val extension = extensions.getByType(nu.studer.gradle.jooq.JooqExtension::class.java)

        extension.configurations.named("main").configure {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "com.mysql.cj.jdbc.Driver"
                    url = container.jdbcUrl
                    user = container.username
                    password = container.password
                }
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = container.databaseName
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "kr.hhplus.jooq"
                        directory = "build/generated/jooq/main"
                    }
                }
            }
        }
    }

    doLast {
        val container = extra["mySqlContainer"] as MySQLContainer<*>
        if (container.isRunning) container.stop()
    }
}
