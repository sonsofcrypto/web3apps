buildscript {
    val kotlinVersion = "1.7.10"
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
        classpath("com.android.tools.build:gradle:7.3.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.13.3")
    }
}

allprojects {
    ext["settings_version"] = "1.0.0-alpha01"
    ext["serialization_version"] = "1.3.3"
    ext["coroutines_version"] = "1.6.3-native-mt"
    ext["ktor_version"] = "2.0.3"
    ext["bignum_version"] = "0.3.6"
    ext["datetime_version"] = "0.4.0"

    repositories {
        google()
        mavenCentral()
        uri("https://oss.sonatype.org/content/repositories/snapshots")

    }

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
