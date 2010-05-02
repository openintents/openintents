This Ant build.xml requires additional properties from your regular Android
projects:

  dm-client.dir:
    Source directory for DependencyManagerClient sources; typically this will be

      e.g. dm-client.dir=../DependencyManagerClient

  platform (instead of target):
    Because DependencyManagerClient is not using SetupTask that would also parse
    AndroidManifest.xml, we'll need to choose the platform explicitly.

      e.g. platform=android-1.6

These properties ensure that the DependencyManagerClient.jar is built that
stores such as this one depend on.

Note that there is no particular reason to customize build.xml in this manner
beyond my own laziness: it is just as easily possible to drop a pre-built
DependencyManagerClient.jar into the libs/ folder. In fact, that is *exactly*
what I would recommend you do.

But for development, where sources of all inter-related projects are likely to
change often, I find this build.xml simpler.
