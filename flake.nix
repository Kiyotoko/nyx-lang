{
  description = "Top-level flake for Nyx and Nyx VM";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs";
    flake-utils.url = "github:numtide/flake-utils";

    nyx-ast.url = "path:./nyx-ast";
    nyx-vm.url = "path:./nyx-vm";
  };

  outputs = { self, nixpkgs, flake-utils, nyx-ast, nyx-vm }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in {
        packages = {
          nyxj = nyx-ast.packages.${system}.default;
          nyxc = nyx-vm.packages.${system}.default;
        };
      });
}
