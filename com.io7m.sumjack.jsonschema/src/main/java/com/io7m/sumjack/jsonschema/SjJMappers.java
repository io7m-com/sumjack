/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.sumjack.jsonschema;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

public final class SjJMappers
{
  private static final JsonMapper MAPPER =
    createJsonMapper();

  private SjJMappers()
  {

  }

  public static JsonMapper mapper()
  {
    return MAPPER;
  }

  private static JsonMapper createJsonMapper()
  {
    final var builder = JsonMapper.builder();

    final var module = new SimpleModule();
    module.addDeserializer(
      SjJDefType.class,
      new SjJDefTypeDeserializer()
    );
    module.addDeserializer(
      SjJAdditionalPropertiesType.class,
      new SjJAdditionalPropertiesDeserializer()
    );
    builder.addModule(module);
    return builder.build();
  }
}
