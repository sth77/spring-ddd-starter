package com.example.app.domain.sample.web;

import com.example.app.domain.person.Person;
import com.example.app.domain.sample.Sample;
import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@InfrastructureRing
@Projection(name = "detail", types = {Sample.class})
public interface SampleDetail {

    String getName();

    String getDescription();

    @Value("#{@people.resolveRequired(target.owner)}")
    Person getOwner();

}
