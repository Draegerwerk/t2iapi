import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import org.jreleaser.model.Active
import org.jreleaser.model.Http.Authorization

plugins {
    `java-library`
    `maven-publish`
    id("com.google.protobuf") version "0.9.1"
    id("com.google.osdetector") version "1.7.1"
    id("org.jreleaser") version "1.19.0"
    signing
}

val configFile = File("../config/versions.txt").readLines()
val configFileMap = configFile.associate { it.split("=")[0] to it.split("=")[1] }

val protocVersion = configFileMap["JAVA_PROTOC_VERSION"]
val grpcVersion = configFileMap["JAVA_GRPC_VERSION"]
val baseVersion = configFileMap["BASE_PACKAGE_VERSION"]!!
val buildId: String? = System.getenv("GITHUB_RUN_NUMBER")
val isRelease = System.getenv("RELEASE_VERSION") == "1"
val t2iapiVersion: String = when (isRelease) {
    true -> baseVersion
    false -> baseVersion + ( buildId?.let { ".$it" } ?: "" ) + "-SNAPSHOT"
}
val targetToStagingDeployRelease = layout.buildDirectory.dir("staging-deploy-release").get().asFile
val targetToStagingDeploySnapshot = layout.buildDirectory.dir("staging-deploy-snapshot").get().asFile

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

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
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
                url.set("https://github.com/Draegerwerk/t2iapi")
                description.set(
                    "t2iapi describes a product-independent interface to manipulate devices which utilize" +
                    " ISO/IEEE 11073 SDC during verification."
                )

                scm {
                    connection.set("scm:git:git://github.com/Draegerwerk/t2iapi.git")
                    developerConnection.set("scm:git:ssh://github.com/Draegerwerk/t2iapi.git")
                    url.set("https://github.com/Draegerwerk/t2iapi")
                }

                developers {
                    developer {
                        id.set("t2i")
                        name.set("T2I Team")
                        email.set("t2i@draeger.com")
                    }
                }

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
            name = "StagingForCentralPortal"
            url = uri(if (isRelease) targetToStagingDeployRelease else targetToStagingDeploySnapshot)
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("MAVEN_GPG_PRIVATE_KEY"), System.getenv("MAVEN_GPG_PASSPHRASE"))
    sign(publishing.publications["maven"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
    options.encoding = "UTF-8"
}

jreleaser {
    deploy {
        maven {
            mavenCentral {
                register("release-deploy") {
                    username = System.getenv("CENTRAL_PORTAL_USERNAME")
                    password = System.getenv("CENTRAL_PORTAL_TOKEN")
                    authorization = Authorization.BEARER
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository(targetToStagingDeployRelease.path)
                    sign = false // Signing is already handled by the signing plugin
                    checksums = true
                    sourceJar = true
                    javadocJar = true
                    verifyPom = true
                }
            }
            nexus2 {
                register("snapshot-deploy") {
                    username = System.getenv("CENTRAL_PORTAL_USERNAME")
                    password = System.getenv("CENTRAL_PORTAL_TOKEN")
                    authorization = Authorization.BEARER
                    active = Active.SNAPSHOT
                    url = "https://central.sonatype.com/repository/maven-snapshots/"
                    snapshotUrl = "https://central.sonatype.com/repository/maven-snapshots/"
                    snapshotSupported = true
                    stagingRepository(targetToStagingDeploySnapshot.path)
                    sign = false // Signing is already handled by the signing plugin
                    checksums = true
                    sourceJar = true
                    javadocJar = true
                    verifyPom = true
                }
            }
        }
    }
}