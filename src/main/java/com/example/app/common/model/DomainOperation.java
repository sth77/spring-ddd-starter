package com.example.app.common.model;

import org.springframework.hateoas.LinkRelation;

public interface DomainOperation {

    String name();

    LinkRelation getRel();

}
