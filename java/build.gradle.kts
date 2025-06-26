import com.google.protobuf.gradle.*
import org.jreleaser.model.Active
import java.time.LocalDate

plugins {
    `java-library`
    `maven-publish`
    id("com.google.protobuf") version "0.9.1"
    id("com.google.osdetector") version "1.7.1"
    id("org.jreleaser") version "1.17.0"
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

configure<org.jreleaser.gradle.plugin.JReleaserExtension> {
    gitRootSearch = true
    project {
        description = "t2iapi describes a product-independent interface to manipulate devices which utilize " +
            "ISO/IEEE 11073 SDC during verification."
        authors = listOf("T2I Team")
        license = "MIT"
        links {
            homepage = "https://github.com/Draegerwerk/t2iapi"
            bugTracker = "https://github.com/Draegerwerk/t2iapi/issues"
            contact = "t2i@draeger.com"
        }
        inceptionYear = "2022"
        vendor = "Draegerwerk AG & Co. KGaA"
        copyright = "Copyright (c) ${LocalDate.now().year} Draegerwerk AG & Co. KGaA"
    }

    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    subprojects.filter { it.name != "examples" }.forEach { project ->
                        stagingRepository(project.layout.buildDirectory.dir("staging-deploy").get().asFile.path)
                    }
                }
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
            name = "Sonatype"

            val releaseUrl = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
            val snapshotsUrl = "https://central.sonatype.com/repository/maven-snapshots/"

            url = uri(if (isRelease) releaseUrl else snapshotsUrl)
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
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