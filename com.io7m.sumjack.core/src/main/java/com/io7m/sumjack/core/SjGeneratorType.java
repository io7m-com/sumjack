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

package com.io7m.sumjack.core;

import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * A schema generator.
 */

public interface SjGeneratorType
{
  /**
   * @return The generator configuration
   */

  SjGeneratorConfiguration configuration();

  /**
   * Execute the generator.
   *
   * @return The schema object
   *
   * @throws SjException On errors
   */

  ObjectNode execute()
    throws SjException;

  /**
   * Execute the generator and write the resulting schema to the given
   * file.
   *
   * @param file     The output file
   * @param fileTemp A temporary output file
   *
   * @return The schema object
   *
   * @throws SjException On errors
   * @throws IOException On errors
   */

  default ObjectNode executeAndWrite(
    final Path file,
    final Path fileTemp)
    throws SjException, IOException
  {
    final var object =
      this.execute();
    final var mapper =
      this.configuration().mapper();
    final var writer =
      mapper.writerWithDefaultPrettyPrinter();
    final var flags =
      new OpenOption[]{CREATE, WRITE, TRUNCATE_EXISTING};

    try (var stream = Files.newOutputStream(fileTemp, flags)) {
      writer.writeValue(stream, object);
      stream.flush();
    }

    Files.move(
      fileTemp,
      file,
      StandardCopyOption.REPLACE_EXISTING,
      StandardCopyOption.ATOMIC_MOVE
    );
    return object;
  }

  /**
   * Execute the generator and write the resulting schema to the given
   * file.
   *
   * @param file The output file
   *
   * @return The schema object
   *
   * @throws SjException On errors
   * @throws IOException On errors
   */

  default ObjectNode executeAndWrite(
    final Path file)
    throws SjException, IOException
  {
    return this.executeAndWrite(
      file,
      Paths.get(file + ".tmp")
    );
  }
}
