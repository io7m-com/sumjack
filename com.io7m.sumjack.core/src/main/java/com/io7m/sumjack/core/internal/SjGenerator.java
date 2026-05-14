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

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedArrayType;
import com.fasterxml.classmate.types.ResolvedInterfaceType;
import com.fasterxml.classmate.types.ResolvedObjectType;
import com.fasterxml.classmate.types.ResolvedPrimitiveType;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.io7m.sumjack.core.SjDefinitionType;
import com.io7m.sumjack.core.SjException;
import com.io7m.sumjack.core.SjGeneratorConfiguration;
import com.io7m.sumjack.core.SjGeneratorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * The main generator.
 */

public final class SjGenerator
  implements SjGeneratorType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SjGenerator.class);

  private static final TypeResolver RESOLVER =
    new TypeResolver();

  private final SjGeneratorConfiguration configuration;
  private final TreeMap<String, SjFullyResolvedType> types;
  private final TreeMap<String, SjDefinitionType> definitions;
  private ObjectNode defs;
  private ObjectNode schema;
  private SjFullyResolvedType rootResolved;

  private SjGenerator(
    final SjGeneratorConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.types =
      new TreeMap<>();
    this.definitions =
      new TreeMap<>();
  }

  /**
   * @param configuration The generator configuration
   *
   * @return A new generator
   */

  public static SjGeneratorType create(
    final SjGeneratorConfiguration configuration)
  {
    return new SjGenerator(configuration);
  }

  private static List<Type> rawSubclassesOf(
    final Type type)
  {
    return switch (type) {
      case final Class<?> clazz -> {
        final var permitted = clazz.getPermittedSubclasses();
        if (permitted != null) {
          yield List.of(permitted);
        }
        yield List.of();
      }

      case final ResolvedInterfaceType clazz -> {
        final var rawClazz = clazz.getErasedType();
        final var permitted = rawClazz.getPermittedSubclasses();
        if (permitted != null) {
          yield List.of(permitted);
        }
        yield List.of();
      }

      case final ResolvedObjectType ignored -> {
        yield List.of();
      }

      case final ResolvedArrayType ignored -> {
        yield List.of();
      }

      case final ResolvedPrimitiveType ignored -> {
        yield List.of();
      }

      default -> {
        throw new IllegalStateException(
          "Unable to extract subclasses from type %s (%s)"
            .formatted(type, type.getClass())
        );
      }
    };
  }

  private static SjFullyResolvedType resolve(
    final Type type,
    final List<? extends Type> typeArguments)
    throws SjException
  {
    final var typeArgumentsArray = new Type[typeArguments.size()];
    typeArguments.toArray(typeArgumentsArray);

    final var base =
      RESOLVER.resolve(type, typeArgumentsArray);

    final var methodResolver =
      new MemberResolver(RESOLVER);

    methodResolver.setMethodFilter(rawMethod -> {
      return Stream.of(rawMethod.getAnnotations())
        .anyMatch(annotation -> {
          return Objects.equals(
            annotation.annotationType(),
            JsonProperty.class
          );
        });
    });

    final var withMembers =
      methodResolver.resolve(
        base,
        null,
        null
      );

    final var methods =
      new TreeMap<String, ResolvedMethod>();
    final var methodsRequired =
      new TreeSet<String>();
    final var methodDescriptions =
      new TreeMap<String, String>();

    for (final var method : withMembers.getMemberMethods()) {
      final var rawMethod =
        method.getRawMember();
      final JsonProperty annotation =
        checkMethodAnnotated(type, rawMethod);

      final var propertyName = annotation.value();
      if (annotation.required()) {
        methodsRequired.add(propertyName);
      }
      methods.put(propertyName, method);

      final var descAnnotation =
        rawMethod.getAnnotation(JsonPropertyDescription.class);
      if (descAnnotation != null) {
        methodDescriptions.put(propertyName, descAnnotation.value());
      }
    }

    final var subclasses = new ArrayList<SjFullyResolvedType>();
    final var rawSubclasses = rawSubclassesOf(type);
    for (final var subclass : rawSubclasses) {
      final List<ResolvedType> bindings;
      if (subclass instanceof final Class<?> subclazz) {
        if (subclazz.getTypeParameters().length > 0) {
          bindings = base.getTypeBindings().getTypeParameters();
        } else {
          bindings = List.of();
        }
      } else {
        bindings = List.of();
      }

      subclasses.add(resolve(subclass, bindings));
    }

    final var typeProperty =
      typePropertyValue(base);
    final var classDescription =
      classDescriptionOf(type);

    return new SjFullyResolvedType(
      base,
      methods,
      methodsRequired,
      methodDescriptions,
      subclasses,
      classDescription,
      typeProperty
    );
  }

  private static Optional<String> classDescriptionOf(
    final Type type)
  {
    return switch (type) {
      case final Class<?> clazz -> {
        final var classDescriptionAnnot =
          clazz.getAnnotation(JsonClassDescription.class);
        if (classDescriptionAnnot != null) {
          yield Optional.of(classDescriptionAnnot.value());
        }
        yield Optional.empty();
      }

      case final ResolvedInterfaceType clazz -> {
        yield classDescriptionOf(clazz.getErasedType());
      }

      case final ResolvedObjectType clazz -> {
        yield classDescriptionOf(clazz.getErasedType());
      }

      case final ResolvedArrayType clazz -> {
        yield classDescriptionOf(clazz.getErasedType());
      }

      case final ResolvedPrimitiveType clazz -> {
        yield classDescriptionOf(clazz.getErasedType());
      }

      default -> {
        throw new IllegalStateException(
          "Unable to extract description from type %s (%s)"
            .formatted(type, type.getClass())
        );
      }
    };
  }

  private static void findAllAnnotatedInterfaces(
    final Set<Class<?>> allInterfaces,
    final ResolvedType type)
  {
    final var erased =
      type.getErasedType();
    final var subtypes =
      erased.getAnnotation(JsonSubTypes.class);

    if (subtypes != null) {
      allInterfaces.add(erased);
    }

    for (final var parent : type.getImplementedInterfaces()) {
      findAllAnnotatedInterfaces(allInterfaces, parent);
    }
  }

  private static Optional<SjTypeAttribute> typePropertyValue(
    final ResolvedType type)
  {
    final var allInterfaces = new HashSet<Class<?>>();
    findAllAnnotatedInterfaces(allInterfaces, type);

    for (final var interfaceT : allInterfaces) {
      final var typeInfo =
        interfaceT.getAnnotation(JsonTypeInfo.class);
      final var subtypes =
        interfaceT.getAnnotation(JsonSubTypes.class);

      for (final var subtype : subtypes.value()) {
        if (Objects.equals(subtype.value(), type.getErasedType())) {
          return Optional.of(
            new SjTypeAttribute(
              typeInfo.property(),
              subtype.name()
            )
          );
        }
      }
    }
    return Optional.empty();
  }

  private static JsonProperty checkMethodAnnotated(
    final Type type,
    final Method rawMethod)
    throws SjException
  {
    final var annotation = rawMethod.getAnnotation(JsonProperty.class);
    if (annotation == null) {
      throw new SjException(
        "Method is missing a @JsonProperty annotation.",
        "error-missing-json-property",
        Map.ofEntries(
          Map.entry("Class", type.getTypeName()),
          Map.entry("Method", rawMethod.getName())
        ),
        Optional.empty()
      );
    }
    return annotation;
  }

  @Override
  public SjGeneratorConfiguration configuration()
  {
    return this.configuration;
  }

  @Override
  public ObjectNode execute()
    throws SjException
  {
    this.collectTypeRoot();
    LOG.debug("Collected {} types", this.types.size());
    this.createDefinitions();
    LOG.debug("Created {} definitions.", this.definitions.size());
    return this.generateSchema();
  }

  private ObjectNode generateSchema()
    throws SjException
  {
    final var mapper = this.configuration.mapper();
    final var rootDef = mapper.createObjectNode();
    rootDef.put("$ref", this.rootResolved.refName());
    final var oneOf = mapper.createArrayNode();
    oneOf.add(rootDef);

    this.defs = mapper.createObjectNode();
    this.executeDefinitions();

    this.schema = mapper.createObjectNode();
    this.schema.put("$schema", this.configuration.schemaVersion().id());
    this.schema.put("$id", this.configuration.id().toString());
    this.schema.put("title", this.configuration.title());
    this.schema.set("oneOf", oneOf);
    this.schema.set("$defs", this.defs);
    return this.schema;
  }

  private void executeDefinitions()
    throws SjException
  {
    for (final var name : this.definitions.keySet()) {
      final var definition = this.definitions.get(name);
      this.defs.set(name, definition.execute());
    }
  }

  private void createDefinitions()
    throws SjException
  {
    for (final var name : this.types.keySet()) {
      if (this.definitions.containsKey(name)) {
        continue;
      }

      final var type = this.types.get(name);
      this.definitions.put(name, this.createDefinition(type));
    }

    this.createStandardDefinition(
      String.class,
      new SjDefinitionString(this.configuration)
    );

    this.createDefinitionsCustom();
  }

  private void createStandardDefinition(
    final Class<?> clazz,
    final SjDefinitionType definition)
    throws SjException
  {
    final var type = resolve(clazz, List.of());
    this.definitions.put(type.name(), definition);
  }

  private void createDefinitionsCustom()
  {
    for (final var provider : this.configuration.definitions()) {
      this.definitions.put(
        provider.typeName(),
        provider.create(this.configuration)
      );
    }
  }

  private SjDefinitionType createDefinition(
    final SjFullyResolvedType type)
  {
    if (Objects.equals(type.type().getErasedType(), Object.class)) {
      return new SjDefinitionObject(this.configuration);
    }

    if (type.isSealedInterface()) {
      return new SjDefinitionSealedInterface(this.configuration, type);
    }
    if (type.isEnum()) {
      return new SjDefinitionEnum(this.configuration, type);
    }
    if (type.isPrimitive()) {
      return new SjDefinitionPrimitive(this.configuration, type);
    }

    if (type.isBaseType(SortedMap.class)) {
      return new SjDefinitionSortedMap(this.configuration, type);
    }
    if (type.isBaseType(Map.class)) {
      return new SjDefinitionMap(this.configuration, type);
    }
    if (type.isBaseType(SortedSet.class)) {
      return new SjDefinitionSortedSet(this.configuration, type);
    }
    if (type.isBaseType(Set.class)) {
      return new SjDefinitionSet(this.configuration, type);
    }
    if (type.isBaseType(List.class)) {
      return new SjDefinitionList(this.configuration, type);
    }
    if (type.isBaseType(Optional.class)) {
      return new SjDefinitionOptional(this.configuration, type);
    }

    if (type.isRecord()) {
      return new SjDefinitionRecord(this.configuration, type);
    }

    return new SjDefinitionFailing(this.configuration, type);
  }

  private void collectTypeRoot()
    throws SjException
  {
    final var root = this.configuration.rootType();
    this.rootResolved = resolve(root, List.of());
    this.collectTypes(this.rootResolved);
  }

  private void collectTypes(
    final SjFullyResolvedType currentType)
    throws SjException
  {
    if (this.types.containsKey(currentType.name())) {
      return;
    }

    LOG.debug("Collected type: {}", currentType.name());
    this.types.put(currentType.name(), currentType);

    for (final var parameter : currentType.type().getTypeParameters()) {
      this.collectTypes(resolve(parameter, List.of()));
    }
    for (final var method : currentType.methods().values()) {
      this.collectTypes(resolve(method.getReturnType(), List.of()));
    }
    for (final var subclass : currentType.subclasses()) {
      this.collectTypes(subclass);
    }
  }
}
