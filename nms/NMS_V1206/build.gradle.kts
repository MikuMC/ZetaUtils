plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

dependencies {
    paperweight.foliaDevBundle("1.20.6-R0.1-SNAPSHOT")
    implementation(project(":zutils-based-api"))
}

configurations.reobf {
    outgoing.artifact(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
}