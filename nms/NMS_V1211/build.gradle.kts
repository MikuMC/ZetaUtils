plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

dependencies {
    paperweight.devBundle("me.earthme.luminol", "1.21.1-R0.1-20240830.020224-7")
    implementation(project(":zutils-based-api"))
}

configurations.reobf {
    outgoing.artifact(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
}