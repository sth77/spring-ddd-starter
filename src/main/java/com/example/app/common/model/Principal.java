package com.example.app.common.model;

import lombok.Value;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
@Value(staticConstructor = "of")
public class Principal {

    String stringValue;
}
