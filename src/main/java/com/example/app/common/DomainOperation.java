package com.example.app.common;

import org.springframework.hateoas.LinkRelation;

public interface DomainOperation {

    String name();

    LinkRelation getRel();

}
