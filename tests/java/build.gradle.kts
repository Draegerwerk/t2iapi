plugins {
    java
}

repositories {
    mavenCentral()
    flatDir {
        dirs("../../java/build/libs/")
    }
}

val configFile = File("../../config/versions.txt").readLines()
val configFileMap = configFile.associate { it.split("=")[0] to it.split("=")[1] }

val grpcVersion = configFileMap["JAVA_GRPC_VERSION"]
val protocVersion = configFileMap["JAVA_PROTOC_VERSION"]
val baseVersion = configFileMap["BASE_PACKAGE_VERSION"]!!
val buildId: String? = System.getenv("GITHUB_RUN_NUMBER")
val t2iapiVersion: String = when (System.getenv("RELEASE_VERSION") == "1") {
    true -> baseVersion
    false -> baseVersion + ( buildId?.let { ".$it" } ?: "" ) + "-SNAPSHOT"
}

dependencies {
    testImplementation("io.grpc:grpc-protobuf:${grpcVersion}")
    testImplementation("io.grpc:grpc-stub:${grpcVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    testImplementation("io.grpc:grpc-netty-shaded:${grpcVersion}")
    testImplementation("com.google.protobuf:protobuf-java-util:${protocVersion}")
    testImplementation("com.draeger.medical:t2iapi:${t2iapiVersion}")
}

tasks.register<JavaExec>("runJavaServer") {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.draeger.medical.t2iapi.helpers.JavaGrpcServer")
    val port = project.findProperty("port")?.toString() ?: "0"
    val testdata = project.findProperty("testdata")?.toString()
        ?: "src/test/resources/integration_scenarios.json"
    args = listOf(port, testdata)
    standardInput = System.`in`
}

tasks.register<JavaExec>("runJavaClient") {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.draeger.medical.t2iapi.helpers.JavaGrpcClient")
    val server = project.findProperty("server")?.toString()
    val testdata = project.findProperty("testdata")?.toString()
        ?: "src/test/resources/integration_scenarios.json"
    args = listOf(server, testdata)
}

tasks.withType<Test> {
    useJUnitPlatform()
    inputs.files(fileTree("../python") { include("**/*.py") })
        .withPropertyName("pythonReferenceImplementation")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    System.getProperty("python.executable")?.let { systemProperty("python.executable", it) }
    testLogging {
        events("passed", "skipped", "failed")
    }
}
