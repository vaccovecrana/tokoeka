pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
      url = uri("https://vacco-oss.s3.us-east-2.amazonaws.com")
    }
  }
}

include("tk-schema", "tk-sdr")