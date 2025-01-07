package com.example.app.infrastructure.web.sampleapi;

import com.example.app.domain.sample.Sample;
import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@InfrastructureRing
@Projection(name = "summary", types = {Sample.class})
public interface SampleSummary {

    String getName();

    @Value("#{@people.resolveRequired(target.owner).getName()}")
    String getOwner();

}
