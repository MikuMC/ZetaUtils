plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

dependencies {
    paperweight.foliaDevBundle("1.20.4-R0.1-SNAPSHOT")
    implementation(project(":zutils-based-api"))
}
