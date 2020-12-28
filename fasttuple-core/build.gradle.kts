val junitVersion = "5.7.0"

dependencies {
    implementation("org.codehaus.janino:commons-compiler:${rootProject.ext.get("janinoVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

apply(plugin = "info.solidsoft.pitest")

configure<info.solidsoft.gradle.pitest.PitestPluginExtension> {
    junit5PluginVersion.set("0.12")
    targetClasses.add("com.nickrobison.tuple.*")
}

tasks.test {
    useJUnitPlatform()
}