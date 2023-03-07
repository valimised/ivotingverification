I-vote verification application
===============================

Android based vote verification application for Estonian I-voting system

The intention behind this repository is to make source code of the official
I-vote verification application for Estonian internet-voting system available
for public review.

The repository is not used for active development, but will be kept up to date,
so the code that can be found here is the code that is used for election. As the
voting system used for legally binding elections must strictly follow the
legislation, the actual development of Estonian I-voting system and I-vote
verification application is supervised by State Electoral Office of Estonia.
Please refer to www.valimised.ee for further information.

Reproducible building
---------------------

The source code published in this repository is enough to reproduce the APKs
distributed via Google Play Store.

The app is published here:
https://play.google.com/store/apps/details?id=ee.ivxv.ivotingverification&hl=en.
Current APK version is 35 (git tag RK2023-APK-35) used during RK2023 election.

Steps to building the project and verifying the codebase matches the published
APKs.

Build pre-requisites:
  * Java 8 JDK
  * Android SDK (Preferably the same build tool version as stated in
    app/build.gradle, easiest to get with SDK Manager)
  * App external dependencies saved to app/libs/
    * xom-1.2.10.jar (sha256: 35134150151dc4d3295c7a617fcce35b1b9537cca92179f48bf97655bae6782f)
      * http://central.maven.org/maven2/com/io7m/xom/xom/1.2.10/xom-1.2.10.jar
    * zxing_core-3.1.0.jar (sha256: f00b32f7a1b0edc914a8f74301e8dc34f189afc4698e9c8cc54e5d46772734a5)
      * http://central.maven.org/maven2/com/google/zxing/core/3.1.0/core-3.1.0.jar

Building:
  * Gradle buildsystem is used, actual version is specified in
    gradle/wrapper/gradle-wrapper.properties (currently 7.0.2)
  * Run gradlew (gradlew.bat on Windows) script in the root directory with
    'assembleRelease' argument (e.g., './gradlew assembleRelease' on Linux)
       * Android Gradle plugin requires Java 11 to run
  * Output apk will be located at
    app/build/outputs/apk/release/app-release-unsigned.apk

Comparing APKs:

Process of obtaining APKs from Google Play Store:
   * Copy the Google Play URL address of the app (https://play.google.com/store/apps/details?id=ee.ivxv.ivotingverification&hl=en)
   * Open the APK downloader (e.g., https://apk-dl.com/) and paste the copied URL address
   * Click "Download APK file" and then "Start download"

As APK is a valid ZIP file, the quickest method to compare two APKs is with some
ZIP comparison tool (e.g., 'zipcmp' or 'diff' on any Linux system). For more thorough
analysis diffoscope (https://diffoscope.org/) could be used.
   * For example, unzipping the APK files and using the 'diff -ru folder1/ folder2/ | sort' command would be helpful

The expected differences will be in the META_INF directory. The published APK
will contain two extra files (CERT.SF, CERT.RSA) and more detailed MANIFEST.MF
file due to being signed.

It is also possible that the AndroidManifest.xml files differ. This is due to
fact that multiple correct encodings of the manifest file exist. If this
happens, manifests of both APKs should be decoded and verified that the
originals match. This can be done for example with APK Analyzer
(https://developer.android.com/studio/build/apk-analyzer.html) in Android
Studio.

