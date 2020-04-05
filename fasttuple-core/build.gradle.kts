dependencies {
    "implementation"("org.codehaus.janino:janino:3.1.0")
    "implementation"("org.codehaus.janino:commons-compiler:3.1.0")
    "implementation"("com.google.guava:guava:23.0")
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.6.1")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.6.1")
}

tasks.test {
    useJUnitPlatform()
}