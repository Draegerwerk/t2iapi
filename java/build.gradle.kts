import com.google.protobuf.gradle.*

plugins {
    `java-library`
    `maven-publish`
    id("com.google.protobuf") version "0.9.1"
    id("com.google.osdetector") version "1.7.1"
}

val configFile = File("../config/versions.txt").readLines()
val configFileMap = configFile.associate { it.split("=")[0] to it.split("=")[1] }

val protocVersion = configFileMap["JAVA_PROTOC_VERSION"]
val grpcVersion = configFileMap["JAVA_GRPC_VERSION"]
val baseVersion = configFileMap["BASE_PACKAGE_VERSION"]!!
val buildId: String? = System.getenv("GITHUB_RUN_NUMBER")
val t2iapiVersion: String = when (System.getenv("RELEASE_VERSION") == "1") {
    true -> baseVersion
    false -> baseVersion + ( buildId?.let { ".$it" } ?: "" )
}

version = t2iapiVersion
group = "com.draeger.medical"


repositories {
    mavenCentral()
}

dependencies {
    api(group = "com.google.protobuf", name = "protobuf-java", version = protocVersion)
    api(group = "io.grpc", name = "grpc-all", version = grpcVersion)
    if (JavaVersion.current().isJava9Compatible) {
        // Workaround for @javax.annotation.Generated
        // see: https://github.com/grpc/grpc-java/issues/3633
        api("javax.annotation:javax.annotation-api:1.3.2")
    }

    protobuf(files("../src"))
}

tasks.compileJava {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:$protocVersion:${osdetector.classifier}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion:${osdetector.classifier}"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = rootProject.name
            version = version

            from(components["java"])
            pom {
                name.set("test 2 interface api")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ldeichmann/t2iapi")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}