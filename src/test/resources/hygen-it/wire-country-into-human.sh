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
TEST_DATA=src/test/java/com/example/app/world/WorldTestData.java
AGGREGATE_TEST=src/test/java/com/example/app/world/HumanTest.java

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

# The generated tests are part of what the templates produce, so they have to keep passing once the command
# requires a country - a developer adding a mandatory field fixes the test data in the same breath. Without
# this, every test built on WorldTestData.human() dies on a null country.

patch "$TEST_DATA" \
    'import com.example.app.world.HumanCommand.CreateHuman;' \
    'import com.example.app.common.model.I18nText;
import com.example.app.referencedata.Country;
import com.example.app.world.HumanCommand.CreateHuman;'

patch "$TEST_DATA" \
    '        val result = Human.create(CreateHuman.builder()
                .name(name)
                .build());' \
    '        val result = Human.create(CreateHuman.builder()
                .name(name)
                .country(Country.of(I18nText.en("Country X")))
                .build());'

patch "$AGGREGATE_TEST" \
    'import com.example.app.common.model.DomainException;' \
    'import com.example.app.common.model.DomainException;
import com.example.app.common.model.I18nText;
import com.example.app.referencedata.Country;'

patch "$AGGREGATE_TEST" \
    '        val human = Human.create(CreateHuman.builder()
                .name(name)
                .build());' \
    '        val human = Human.create(CreateHuman.builder()
                .name(name)
                .country(Country.of(I18nText.en("Country 1")))
                .build());'

# Assert the wiring itself, so the reference-data round trip is covered and not merely compiled. The name
# assertion is part of the anchor because the update test repeats the state assertion verbatim.
patch "$AGGREGATE_TEST" \
    '        assertThat(human.getName()).isEqualTo(name);
        assertThat(human.getState()).isEqualTo(DRAFT);' \
    '        assertThat(human.getName()).isEqualTo(name);
        assertThat(human.getState()).isEqualTo(DRAFT);
        assertThat(human.getCitizenship()).isEqualTo(I18nText.en("Country 1"));'
