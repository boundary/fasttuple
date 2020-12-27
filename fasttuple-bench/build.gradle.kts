val jmhVersion = "1.27"

dependencies {
    implementation(project(":fasttuple-core"))
    implementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    implementation("nf.fr.eraasoft:objectpool:1.1.2")
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