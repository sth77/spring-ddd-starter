package com.example.app.airplane;

import org.jmolecules.ddd.types.Repository;

import java.util.Collection;

public interface Airplanes extends Repository<Airplane, Airplane.Id> {
    Collection<Airplane> findAll();

    Airplane findById(final Airplane.Id id);

    void save(final Airplane airplane);
}
