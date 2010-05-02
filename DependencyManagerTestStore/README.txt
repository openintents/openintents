This Ant build.xml requires additional properties from your regular Android
projects:

  dm-common.dir:
    Source directory for DependencyManagerCommon sources; typically this will be

      e.g. dm-common.dir=../DependencyManagerCommon

These properties ensure that the DependencyManagerCommon.jar is built that
stores such as this one depend on.

Note that there is no particular reason to customize build.xml in this manner
beyond my own laziness: it is just as easily possible to drop a pre-built
DependencyManagerCommon.jar into the libs/ folder. In fact, that is *exactly*
what I would recommend you do.

But for development, where sources of all inter-related projects are likely to
change often, I find this build.xml simpler.
