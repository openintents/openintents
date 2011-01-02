This Ant build.xml requires slightly different properties from your regular
Android projects:

  sdk.dir:
    Base directory for your SDK, i.e. the directory that contains the
    'platforms' directory.

      e.g. sdk.dir=~/Android/sdk

  platform (instead of target):
    Because we're not using SetupTask that would also parse AndroidManifest.xml,
    we'll need to choose the platform explicitly.

      e.g. platform=android-1.6

  install.dir (optional):
    Target directory for the "install" Ant target.

      e.g. install.dir=/path/to/install/dir


  dm-common.dir:
    Source directory for DependencyManagerCommon sources; typically this will be

      e.g. dm-common.dir=../DependencyManagerCommon

  dm.dir:
    Source directory for DependencyManager sources; typically this will be

      e.g. dm-common.dir=../DependencyManager
