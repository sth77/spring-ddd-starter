package com.example.app.airplane;

public record InventoryCode(String stringValue) {
    static InventoryCode of(String stringValue) {
        return new InventoryCode(stringValue);
    }
}
