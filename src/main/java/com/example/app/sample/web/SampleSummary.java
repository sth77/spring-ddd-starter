package com.example.app.sample.web;

import com.example.app.sample.Sample;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "summary", types = {Sample.class})
public interface SampleSummary {

    String getName();

    String getOwnerName();

}
