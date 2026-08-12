# Graph Report - .  (2026-08-12)

## Corpus Check
- Corpus is ~12,812 words - fits in a single context window. You may not need a graph.

## Summary
- 30 nodes · 34 edges · 5 communities (3 shown, 2 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 1 edges (avg confidence: 0.95)
- Token cost: 20,000 input · 7,634 output

## Community Hubs (Navigation)
- graphify Project Integration
- Maven Wrapper Script
- Application Test Suite
- Spring Boot Bootstrap
- Base Package

## God Nodes (most connected - your core abstractions)
1. `graphify` - 6 edges
2. `ConnectautoApplication` - 3 edges
3. `ConnectautoApplicationTests` - 3 edges
4. `graphify query <question>` - 3 edges
5. `graphify (skill trigger /graphify)` - 2 edges
6. `graphify path <A> <B>` - 1 edges
7. `graphify explain <concept>` - 1 edges
8. `graphify update .` - 1 edges
9. `GRAPH_REPORT.md` - 1 edges
10. `graphify-out/wiki/index.md` - 1 edges

## Surprising Connections (you probably didn't know these)
- `graphify (skill trigger /graphify)` --semantically_similar_to--> `graphify`  [INFERRED] [semantically similar]
  .claude/CLAUDE.md → CLAUDE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **graphify CLI workflow (query/path/explain/update)** — claude_graphify, claude_graphify_query, claude_graphify_path, claude_graphify_explain, claude_graphify_update [EXTRACTED 1.00]

## Communities (5 total, 2 thin omitted)

### Community 0 - "graphify Project Integration"
Cohesion: 0.20
Nodes (10): graphify (skill trigger /graphify), .claude/skills/graphify/SKILL.md, graphify-out/graph.json, GRAPH_REPORT.md, graphify, graphify explain <concept>, graphify path <A> <B>, graphify query <question> (+2 more)

### Community 1 - "Maven Wrapper Script"
Cohesion: 0.33
Nodes (6): mvnw script, clean(), die(), exec_maven(), set_java_home(), verbose()

### Community 2 - "Application Test Suite"
Cohesion: 0.60
Nodes (3): org.junit.jupiter.api.Test, org.springframework.boot.test.context.SpringBootTest, ConnectautoApplicationTests

## Knowledge Gaps
- **8 isolated node(s):** `com.acc:connectauto`, `graphify path <A> <B>`, `graphify explain <concept>`, `graphify update .`, `GRAPH_REPORT.md` (+3 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What connects `com.acc:connectauto`, `graphify path <A> <B>`, `graphify explain <concept>` to the rest of the system?**
  _8 weakly-connected nodes found - possible documentation gaps or missing edges._