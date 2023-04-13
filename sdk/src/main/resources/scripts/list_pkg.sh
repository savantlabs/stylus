#!/usr/bin/env bash

PKGS=${@:1}
echo "List pkg "$PKGS
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

echo "activating conda env ..."
cd ../canvas
$CONDA_COMMAND activate ./venv
echo "Listing conda packages: ${PKGS}"
$CONDA_COMMAND list ${PKGS}