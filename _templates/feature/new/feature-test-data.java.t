---
to: src/test/java/com/example/app/<%= name %>/<%= Name %>TestData.java
---
package com.example.app.<%= name %>;

import lombok.experimental.UtilityClass;
import lombok.val;

import static com.example.app.AggregateEvents.clearEvents;

@UtilityClass
public class <%= Name %>TestData {

}
