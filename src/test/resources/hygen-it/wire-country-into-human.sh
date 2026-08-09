#!/usr/bin/env bash
#
# Wires the scaffolded Country reference data into the scaffolded Human aggregate, the way a developer does
# after generating both: the command references the entity by URI, and the aggregate embeds a copy of the
# fields it cares about (see "Master data handling" in the README).
#
# Every edit fails if its anchor is gone, so a template change breaks the build instead of quietly leaving
# the integration test without a reference to master data.

set -euo pipefail

HUMAN=src/main/java/com/example/app/world/Human.java
COMMAND=src/main/java/com/example/app/world/HumanCommand.java
MIGRATION=$(ls src/main/resources/db/migration/*__create_human.sql)

patch() {
    local file=$1
    export ANCHOR=$2 REPLACEMENT=$3
    perl -0777 -i -pe 'BEGIN { $n = 0 }
                       $n += s/\Q$ENV{ANCHOR}\E/$ENV{REPLACEMENT}/;
                       END { exit 9 unless $n }' "$file" ||
        { printf 'ERROR: anchor not found in %s:\n%s\n' "$file" "$ANCHOR" >&2; exit 1; }
}

patch "$HUMAN" \
    'import com.example.app.common.model.DomainException;' \
    'import com.example.app.common.model.DomainException;
import com.example.app.common.model.I18nText;'

patch "$HUMAN" \
    '    private HumanState state;' \
    '    private HumanState state;
    private final I18nText citizenship;'

patch "$HUMAN" \
    '            HumanState.DRAFT);' \
    '            HumanState.DRAFT,
            data.country().getName());'

patch "$COMMAND" \
    'import com.example.app.common.model.Command;' \
    'import com.example.app.common.model.Command;
import com.example.app.referencedata.Country;'

patch "$COMMAND" \
    'import jakarta.validation.constraints.NotBlank;' \
    'import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;'

patch "$COMMAND" \
    '        @NotBlank String name
        // TODO add the fields required to create the aggregate' \
    '        @NotBlank String name,
        @NotNull Country country'

patch "$MIGRATION" \
    '    state VARCHAR(20) NOT NULL
    );' \
    '    state VARCHAR(20) NOT NULL,
    citizenship_en VARCHAR(255) NOT NULL,
    citizenship_de VARCHAR(255) NOT NULL
    );'
