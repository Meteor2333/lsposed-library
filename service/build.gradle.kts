plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.publish.central)
    id("signing")
}

android {
    namespace = "io.github.libxposed.service"
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    androidResources.enable = false

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    compileOnly(libs.annotation)
}

val androidJavadoc by tasks.registering(Javadoc::class) {
    title = "LSPosed Service $version"
    source(android.sourceSets["main"].java.srcDirs)
    destinationDir = layout.buildDirectory.dir("javadoc").get().asFile

    (options as StandardJavadocDocletOptions).apply {
        links("https://docs.oracle.com/en/java/javase/17/docs/api/")
        links("https://developer.android.com/reference/")
        encoding = "UTF-8"
        charSet = "UTF-8"
        docEncoding = "UTF-8"
        addBooleanOption("Xdoclint:all,-missing", true)
    }

    isFailOnError = false

    val bootCp = project.extensions.getByType<com.android.build.api.variant.LibraryAndroidComponentsExtension>()
        .sdkComponents.bootClasspath

    doFirst {
        classpath = files(bootCp.get()) + configurations["releaseCompileClasspath"]
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    dependsOn(androidJavadoc)
    from(androidJavadoc.map { it.destinationDir!! })
}

publishing {
    publications {
        register<MavenPublication>("service") {
            afterEvaluate {
                artifact(javadocJar)
                from(components["release"])
            }
            pom {
                developers {
                    developer {
                        name.set("libxposed")
                        url.set("https://libxposed.github.io")
                    }
                    developer {
                        name.set("Meteor23333")
                        url.set("https://meteormc.cc")
                    }
                }
            }
        }
    }
}

publishOnCentral {
    repoOwner.set("Meteor2333")
    projectDescription.set("Modern Xposed Service")
    projectLongName.set(project.name)
    licenseName.set("Apache License 2.0")
    licenseUrl.set("https://raw.githubusercontent.com/${repoOwner.get()}/lsposed-library/master/LICENSE")
    projectUrl.set("https://github.com/${repoOwner.get()}/lsposed-library/blob/master/service")
    scmConnection.set("scm:git:https://github.com/${repoOwner.get()}/lsposed-library/blob/master/service")
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signing.keyId").orNull,
        providers.gradleProperty("signing.secretKey").orNull,
        providers.gradleProperty("signing.password").orNull
    )
}