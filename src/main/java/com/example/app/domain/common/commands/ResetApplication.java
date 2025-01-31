package com.example.app.domain.common.commands;

import com.example.app.domain.common.model.Command;
import lombok.Value;

@Value(staticConstructor = "create")
public class ResetApplication implements Command {

}
