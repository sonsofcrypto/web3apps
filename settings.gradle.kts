pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "web3wallet"
include(":androidApp")
include(":web3lib")
include(":web3walletcore")
include(":coreCrypto")
