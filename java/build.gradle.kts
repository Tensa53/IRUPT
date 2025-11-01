import javax.management.JMX

plugins {
    java
    jacoco
    id("me.champeau.jmh") version "0.7.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.24.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("org.jacoco:org.jacoco.core:0.8.13")
    testImplementation("org.jacoco:org.jacoco.agent:0.8.13")
    testImplementation("org.jacoco:org.jacoco.agent:0.8.13:runtime")
    testImplementation("com.nordstrom.tools:junit-foundation:9.4.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:3.10.0")
    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    implementation("org.openjdk.jmh:jmh-generator-bytecode:1.37")
}

tasks.test {
    useJUnit()

    doFirst {
        // resolve junit-foundation jar from testRuntimeClasspath
        val junitFoundation = configurations.testRuntimeClasspath.get()
            .resolvedConfiguration
            .resolvedArtifacts
            .first { it.name == "junit-foundation" }
            .file

        jvmArgs("-javaagent:$junitFoundation")
    }

    extensions.configure(JacocoTaskExtension::class) {
        excludes = listOf("org/example/runners/**")
        includes = listOf("org/example/banca/**", "org/example/utente/**")
        isJmx = true
    }

    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    finalizedBy(executionTimes)
    finalizedBy(uncoveredMethods)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("org/example/runners/**")
            }
        })
    )
}

tasks.register<JacocoReport>("jacocoExternalReport") {
    group = "verification"
    description = "Generates JaCoCo report from external exec file"

    // Path to the external exec file
    executionData.setFrom(file("build/jacoco/bench.exec"))

    // Source sets (adjust to your project)
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(
        fileTree("build/classes/java/main") {
            exclude(
                "**/runners/**",          // example of excluding packages
            )
        }
    )

    // Output formats
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(file("reports-coverage/jmh/jacoco/jacoco.xml"))
        html.outputLocation.set(file("reports-coverage/jmh/jacoco/html"))
    }
}

val executionTimes by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Extract execution times from test results XML"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.example.runners.SurefireXMLExecutionTimes")
    args("build/test-results/test/", "reports-time/junit/times.json")
}

val uncoveredMethods by tasks.registering(JavaExec::class) {
    dependsOn(tasks.jacocoTestReport)
    group = "verification"
    description = "Extract uncovered methods from JaCoCo XML report"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.example.runners.JaCoCoXMLUncoveredMethods")
    args("build/reports/jacoco/test/jacocoTestReport.xml", "reports-coverage/junit/uncovered.json")
}
