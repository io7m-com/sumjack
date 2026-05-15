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

import com.io7m.jaffirm.core.Preconditions;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SjJSchemaGraph
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SjJSchemaGraph.class);

  private final SjJSchema schema;
  private final Graph<SjJDefName, SchemaReference> graph;
  private final AsUnmodifiableGraph<SjJDefName, SchemaReference> graphRead;
  private final List<SjJDefName> namesOrdered;
  private final Map<SjJDefName, SjJDefType> defsByName;

  private SjJSchemaGraph(
    final SjJSchema inSchema,
    final Graph<SjJDefName, SchemaReference> inGraph,
    final ArrayList<SjJDefName> inNamesOrdered,
    final HashMap<SjJDefName, SjJDefType> inDefsByFullName)
  {
    this.schema =
      Objects.requireNonNull(inSchema, "Schema");
    this.graph =
      Objects.requireNonNull(inGraph, "Graph");
    this.graphRead =
      new AsUnmodifiableGraph<>(this.graph);
    this.namesOrdered =
      List.copyOf(inNamesOrdered);
    this.defsByName =
      Map.copyOf(inDefsByFullName);
  }

  public static SjJSchemaGraph createGraph(
    final SjJSchema schema)
  {
    final var graph =
      new DirectedAcyclicGraph<SjJDefName, SchemaReference>(SchemaReference.class);

    final var defsByFullName =
      new HashMap<SjJDefName, SjJDefType>();

    for (final var entry : schema.defs().entrySet()) {
      final var name = entry.getKey();
      final SjJDefName defName;
      if (!name.startsWith("#/$defs/")) {
        defName = new SjJDefName("#/$defs/" + name);
      } else {
        defName = new SjJDefName(name);
      }
      defsByFullName.put(defName, entry.getValue());
    }

    for (final var entry : defsByFullName.entrySet()) {
      addDefReferences(
        defsByFullName,
        graph,
        entry.getKey(),
        entry.getValue()
      );
    }

    final var ordered =
      new TopologicalOrderIterator<>(graph);

    final var namesOrdered =
      new ArrayList<SjJDefName>();
    final var namesSet =
      new HashSet<SjJDefName>();

    while (ordered.hasNext()) {
      final var name = ordered.next();
      Preconditions.checkPreconditionV(
        !namesSet.contains(name),
        "Name %s must only be used once",
        name
      );
      namesOrdered.add(name);
      namesSet.add(name);
    }
    Collections.reverse(namesOrdered);
    return new SjJSchemaGraph(schema, graph, namesOrdered, defsByFullName);
  }

  public Map<SjJDefName, SjJDefType> defsByName()
  {
    return this.defsByName;
  }

  private static void addDefReferences(
    final Map<SjJDefName, SjJDefType> defsByFullName,
    final Graph<SjJDefName, SchemaReference> graph,
    final SjJDefName defCurrent,
    final SjJDefType def)
  {
    Preconditions.checkPreconditionV(
      defsByFullName.containsKey(defCurrent),
      "Name %s must be in definition map",
      defCurrent
    );

    graph.addVertex(defCurrent);

    switch (def) {
      case final SjJDefArray array -> {
        addReference(
          defsByFullName,
          graph,
          defCurrent,
          new SjJDefName(array.items().target())
        );
      }

      case final SjJDefBoolean ignored -> {

      }

      case final SjJDefInteger ignored -> {

      }

      case final SjJDefNumber ignored -> {

      }

      case final SjJDefObject object -> {
        for (final var objectEntry : object.properties().entrySet()) {
          addDefReferences(
            defsByFullName,
            graph,
            defCurrent,
            objectEntry.getValue()
          );
        }

        switch (object.additionalProperties()) {
          case final SjJAdditionalPropertiesBoolean ignored -> {
          }
          case final SjJAdditionalPropertiesRef ref -> {
            final var target = new SjJDefName(ref.value().target());
            addReference(defsByFullName, graph, defCurrent, target);
          }
        }
      }
      case final SjJDefOneOf oneOf -> {
        for (final var ref : oneOf.refs()) {
          final var target = new SjJDefName(ref.target());
          addReference(defsByFullName, graph, defCurrent, target);
        }
      }
      case final SjJDefString ignored -> {

      }
      case final SjJRef ref -> {
        final var target = new SjJDefName(ref.target());
        addReference(defsByFullName, graph, defCurrent, target);
      }
    }
  }

  private static void addReference(
    final Map<SjJDefName, SjJDefType> defsByFullName,
    final Graph<SjJDefName, SchemaReference> graph,
    final SjJDefName current,
    final SjJDefName target)
  {
    Preconditions.checkPreconditionV(
      defsByFullName.containsKey(current),
      "Name %s must be in definition map",
      current
    );
    Preconditions.checkPreconditionV(
      defsByFullName.containsKey(target),
      "Name %s must be in definition map",
      target
    );

    LOG.debug("Reference: {} -> {}", current, target);
    graph.addVertex(current);
    graph.addVertex(target);
    graph.addEdge(current, target, new SchemaReference(current, target));
  }

  public Graph<SjJDefName, SchemaReference> graph()
  {
    return this.graphRead;
  }

  public SjJSchema schema()
  {
    return this.schema;
  }

  public List<SjJDefName> namesInDependencyOrder()
  {
    return this.namesOrdered;
  }

  public record SchemaReference(
    SjJDefName source,
    SjJDefName target)
  {
    @Override
    public String toString()
    {
      return "[%s -> %s]".formatted(this.source, this.target);
    }

    public SchemaReference
    {
      Objects.requireNonNull(source, "Source");
      Objects.requireNonNull(target, "Target");
    }
  }
}
