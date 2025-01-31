package com.example.app.domain.person;

import com.example.app.domain.common.model.Command;
import lombok.Builder;

public sealed interface PersonCommand extends Command {

    @Builder
    record CreatePerson(
        String name
        // TODO add the fields required to create the aggregate
        ) implements PersonCommand { }

    @Builder
    record UpdatePersonName(
        String name) implements PersonCommand { }

}
