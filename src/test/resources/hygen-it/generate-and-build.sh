#!/usr/bin/env bash
#
# Generates a throwaway feature from the hygen templates and builds it: usage
#   generate-and-build.sh <build output directory> <test output directory>
#
# world/Human and Country are deliberately domain-neutral throwaways: they are generated into src/ and reverted
# again, so their names must not collide with a feature, aggregate or reference data entity of the project built on
# this starter - otherwise the git cleanup would delete hand-written code, and the generated Flyway migration would
# clash with an existing table. Reference data is only worth generating if an aggregate then refers to it, hence the
# wiring step.
#
# The operation generator injects into the files the aggregate and controller generators produced; an injection
# whose anchor has moved inserts nothing or lands at the top of the file, which the build then fails on.
#
# Generation and the build over it are one step so that the tree is restored even when they fail - which is the
# interesting case, since a template emitting code that does not compile is exactly what this profile exists to
# catch. The profile's cleanups sit in post-integration-test, which Maven never reaches once a mojo has failed, and
# the leftovers would then trip the uncommitted-changes guard on the next run. Restoring src/ is only half of it -
# the output directories have to go the same way as in the profile's clean execution, or the next build still finds
# two create_human migrations. The subshell carries the errexit: an ERR trap does not fire on a failing nested
# `mvn`, and `if ! generate...` would suppress errexit inside the sequence, so a broken early step would run on into
# the build instead of stopping.
#
# The nested build runs `mvn test`, not just `compile`: the outer build compiled and tested before hygen had
# generated anything, so this is the only place where the generated aggregate test, repository test and feature
# test data are compiled and run at all, and the only place where ArchUnit and Modulith see the generated main code.
# byte-buddy's transform-extended defaults to process-classes, so `test` picks it up without naming the goal. The
# nested build does not inherit -Phygen-it and so cannot recurse into itself.

set -uo pipefail

output_directory=$1
test_output_directory=$2

(
    set -e
    hygen feature new world
    hygen aggregate new Human --feature=world
    hygen controller new Human --feature=world
    hygen operation new Archive --aggregate=Human --feature=world
    hygen referencedata new Country
    bash src/test/resources/hygen-it/wire-country-into-human.sh
    mvn test -o
)
status=$?
if [ $status -ne 0 ]; then
    git checkout -- src/
    git clean -fdq src/
    rm -rf "$output_directory" "$test_output_directory"
fi
exit $status
