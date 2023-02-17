#!/usr/bin/env bash

{
  ./gradlew spotlessCheck;
} || {
  ./gradlew spotlessApply;
  exit 1;
}
