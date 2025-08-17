{
  description = "Top-level flake for Nyx and Nyx VM";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs";
    flake-utils.url = "github:numtide/flake-utils";

    nyx = { path = ./nyx; };
    nyx-vm = { path = ./nyx-vm; };
  };

  outputs = { self, nixpkgs, flake-utils, nyx, nyx-vm }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in {
        packages = {
          nyx = nyx.packages.${system}.default;
          nyx-vm = nyx-vm.packages.${system}.default;
        };
      });
}