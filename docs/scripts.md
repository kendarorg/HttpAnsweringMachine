* version.txt: the current version
* build: Build scripts
    * build_docs: Regenerate the "puzzle" documentation
    * build_docker: Create local docker images for HAM
    * build_docker_samples: Create local docker images for Samples
    * build_release: Create the release tar.gz into "release" dir for HAM
    * build_release_samples: Create the release tar.gz into "release" dir for Samples
    * clean: Clean all projects
    * deploy_docker: Deploy and tag local HAM docker images
    * deploy_docker_samples: Deploy and tag local Samples docker images
    * deploy_jar: Deploy on Kendar maven repo
    * init: Utility to initialize all scripts
    * libs
      * copy_ham: Copy HAM data
      * copy_simpledns: Copy Simpledns data
* libs: Libraries used by the scripts
  * disk: Disk utilities
  * docker: Docker utilities
  * utils: Generic utilities
  * version: Version reader
  * win: Windows specific
    * sed: SED for Windows
  * win64: Windows 64bit specific
    * curl
    * jq-win64: Cmd Json parser
* run: Run everything
  * docker_multi_calendar: Run multiple docker image with Calendar sample
  * docker_single_calendar: Run single docker image with Calendar sample
  * docker_multi_quotes: Run multiple docker image with Quotes sample
  * local: Run HAM as localhost
  * local_calendar: Run calendar as localhost
  * init: Utility to initialize all scripts
* templates: Template files for release build