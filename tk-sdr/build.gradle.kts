configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
}

val api by configurations

dependencies {
  api("org.java-websocket:Java-WebSocket:1.5.6")
  api(project(":tk-schema"))
  testImplementation("io.vacco.shax:shax:2.0.6.0.1.0")
  testImplementation("com.google.code.gson:gson:2.10.1")
}
