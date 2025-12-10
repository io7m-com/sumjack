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

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.ResolvedMethod;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Collectors;

record SjFullyResolvedType(
  ResolvedType type,
  SortedMap<String, ResolvedMethod> methods,
  SortedSet<String> methodRequired,
  SortedMap<String, String> methodDescriptions,
  List<SjFullyResolvedType> subclasses,
  Optional<String> description,
  Optional<String> typeProperty)
{
  static String refName(
    final ResolvedType type)
  {
    return String.format("#/$defs/%s", shortName(type));
  }

  static String shortName(
    final ResolvedType type)
  {
    final var simpleName = type.getErasedType().getSimpleName();
    final var parameters = type.getTypeParameters();
    if (parameters.isEmpty()) {
      return simpleName;
    }
    return String.format(
      "%s<%s>",
      simpleName,
      parameters.stream()
        .map(SjFullyResolvedType::shortName)
        .collect(Collectors.joining(","))
    );
  }

  String name()
  {
    return shortName(this.type);
  }

  String refName()
  {
    return refName(this.type);
  }

  public boolean isSealedInterface()
  {
    final var clazz = this.type.getErasedType();
    return clazz.isInterface() && clazz.isSealed();
  }

  public boolean isEnum()
  {
    final var clazz = this.type.getErasedType();
    return clazz.isEnum();
  }

  public boolean isPrimitive()
  {
    final var clazz = this.type.getErasedType();
    return clazz.isPrimitive();
  }

  public void putDescription(
    final ObjectNode object)
  {
    this.description.ifPresent(text -> {
      object.put("description", text);
    });
  }

  public boolean isBaseType(
    final Class<?> target)
  {
    final var clazz = this.type.getErasedType();
    return Objects.equals(clazz, target);
  }

  public boolean isRecord()
  {
    final var clazz = this.type.getErasedType();
    return clazz.isRecord();
  }
}
