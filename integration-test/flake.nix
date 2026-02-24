{
  description = "Mill build tool FHS dev shell";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = nixpkgs.legacyPackages.${system};

      fhs = pkgs.buildFHSEnv {
        name = "mill-fhs";
        targetPkgs = pkgs: [
          pkgs.jdk17
          pkgs.curl
          pkgs.coreutils
          pkgs.bash
          pkgs.gnused
          pkgs.gnugrep
          pkgs.findutils
          pkgs.gawk
          pkgs.zlib
        ];
        profile = ''
          export JAVA_HOME="${pkgs.jdk17}"
        '';
      };
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = [ fhs ];
        shellHook = ''
          exec mill-fhs
        '';
      };
    };
}
