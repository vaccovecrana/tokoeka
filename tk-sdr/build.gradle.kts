configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
}

val api by configurations

dependencies {
  api(project(":tk-schema"))
  api("org.slf4j:slf4j-api:2.0.6")
  testImplementation("io.vacco.shax:shax:2.0.6.0.1.0")
  testImplementation("com.google.code.gson:gson:2.10.1")
}
