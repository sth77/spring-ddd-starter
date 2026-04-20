package com.example.app.sample.web;

import com.example.app.sample.Sample;
import com.example.app.sample.Sample.SampleId;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.Samples;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.function.Consumer;

@Transactional
@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(Sample.class)
@SecurityRequirement(name = "basicAuth")
public class SampleOperationsController {

    private final Samples samples;

    @Secured("ROLE_USER")
    @PostMapping("/samples")
    public ResponseEntity<EntityModel<Sample>> create(@Valid @RequestBody CreateSample data) {
        final var result = samples.save(Sample.create(data));
        return ResponseEntity.ok(EntityModel.of(result));
    }

    @Secured("ROLE_USER")
    @PutMapping(path = "/samples/{sampleId}")
    public ResponseEntity<EntityModel<Sample>> update(
            @PathVariable SampleId sampleId,
            @Valid @RequestBody UpdateSample data) {
        return doWithSample(sampleId, it -> it.update(data));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping(path = "/samples/{sampleId}/publish")
    public ResponseEntity<EntityModel<Sample>> publish(@PathVariable SampleId sampleId) {
        return doWithSample(sampleId, it -> it.publish(new com.example.app.sample.SampleCommand.PublishSample()));
    }

    private ResponseEntity<EntityModel<Sample>> doWithSample(
            SampleId sampleId,
            Consumer<Sample> action) {
        return samples.doWith(sampleId, action)
                .map(EntityModel::of)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
