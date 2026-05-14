/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.sumjack.core.internal;

import com.io7m.sumjack.core.SjDefinitionType;
import com.io7m.sumjack.core.SjGeneratorConfiguration;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

final class SjDefinitionRecord
  implements SjDefinitionType
{
  private final SjFullyResolvedType type;
  private final SjGeneratorConfiguration configuration;

  SjDefinitionRecord(
    final SjGeneratorConfiguration inConfiguration,
    final SjFullyResolvedType inType)
  {
    this.configuration = inConfiguration;
    this.type = inType;
  }

  @Override
  public ObjectNode execute()
  {
    final var mapper =
      this.configuration.mapper();

    final var props =
      mapper.createObjectNode();
    final var required =
      mapper.createArrayNode();

    for (final var entry : this.type.methods().entrySet()) {
      final var name =
        entry.getKey();
      final var description =
        Optional.ofNullable(this.type.methodDescriptions().get(name));

      final var ref = mapper.createObjectNode();
      description.ifPresent(text -> {
        ref.put("description", text);
      });
      ref.put(
        "$ref",
        SjFullyResolvedType.refName(entry.getValue().getReturnType())
      );
      props.set(name, ref);
    }

    this.type.typeProperty().ifPresent(attrib -> {
      final var pattern = "^%s$".formatted(attrib.propertyValue());
      final var typeProp = mapper.createObjectNode();
      typeProp.put("type", "string");
      typeProp.put("pattern", pattern);
      props.set(attrib.propertyName(), typeProp);
      required.add(attrib.propertyName());
    });

    for (final var name : this.type.methodRequired()) {
      required.add(name);
    }

    final var object = mapper.createObjectNode();
    this.type.putDescription(object);
    object.put("type", "object");
    object.set("properties", props);
    object.set("required", required);
    object.put("additionalProperties", false);
    return object;
  }
}
