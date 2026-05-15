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

package com.io7m.sumjack.tests;

import com.io7m.sumjack.jsonschema.SjJMappers;
import com.io7m.sumjack.jsonschema.SjJSchema;
import com.io7m.sumjack.jsonschema.SjJSchemaGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SjJSchemaTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SjJSchemaTest.class);

  @Test
  public void testSoftLeaf()
    throws Exception
  {
    final var mapper =
      SjJMappers.mapper();

    final var schemaText =
      resourceText("softleaf.json");
    final var schema =
      mapper.readValue(schemaText, SjJSchema.class);
    final var graph =
      SjJSchemaGraph.createGraph(schema);

    assertEquals(42, schema.defs().size());

    for (final var name : graph.namesInDependencyOrder()) {
      LOG.debug("Name: {}", name);
    }
  }

  private static String resourceText(
    final String name)
    throws IOException
  {
    final var url =
      SjGeneratorTest.class.getResource(
        "/com/io7m/sumjack/tests/%s".formatted(name)
      );

    if (url == null) {
      throw new IllegalStateException(
        "Missing resource: %s".formatted(name)
      );
    }

    try (var stream = url.openStream()) {
      return new String(stream.readAllBytes(), UTF_8).strip();
    }
  }

  private static String textOf(
    final ObjectNode r)
  {
    final var mapper =
      JsonMapper.shared();
    final var writer =
      mapper.writer();

    return writer.writeValueAsString(r).strip();
  }
}
