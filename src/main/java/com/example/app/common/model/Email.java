package com.example.app.common.model;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

@Value(staticConstructor = "of")
@Accessors(fluent = true)
public class Email {

    @NonNull
    String stringValue;

}
