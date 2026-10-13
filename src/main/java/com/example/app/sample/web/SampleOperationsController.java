package com.example.app.sample.web;

import com.example.app.sample.Sample;
import com.example.app.sample.Sample.SampleId;
import com.example.app.sample.SampleCommand.AddSampleItem;
import com.example.app.sample.SampleCommand.CheckSampleItem;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleCommand.RemoveSampleItem;
import com.example.app.sample.SampleCommand.RenameSampleItem;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.SampleItem.SampleItemId;
import com.example.app.sample.Samples;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Extends the REST controller provided by Spring Data REST with aggregate specific operations.
 */
@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(Sample.class)
@SecurityRequirement(name = "basicAuth")
public class SampleOperationsController {

    private final Samples samples;

    @Secured("ROLE_USER")
    @PostMapping("/samples")
    public ResponseEntity<EntityModel<Sample>> create(@Valid @RequestBody CreateSample data) {
        val result = samples.save(Sample.create(data));
        return ResponseEntity.ok(EntityModel.of(result));
    }

    @Secured("ROLE_USER")
    @PostMapping(path = "/samples/{sampleId}/update")
    public ResponseEntity<EntityModel<Sample>> update(
            @PathVariable SampleId sampleId, @Valid @RequestBody UpdateSample data) {
        return doWithSample(sampleId, it -> it.update(data));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping(path = "/samples/{sampleId}/publish")
    public ResponseEntity<EntityModel<Sample>> publish(
            @PathVariable SampleId sampleId, @Valid @RequestBody PublishSample data) {
        return doWithSample(sampleId, it -> it.publish(data));
    }

    // sample enrichment: operations on the SampleItem child entity. An item operation answers with the whole
    // aggregate, since the links of the root and of every other item may change along with it.

    @Secured("ROLE_USER")
    @PostMapping(path = "/samples/{sampleId}/addItem")
    public ResponseEntity<EntityModel<Sample>> addItem(
            @PathVariable SampleId sampleId, @Valid @RequestBody AddSampleItem data) {
        return doWithSample(sampleId, it -> it.addItem(data));
    }

    @Secured("ROLE_USER")
    @PostMapping(path = "/samples/{sampleId}/items/{itemId}/renameItem")
    public ResponseEntity<EntityModel<Sample>> renameItem(
            @PathVariable SampleId sampleId, @PathVariable SampleItemId itemId,
            @Valid @RequestBody RenameSampleItem data) {
        return doWithSample(sampleId, it -> it.renameItem(itemId, data));
    }

    @Secured("ROLE_USER")
    @PostMapping(path = "/samples/{sampleId}/items/{itemId}/checkItem")
    public ResponseEntity<EntityModel<Sample>> checkItem(
            @PathVariable SampleId sampleId, @PathVariable SampleItemId itemId,
            @Valid @RequestBody CheckSampleItem data) {
        return doWithSample(sampleId, it -> it.checkItem(itemId, data));
    }

    @Secured("ROLE_USER")
    @PostMapping(path = "/samples/{sampleId}/items/{itemId}/removeItem")
    public ResponseEntity<EntityModel<Sample>> removeItem(
            @PathVariable SampleId sampleId, @PathVariable SampleItemId itemId,
            @Valid @RequestBody RemoveSampleItem data) {
        return doWithSample(sampleId, it -> it.removeItem(itemId, data));
    }

    private ResponseEntity<EntityModel<Sample>> doWithSample(SampleId sampleId, Consumer<Sample> action) {
        return samples.doWith(sampleId, action)
                .map(EntityModel::of)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
