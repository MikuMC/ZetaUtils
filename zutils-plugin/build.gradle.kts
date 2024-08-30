group = rootProject.group
version = rootProject.version

dependencies {
    compileOnly("dev.folia:folia-api:1.20.4-R0.1-SNAPSHOT")
    implementation(project(":zutils-based-api"))
    /*implementation(project(":nms:NMS_V1201","reobf"))
    implementation(project(":nms:NMS_V1202","reobf"))
    implementation(project(":nms:NMS_V1204","reobf"))*/
    implementation(project(":nms:NMS_V1206","reobf"))
    implementation(project(":nms:NMS_V1211","reobf"))
}