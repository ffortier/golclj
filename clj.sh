#!/usr/bin/env bash

export JAVA_OPTS="--sun-misc-unsafe-memory-access=allow"

deps=(
    "org.clojure:clojure:1.12.4"
)

cp="$(coursier fetch "${deps[@]}" -p)"

cmd=(java -cp ".:./resources:$cp" clojure.main)

# [[ $# -eq 0 ]] && cmd="$(rlwrap "${cmd[@]}")"

exec "${cmd[@]}" "$@"
