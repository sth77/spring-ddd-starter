package com.example.app.referencedata;

import com.example.app.common.model.ReferenceDataRepository;
import com.example.app.referencedata.City.CityId;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "cities", path = "cities")
public interface Cities extends ReferenceDataRepository<City, CityId> {

    @Override
    default Class<City> getItemType() {
        return City.class;
    }

    List<City> findByNameEnStartingWith(String name);

    Optional<City> findByPostalCode(int postalCode);
}
