---
to: src/main/java/com/example/app/<%= feature %>/web/<%= Name %>CommandController.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>;
import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= FeaturePackage %>.<%= RepositoryType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= UpdateCommandType %>;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.function.Consumer;

/**
 * Extends the REST controller provided by Spring Data REST with aggregate specific operations.
 */
@Transactional
@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(<%= AggregateType %>.class)
@SecurityRequirement(name = "basicAuth")
public class <%= ControllerType %> {

	private final <%= RepositoryType %> <%= repositoryName %>;

    @Secured("ROLE_USER")
    @PostMapping("/<%= collectionRel %>")
    public ResponseEntity<EntityModel<<%= AggregateType %>>> create(@RequestBody <%= CreateCommandType %> data) {
        val result = <%= repositoryName %>.save(<%= AggregateType %>.create(data));
        return ResponseEntity.ok(EntityModel.of(result));
    }

    @Secured("ROLE_USER")
	@PostMapping(path = "/<%= collectionRel %>/{<%= idName %>}/update")
	public ResponseEntity<EntityModel<<%= AggregateType %>>> update(@PathVariable <%= IdType %> <%= idName %>, @RequestBody <%= UpdateCommandType %> data) {
		return doWith<%= AggregateType %>(<%= idName %>, it -> it.update(data));
	}

    @Secured("ROLE_ADMIN")
	@PostMapping(path = "/<%= collectionRel %>/{<%= idName %>}/publish")
	public ResponseEntity<EntityModel<<%= AggregateType %>>> publish(@PathVariable <%= IdType %> <%= idName %>) {
		return doWith<%= AggregateType %>(<%= idName %>, <%= AggregateType %>::publish);
	}

	private ResponseEntity<EntityModel<<%= AggregateType %>>> doWith<%= AggregateType %>(<%= IdType %> <%= idName %>, Consumer<<%= AggregateType %>> action) {
		return <%= repositoryName %>.doWith(<%= idName %>, action)
				.map(EntityModel::of)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

}
