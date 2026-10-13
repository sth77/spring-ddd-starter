package com.example.app.person;

import static com.example.app.AggregateEvents.clearEvents;

import com.example.app.person.PersonCommand.CreatePerson;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class PersonTestData {

    public static Person person() {
        return person("Person X");
    }

    public static Person person(String name) {
        val result = Person.create(CreatePerson.builder()
                .name(name)
                .build());
        clearEvents(result);
        return result;
    }
}
