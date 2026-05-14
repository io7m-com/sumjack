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

package com.io7m.sumjack.tests.bug19;

import com.io7m.sumjack.core.SjGeneratorConfiguration;
import com.io7m.sumjack.core.SjGenerators;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class Bug19Test
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Bug19Test.class);

  public static SjGeneratorConfiguration.Builder builder()
  {
    return SjGeneratorConfiguration.builder()
      .setId(URI.create("urn:example.com"))
      .setTitle("Example Schema")
      .setMapper(JsonMapper.shared());
  }

  @Test
  public void testSimple0Write(
    final @TempDir Path directory)
    throws Exception
  {
    final var config =
      builder()
        .setRootType(Bug19MessageType.class)
        .build();

    final var generator =
      SjGenerators.create(config);
    final var fileName =
      directory.resolve("file.json");
    final var r =
      generator.executeAndWrite(fileName);

    dump(r);
    assertTrue(Files.isRegularFile(fileName));
    assertEquals(1, Files.list(directory).toList().size());
  }

  private static void dump(
    final ObjectNode r)
  {
    final var mapper =
      JsonMapper.shared();
    final var pretty =
      mapper.writerWithDefaultPrettyPrinter();

    System.out.println(pretty.writeValueAsString(r));
  }
}
