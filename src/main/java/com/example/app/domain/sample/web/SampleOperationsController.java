package com.example.app.domain.sample.web;

import com.example.app.domain.common.model.AggregateCommands;
import com.example.app.domain.sample.Sample;
import com.example.app.domain.sample.Sample.SampleId;
import com.example.app.domain.sample.SampleCommand;
import com.example.app.domain.sample.SampleCommand.PublishSample;
import com.example.app.domain.sample.Samples;
import com.example.app.domain.sample.SampleCommand.CreateSample;
import com.example.app.domain.sample.SampleCommand.UpdateSample;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.jmolecules.architecture.onion.simplified.InfrastructureRing;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.function.Consumer;

/**
 * Extends the REST controller provided by Spring Data REST with aggregate specific operations.
 */
@InfrastructureRing
@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(Sample.class)
@SecurityRequirement(name = "basicAuth")
public class SampleOperationsController {

	private final Samples samples;

	@PostMapping("/samples")
    public ResponseEntity<EntityModel<Sample>> create(@RequestBody CreateSample data) {
        val result = samples.save(Sample.create(data));
        return ResponseEntity.ok(EntityModel.of(result));
    }

	@PostMapping(path = "/samples/{sampleId}/update")
	public ResponseEntity<EntityModel<Sample>> update(@PathVariable SampleId sampleId, @RequestBody UpdateSample data) {
		return doWithSample(sampleId, it -> it.update(data));
	}

	@PostMapping(path = "/samples/{sampleId}/publish")
	public ResponseEntity<EntityModel<Sample>> publish(@PathVariable SampleId sampleId, @RequestBody PublishSample data) {
		return doWithSample(sampleId, it -> it.publish(data));
	}

	private ResponseEntity<EntityModel<Sample>> doWithSample(SampleId sampleId, Consumer<Sample> action) {
		return samples.doWith(sampleId, action)
				.map(EntityModel::of)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

}
