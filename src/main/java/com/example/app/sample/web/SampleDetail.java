package com.example.app.sample.web;

import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.sample.Sample;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "detail", types = {Sample.class})
public interface SampleDetail {

    I18nText getName();

    String getDescription();

    @Value("#{@people.resolveRequired(target.owner)}")
    Person getOwner();
}
