---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/web/<%= Name + "Controller" %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>;
import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= FeaturePackage %>.<%= RepositoryType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= UpdateNameCommandType %>;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;

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
@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(<%= AggregateType %>.class)
@SecurityRequirement(name = "basicAuth")
public class <%= ControllerType %> implements RepresentationModelProcessor<CollectionModel<EntityModel<<%= AggregateType %>>>> {

	private final <%= RepositoryType %> <%= repositoryName %>;
	private final EntityLinks entityLinks;

    @PostMapping
    public ResponseEntity<EntityModel<<%= AggregateType %>>> create(@RequestBody <%= CreateCommandType %> data) {
        val result = <%= repositoryName %>.save(<%= AggregateType %>.create(data));
        return ResponseEntity.ok(EntityModel.of(result));
    }

	@PostMapping(path = "/{<%= idName %>}/updateName")
	public ResponseEntity<EntityModel<<%= AggregateType %>>> updateName(@PathVariable <%= IdType %> <%= idName %>, @RequestBody <%= UpdateNameCommandType %> data) {
		return doWith<%= AggregateType %>(<%= idName %>, it -> it.updateName(data));
	}

	private ResponseEntity<EntityModel<<%= AggregateType %>>> doWith<%= AggregateType %>(<%= IdType %> <%= idName %>, Consumer<<%= AggregateType %>> action) {
		return <%= repositoryName %>.doWith(<%= idName %>, action)
				.map(EntityModel::of)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

    @Nonnull
    @Override
    public CollectionModel<EntityModel<<%= AggregateType %>>> process(CollectionModel<EntityModel<<%= AggregateType %>>> model) {
        return model.add(entityLinks.linkFor(<%= AggregateType %>.class).withRel(<%= AggregateType %>.Operation.CREATE.rel));
    }

}
