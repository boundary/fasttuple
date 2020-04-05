val jmhVersion = "1.23"

dependencies {
    implementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    implementation("nf.fr.eraasoft:objectpool:1.1.2")
    implementation("com.google.guava:guava:23.0")
    implementation("org.codehaus.janino:janino:3.1.0")
    implementation(project(":fasttuple-core"))
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
}

plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    application
}

application {
    mainClassName = "org.openjdk.jmh.Main"
}

tasks.shadowJar {
    archiveFileName.set("microbenchmarks.jar")
}