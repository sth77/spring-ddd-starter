package com.example.app.common.model;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;

@ValueObject
@Value(staticConstructor = "of")
public class Principal {

	String stringValue;

}
