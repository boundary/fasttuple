val janinoVersion by extra("3.1.0")
plugins {
    java
    signing
    `maven-publish`
    jacoco
    id("org.sonarqube") version "2.8"
}

allprojects {
    group = "com.nickrobison"
    version = "0.3.2-SNAPSHOT"

    apply(plugin = "signing")
    apply(plugin = "org.sonarqube")
    apply(plugin = "java")
    apply(plugin = "jacoco")

    repositories {
        jcenter()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withJavadocJar()
        withSourcesJar()
    }

    dependencies {
        val implementation by configurations
        implementation("org.codehaus.janino:janino:$janinoVersion")
        implementation("com.google.guava:guava:28.0-jre")
    }

    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
        }
    }
}



publishing {
    publications {
        create<MavenPublication>("mavenJava") {
        }
    }

    repositories {
        maven {
            credentials {
                val bintrayUsername: String? by project
                val bintrayPassword: String? by project
                username = bintrayUsername ?: System.getenv("BINTRAY_USER")
                password = bintrayPassword ?: System.getenv("BINTRAY_APIKEY")
            }
            val releasesRepoUrl = "https://api.bintray.com/maven/nickrobison/maven/fasttuple/;publish=1"
            val snapshotsRepoUrl = "http://oss.jfrog.org/artifactory/oss-snapshot-local"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            name = "bintray-nickrobison-maven"
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}