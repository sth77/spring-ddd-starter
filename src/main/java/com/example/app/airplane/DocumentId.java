package com.example.app.airplane;

/**
 * Document ID, taken from some external system.
 */
public record DocumentId(String stringValue) {
    static DocumentId of(String stringValue) {
        return new DocumentId(stringValue);
    }
}
