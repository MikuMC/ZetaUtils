plugins {
    id("io.papermc.paperweight.userdev") version "1.7.5"
}

dependencies {
    paperweight.devBundle("me.earthme.luminol", "1.21.3-R0.1-20241201.004730-1")
    implementation(project(":zutils-based-api"))
}

configurations.reobf {
    outgoing.artifact(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
}