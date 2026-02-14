{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs";
  };
  outputs = { self, nixpkgs, ... }:
    let
      pkgs = nixpkgs.outputs.legacyPackages.x86_64-linux;
      androidComposition = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "34" ];
        buildToolsVersions = [ "30.0.3" ];
        abiVersions = [ "x86" "x86_64"];
      };
      jdk = pkgs.jdk11;
    in with pkgs; {
      devShells.x86_64-linux.default = mkShell rec {
        # nativeBuildInputs is usually what you want -- tools you need to run
        nativeBuildInputs = [  
          jdk
          (callPackage gradle-packages.gradle_7 {
            java = jdk;
          })
          androidComposition.androidsdk
          androidComposition.platform-tools
        ];

        ANDROID_SDK_ROOT = "${androidComposition.androidsdk}/libexec/android-sdk";
        ANDROID_NDK_ROOT = "${ANDROID_SDK_ROOT}/ndk-bundle"; 
        GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidComposition.androidsdk}/libexec/android-sdk/build-tools/28.0.3/aapt2 -Dorg.gradle.jvmargs=-Xmx4096M";
      };
    };
}
