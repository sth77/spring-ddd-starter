package com.example.app.sample.web;

import com.example.app.sample.Sample;
import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.data.rest.core.config.Projection;

@InfrastructureRing
@Projection(name = "summary", types = {Sample.class})
public interface SampleSummary {

    String getName();

    String getOwnerName();

}
