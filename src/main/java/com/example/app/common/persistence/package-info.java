/**
 * Shared persistence helpers usable from feature packages. Deliberately carries no onion-ring annotation:
 * like the {@code jakarta.persistence} API it wraps, it is used by converters that live next to the domain
 * enums they map, and must therefore not count as an infrastructure-ring dependency of the domain ring.
 */
@NullMarked
@org.springframework.modulith.NamedInterface("persistence")
package com.example.app.common.persistence;

import org.jspecify.annotations.NullMarked;
