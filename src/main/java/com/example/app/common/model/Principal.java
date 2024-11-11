package com.example.app.common.model;

import org.jmolecules.ddd.annotation.ValueObject;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Value;

@ValueObject
@Value(staticConstructor = "of")
public class Principal {
	
	@JsonValue
	String stringValue;
	
}
