{
  inputs = {
    flake-utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
  };

  outputs =
    {
      self,
      flake-utils,
      nixpkgs,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = (import nixpkgs) {
          inherit system;
          overlays = [];
        };
        maven = pkgs.maven;
      in
      {
        packages.default = maven.buildMavenPackage {
          pname = "nyx";
          version = "1.0-SNAPSHOT";
          src = ./.;
          mvnHash = "sha256-mXxLYE8BVv+E0KrsoEFHKGcpdjVJR2lNSEDgNN/pqfk=";

          nativeBuildInputs = [ pkgs.makeWrapper ];#

            installPhase = ''
              mkdir -p $out/bin $out/share/nyx
              install -Dm644 target/nyx-1.0-SNAPSHOT.jar $out/share/nyx

              makeWrapper ${pkgs.jre}/bin/java $out/bin/nyx \
                --add-flags "-jar $out/share/nyx/nyx-1.0-SNAPSHOT.jar"
            '';
        };

        devShells.default = pkgs.mkShell {
          nativeBuildInputs = with pkgs; [
            jdk17
            maven
          ];
        };
      }
    );
}
