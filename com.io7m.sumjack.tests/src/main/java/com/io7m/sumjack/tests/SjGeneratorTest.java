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

package com.io7m.sumjack.tests;

import com.io7m.seltzer.slf4j.SSLogging;
import com.io7m.sumjack.core.SjException;
import com.io7m.sumjack.core.SjGeneratorConfiguration;
import com.io7m.sumjack.core.SjGenerators;
import com.io7m.sumjack.core.standard.SjPrimitives;
import com.io7m.sumjack.core.standard.SjURI;
import com.io7m.sumjack.core.standard.SjUUID;
import com.io7m.sumjack.lanark.SjDottedName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.io7m.sumjack.core.standard.SjBase64ByteArray.BASE64_BYTE_ARRAY;
import static com.io7m.sumjack.core.standard.SjBigDecimal.BIG_DECIMAL;
import static com.io7m.sumjack.core.standard.SjBigInteger.BIG_INTEGER;
import static com.io7m.sumjack.core.standard.SjOffsetDateTime.OFFSET_DATE_TIME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SjGeneratorTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SjGeneratorTest.class);

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
        .setRootType(SimpleBase0Type.class)
        .build();

    final var generator =
      SjGenerators.create(config);
    final var fileName =
      directory.resolve("file.json");
    final var r =
      generator.executeAndWrite(fileName);

    assertTrue(Files.isRegularFile(fileName));
    assertEquals(1, Files.list(directory).toList().size());
  }

  @Test
  public void testSimple0()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleBase0Type.class)
        .build();

    runCheck(config, "Simple0.json");
  }

  @Test
  public void testSimple1()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleBaseA.class)
        .build();

    runCheck(config, "Simple1.json");
  }

  @Test
  public void testTrafficLight()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(TrafficLight.class)
        .build();

    runCheck(config, "TrafficLight.json");
  }

  @Test
  public void testSimpleContainsList()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsList.class)
        .build();

    runCheck(config, "SimpleContainsList.json");
  }

  @Test
  public void testSimpleContainsSet()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsSet.class)
        .build();

    runCheck(config, "SimpleContainsSet.json");
  }

  @Test
  public void testSimpleContainsMap()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsMap.class)
        .build();

    runCheck(config, "SimpleContainsMap.json");
  }

  @Test
  public void testPrimitives()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(int.class)
        .addDefinitions(SjPrimitives.values())
        .build();

    runCheck(config, "primitives.json");
  }

  @Test
  public void testSimpleContainsOptional()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsOptional.class)
        .build();

    runCheck(config, "SimpleContainsOptional.json");
  }

  @Test
  public void testRDottedName()
    throws Exception
  {
    final var config =
      builder()
        .addDefinitions(SjDottedName.DOTTED_NAME)
        .setRootType(SimpleContainsDottedName.class)
        .build();

    runCheck(config, "SimpleContainsDottedName.json");
  }

  @Test
  public void testOffsetDateTime()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsODT.class)
        .addDefinitions(OFFSET_DATE_TIME)
        .build();

    runCheck(config, "OffsetDateTime.json");
  }

  @Test
  public void testUUID()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsUUID.class)
        .addDefinitions(SjUUID.UUID)
        .build();

    runCheck(config, "UUID.json");
  }

  @Test
  public void testURI()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsURI.class)
        .addDefinitions(SjURI.URI)
        .build();

    runCheck(config, "URI.json");
  }

  @Test
  public void testBase64ByteArray()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsByteArray.class)
        .addDefinitions(BASE64_BYTE_ARRAY)
        .build();

    runCheck(config, "Base64ByteArray.json");
  }

  @Test
  public void testBigDecimal()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsBigDecimal.class)
        .addDefinitions(BIG_DECIMAL)
        .build();

    runCheck(config, "BigDecimal.json");
  }

  @Test
  public void testBigInteger()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(SimpleContainsBigInteger.class)
        .addDefinitions(BIG_INTEGER)
        .build();

    runCheck(config, "BigInteger.json");
  }

  @Test
  public void testObject()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(Object.class)
        .build();

    runCheck(config, "Object.json");
  }

  @Test
  public void testVector3()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(Vector3.class)
        .addDefinitions(SjPrimitives.DOUBLE)
        .build();

    runCheck(config, "Vector3.json");
  }

  @Test
  public void testGeneric()
    throws Exception
  {
    final var config =
      builder()
        .setRootType(Generic.class)
        .build();

    runCheck(config, "Generic.json");
  }

  private static void runCheck(
    final SjGeneratorConfiguration config,
    final String name)
    throws SjException, IOException
  {
    final var generator =
      SjGenerators.create(config);
    final var r =
      generator.execute();

    dump(r);
    assertEquals(resourceText(name), textOf(r));
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

  private static void dump(
    final ObjectNode r)
  {
    final var mapper =
      JsonMapper.shared();
    final var writer =
      mapper.writer();

    System.out.println(writer.writeValueAsString(r));
  }
}
