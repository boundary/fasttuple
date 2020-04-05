val janinoVersion = "3.1.0"
val junitVersion = "5.6.1"

dependencies {
    implementation("org.codehaus.janino:janino:$janinoVersion")
    implementation("com.google.guava:guava:23.0")
    implementation("org.codehaus.janino:commons-compiler:$janinoVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}