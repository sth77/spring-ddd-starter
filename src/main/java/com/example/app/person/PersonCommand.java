package com.example.app.person;

import com.example.app.common.model.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public sealed interface PersonCommand extends Command {

    @Builder
    record CreatePerson(@NotBlank @Size(max = 255) String name) implements PersonCommand {
    }

    @Builder
    record UpdatePersonName(@NotBlank @Size(max = 255) String name) implements PersonCommand {
    }
}
