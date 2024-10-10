plugins { id("io.vacco.oss.gitflow") version "1.0.1" apply(false) }

subprojects {
  apply(plugin = "io.vacco.oss.gitflow")

  group = "io.vacco.tokoeka"
  version = "0.5.1"

  configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
    sharedLibrary(true, false)
  }
}
