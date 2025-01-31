package com.example.app.domain.common.model;

import org.springframework.hateoas.LinkRelation;

public interface DomainOperation {

    String name();

    LinkRelation getRel();

}
