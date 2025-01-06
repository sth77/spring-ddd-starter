package com.example.app.common.model;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jmolecules.ddd.annotation.ValueObject;

@Value(staticConstructor = "of")
@Accessors(fluent = true)
@ValueObject
public class Email {

    @NonNull
    String stringValue;

}
