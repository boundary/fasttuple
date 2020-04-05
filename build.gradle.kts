val janinoVersion by extra("3.1.0")
plugins {
    signing
    `maven-publish`
}

allprojects {
    group = "com.nickrobison"
    version = "0.3.2-SNAPSHOT"

    apply(plugin = "java")
    apply(plugin = "signing")

    repositories {
        jcenter()
    }

    dependencies {
        val implementation by configurations
        implementation("org.codehaus.janino:janino:$janinoVersion")
        implementation("com.google.guava:guava:28.0-jre")
    }


    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
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
                val bintrayUsername: String by project
                val bintrayPassword: String by project
                username = bintrayUsername
                password = bintrayPassword
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