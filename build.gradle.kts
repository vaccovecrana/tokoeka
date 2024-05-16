plugins { id("io.vacco.oss.gitflow") version "0.9.8" }

group = "io.vacco.tokoeka"
version = "0.1.0"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  sharedLibrary(true, false)
}

val api by configurations

dependencies {
  api("org.java-websocket:Java-WebSocket:1.5.6")
  testImplementation("io.vacco.shax:shax:2.0.6.0.1.0")
  testImplementation("com.google.code.gson:gson:2.10.1")
}
