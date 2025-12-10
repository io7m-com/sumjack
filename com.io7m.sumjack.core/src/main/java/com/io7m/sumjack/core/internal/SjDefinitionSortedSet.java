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

final class SjDefinitionSortedSet
  implements SjDefinitionType
{
  private final SjFullyResolvedType type;
  private final SjGeneratorConfiguration configuration;

  SjDefinitionSortedSet(
    final SjGeneratorConfiguration inConfiguration,
    final SjFullyResolvedType inType)
  {
    this.configuration = inConfiguration;
    this.type = inType;
  }

  @Override
  public ObjectNode execute()
  {
    final var param =
      this.type.type().getTypeBindings()
        .getBoundType(0);

    final var mapper = this.configuration.mapper();
    final var ref = mapper.createObjectNode();
    ref.put("$ref", SjFullyResolvedType.refName(param));

    final var object = mapper.createObjectNode();
    this.type.putDescription(object);
    object.put("type", "array");
    object.set("items", ref);
    return object;
  }
}
