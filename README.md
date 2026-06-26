sumjack
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.sumjack/com.io7m.sumjack.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.sumjack%22)
[![Maven Central (snapshot)](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fcom%2Fio7m%2Fsumjack%2Fcom.io7m.sumjack%2Fmaven-metadata.xml&style=flat-square)](https://central.sonatype.com/repository/maven-snapshots/com/io7m/sumjack/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/sumjack.svg?style=flat-square)](https://codecov.io/gh/io7m-com/sumjack)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.sumjack](./src/site/resources/sumjack.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/sumjack/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/sumjack/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/sumjack/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/sumjack/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/sumjack/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/sumjack/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/sumjack/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/sumjack/actions?query=workflow%3Amain.windows.temurin.lts)|

## Repository Relocation

Development of this project has moved to an
[open-source but not open-contribution](https://sqlite.org/copyright.html#notopencontrib)
model.

Source code and commits will remain publicly available perpetually, but issues
and/or pull requests will be rejected and/or ignored. Additionally, this project
will now only be available via a read-only mirror at:

  https://codeberg.org/io7m-com/sumjack


## Sumjack

A JSON schema generator for [Jackson](https://jackson.tools) annotated Java types.

## Usage

### Building

```
$ mvn clean package
```

### Overview

The `sumjack` package is intended to produce [JSON schema](https://json-schema.org/)
documents from hierarchies of [Jackson](https://jackson.tools) annotated Java
types. The package provides a _generator_ that walks a tree of Java classes,
inspects the `@JsonProperty` annotations on the class methods, and then calls
registered _definitions_ to generate fragments of JSON schemas that are
eventually combined into a single schema document.

### Five Second Example

```
final var configuration =
  SjGeneratorConfiguration.builder()
    .setId(URI.create("urn:com.io7m.example:1.0"))
    .setMapper(JsonMapper.shared())
    .setRootType(Example.class)
    .setSchemaVersion(SjSchemaVersion.DRAFT_2020_12)
    .setTitle("Example 1.0")
    .build();

final var generator =
  SjGenerators.create(configuration);

generator.executeAndWrite(outputFile);
```

### Classes And Annotations

The `sumjack` package is opinionated about the kinds of classes for
which it will generate schemas. Specifically, the package expects to be
generating schemas for hierarchies of classes that are expressed in an
_algebraic sums and products_ style. That is, the classes for which schemas
are to be generated must be one of:

  * Sealed interfaces (representing _sum_ types)
  * Records (representing _product_ types)
  * Primitives (`int`, `long`, etc.)
  * Collections (`java.util.Map`, `java.util.Set`, `java.util.List`, `java.util.SortedMap`, `java.util.Optional`, etc.)
  * Enums

The package is capable of producing schemas for any classes that fall into
the above categories without any kind of extra configuration or work needed
on the part of the user of the package. However, if a class does not fit into
one of these categories, then it is necessary to register a custom _definition_
in order to generate a schema for it.

### Definition

A _definition_ is a Java function that, when evaluated, produces a schema
object. For example, a _definition_ that produces a schema for the
`java.util.UUID` type might look like this:

```
SjGeneratorConfiguration configuration;

() -> {
  final var mapper = configuration.mapper();
  final var object = mapper.createObjectNode();
  object.put("description", "An RFC 9562 UUID string.");
  object.put("type", "string");
  object.put("format", "uuid");
  return object;
};
```

The `sumjack` package includes definitions for many of the standard Java
classes, although these must be enabled manually by adding them to the
`SjGeneratorConfiguration` value used to configure the generator:

```
final var configuration =
  SjGeneratorConfiguration.builder()
    .addDefinitions(SjUUID.UUID)
    .addDefinitions(SjBigInteger.BIG_INTEGER)
    .addDefinitions(SjOffsetDateTime.OFFSET_DATE_TIME)
    .setId(URI.create("urn:com.io7m.example:1.0"))
    .setMapper(JsonMapper.shared())
    .setRootType(Example.class)
    .setSchemaVersion(SjSchemaVersion.DRAFT_2020_12)
    .setTitle("Example 1.0")
    .build();
```

### Generation

#### Sealed Interfaces

A _sealed interface_ is expected to represent a _sum type_ and therefore
compiles down to a schema that matches `oneOf` a set of types. For example:

```
sealed interface SimpleBase0Type
  permits SimpleBaseA,
  SimpleBaseB,
  SimpleBaseC
{

}
```

Produces a schema:

```
"SimpleBase0Type": {
  "oneOf": [
    {
      "$ref": "#/$defs/SimpleBaseA"
    },
    {
      "$ref": "#/$defs/SimpleBaseB"
    },
    {
      "$ref": "#/$defs/SimpleBaseC"
    }
  ]
},
```

#### Records

A _record_ is expected to represent a _product type_. The produced schema
for a record type is simply the `@JsonProperty` annotated constructor
parameters. For example:

```
public record Vector3(
  @JsonProperty(value = "X", required = true)
  double x,
  @JsonProperty(value = "Y", required = true)
  double y,
  @JsonProperty(value = "Z", required = true)
  double z)
{

}
```

Produces a schema:

```
"Vector3": {
  "type": "object",
  "properties": {
    "X": {
      "$ref": "#/$defs/double"
    },
    "Y": {
      "$ref": "#/$defs/double"
    },
    "Z": {
      "$ref": "#/$defs/double"
    }
  },
  "required": [
    "X",
    "Y",
    "Z"
  ]
},
"double": {
  "type": "number",
  "minimum": 2.2250738585072014E-308,
  "maximum": 1.7976931348623157E308,
  "description": "An IEEE764 64-bit floating point value."
}
```

#### Enums

Enums are translated to enumeration strings.

```
public enum TrafficLight
{
  RED,
  GREEN,
  YELLOW
}
```

Produces a schema:

```
"TrafficLight": {
  "type": "string",
  "enum": [
    "RED",
    "GREEN",
    "YELLOW"
  ]
}
```

#### Primitives

The Java primitives are, if the definitions in the `SjPrimitives` class
are registered, transformed to numeric and boolean types:

```
"boolean": {
  "type": "boolean",
  "description": "A boolean value."
},
"byte": {
  "type": "number",
  "description": "A primitive byte.",
  "minimum": -128,
  "maximum": 127
},
"char": {
  "type": "number",
  "description": "A primitive char.",
  "minimum": 0,
  "maximum": 65535
},
"double": {
  "type": "number",
  "minimum": 2.2250738585072014E-308,
  "maximum": 1.7976931348623157E308,
  "description": "An IEEE764 64-bit floating point value."
},
"float": {
  "type": "number",
  "minimum": 1.1754943508222875E-38,
  "maximum": 3.4028234663852886E38,
  "description": "An IEEE764 32-bit floating point value."
},
"int": {
  "type": "number",
  "description": "A primitive int.",
  "minimum": -2147483648,
  "maximum": 2147483647
},
"long": {
  "type": "number",
  "description": "A primitive long.",
  "minimum": -9223372036854775808,
  "maximum": 9223372036854775807
},
"short": {
  "type": "number",
  "description": "A primitive short.",
  "minimum": -32768,
  "maximum": 32767
}
```

#### Collections

Collection types are transformed to schema objects that match their types
as closely as possible. A `List<SimpleBaseA>` type, for example, becomes:

```
"List<SimpleBaseA>": {
  "type": "array",
  "items": {
    "$ref": "#/$defs/SimpleBaseA"
  }
},
"SimpleBaseA": {
  "type": "object",
  "properties": {},
  "required": []
},
```

`java.util.Optional` values are effectively erased and become non-required
properties of the containing object:

```
public record SimpleContainsOptional(
  @JsonProperty("Optional")
  @JsonPropertyDescription("An element that might not be there.")
  Optional<SimpleBaseA> elements)
{

}
```

Becomes:

```
"Optional<SimpleBaseA>": {
  "$ref": "#/$defs/SimpleBaseA"
},
"SimpleBaseA": {
  "type": "object",
  "properties": {},
  "required": []
},
"SimpleContainsOptional": {
  "type": "object",
  "properties": {
    "Optional": {
      "description": "An element that might not be there.",
      "$ref": "#/$defs/Optional<SimpleBaseA>"
    }
  },
  "required": []
},
```

