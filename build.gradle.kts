val janinoVersion by extra("3.1.0")

allprojects {
    group = "com.nickrobison"
    version = "0.3.2-SNAPSHOT"

    apply(plugin = "java")

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