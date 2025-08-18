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
        zig = pkgs.zig;
      in
      {
        packages.default = pkgs.stdenv.mkDerivation {
          pname = "nyx-vm";
          version = "1.0-SNAPSHOT";
          src = ./.;
          nativeBuildInputs = [
            zig.hook
            zig
          ];
        };

        checks."valgrind" = pkgs.runCommand "valgrind" {
          nativeBuildInputs = [ pkgs.valgrind ];
          buildInputs = [ self.packages.${system}.default ];
        } ''
          mkdir -p $out
          valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes --log-file=valgrind-out.txt ${self.packages.${system}.default}/bin/nyx-vm
        '';

        devShells.default = pkgs.mkShell {
          nativeBuildInputs = with pkgs; [
            zig
            zls
            clang-tools
            clang
          ];
        };
      }
    );
}
