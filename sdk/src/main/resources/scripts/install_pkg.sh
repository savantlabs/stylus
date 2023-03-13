#!/usr/bin/env bash

PKGS=${@:1}

if [[ ${IS_GITHUB_RUNNER} = "true" ]]; then
  echo "using micromamba ..."
  export MAMBA_EXE="/home/runner/micromamba-bin/micromamba";
  export MAMBA_ROOT_PREFIX="/home/runner/micromamba-root";
  __mamba_setup="$("$MAMBA_EXE" shell hook --shell zsh --prefix "$MAMBA_ROOT_PREFIX" 2> /dev/null)"
  eval "$__mamba_setup"
else
  if [ -f "$HOME/.bash_profile" ]; then
      echo "Sourcing bash_profile"
      source $HOME/.bash_profile
  elif [ -f "$HOME/.bashrc" ]; then
      echo "Sourcing bashrc"
      source $HOME/.bashrc
  else
      echo "No bash_profile or bashrc"
  fi

  if [ -x "$(command -v micromamba)" ]; then
      echo "using micromamba ..."
      CONDA_COMMAND=micromamba
      eval "$(micromamba shell hook --shell=bash)"
  else
      echo "using conda ..."
      CONDA_COMMAND=conda
      conda init
      if [ -f "$HOME/.bash_profile" ]; then
          echo "Sourcing bash_profile"
          source $HOME/.bash_profile
      elif [ -f "$HOME/.bashrc" ]; then
          echo "Sourcing bashrc"
          source $HOME/.bashrc
      else
          echo "No bash_profile or bashrc"
      fi
  fi
fi

echo "activating conda env ..."
cd ../canvas
$CONDA_COMMAND activate ./venv
echo "Install conda packages: ${PKGS}"
$CONDA_COMMAND install ${PKGS}
