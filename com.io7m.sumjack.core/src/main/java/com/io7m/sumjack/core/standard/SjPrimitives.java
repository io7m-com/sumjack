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

package com.io7m.sumjack.core.standard;

import com.io7m.sumjack.core.SjDefinitionProviderType;
import com.io7m.sumjack.core.SjDefinitionType;
import com.io7m.sumjack.core.SjGeneratorConfiguration;

/**
 * Definitions for primitives.
 */

public enum SjPrimitives implements SjDefinitionProviderType
{
  /**
   * java.lang.boolean
   */

  BOOLEAN {
    @Override
    public String typeName()
    {
      return boolean.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "boolean");
        object.put("description", "A boolean value.");
        return object;
      };
    }
  },

  /**
   * java.lang.byte
   */

  BYTE {
    @Override
    public String typeName()
    {
      return byte.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "number");
        object.put("description", "A primitive byte.");
        object.put("minimum", Byte.MIN_VALUE);
        object.put("maximum", Byte.MAX_VALUE);
        return object;
      };
    }
  },

  /**
   * java.lang.short
   */

  SHORT {
    @Override
    public String typeName()
    {
      return short.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "number");
        object.put("description", "A primitive short.");
        object.put("minimum", Short.MIN_VALUE);
        object.put("maximum", Short.MAX_VALUE);
        return object;
      };
    }
  },

  /**
   * java.lang.char
   */

  CHAR {
    @Override
    public String typeName()
    {
      return char.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "number");
        object.put("description", "A primitive char.");
        object.put("minimum", Character.MIN_VALUE);
        object.put("maximum", Character.MAX_VALUE);
        return object;
      };
    }
  },

  /**
   * java.lang.int
   */

  INT {
    @Override
    public String typeName()
    {
      return int.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "number");
        object.put("description", "A primitive int.");
        object.put("minimum", Integer.MIN_VALUE);
        object.put("maximum", Integer.MAX_VALUE);
        return object;
      };
    }
  },

  /**
   * java.lang.long
   */

  LONG {
    @Override
    public String typeName()
    {
      return long.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "number");
        object.put("description", "A primitive long.");
        object.put("minimum", Long.MIN_VALUE);
        object.put("maximum", Long.MAX_VALUE);
        return object;
      };
    }
  },

  /**
   * java.lang.double
   */

  DOUBLE {
    @Override
    public String typeName()
    {
      return double.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "number");
        object.put("minimum", 2.2250738585072014e-308);
        object.put("maximum", 1.7976931348623157e308);
        object.put("description", "An IEEE764 64-bit floating point value.");
        return object;
      };
    }
  },

  /**
   * java.lang.float
   */

  FLOAT {
    @Override
    public String typeName()
    {
      return float.class.getCanonicalName();
    }

    @Override
    public SjDefinitionType create(
      final SjGeneratorConfiguration configuration)
    {
      return () -> {
        final var mapper = configuration.mapper();
        final var object = mapper.createObjectNode();
        object.put("type", "number");
        object.put("minimum", 1.1754943508222875e-38);
        object.put("maximum", 3.4028234663852886e38);
        object.put("description", "An IEEE764 32-bit floating point value.");
        return object;
      };
    }
  },
}
