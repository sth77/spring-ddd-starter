package com.example.app.airplane;

import java.util.Collection;

public interface Airplanes {
    Collection<Airplane> findAll();

    Airplane findById(final Airplane.Id id);

    void save(final Airplane airplane);
}
