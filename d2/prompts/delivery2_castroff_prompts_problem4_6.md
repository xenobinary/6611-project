# Problem 4 CASTROFF Prompt

## CASTROFF Mapping

- **Constraints**: Work only within the current iBank project scope and existing Java Swing implementation model. Keep suggestions feasible for a small student team. Do not assume external infrastructure, real card hardware, or production banking integrations. Distinguish suggestion from final design truth. Avoid recommending features that would significantly distort later measurement tasks.
- **Audience**: Course instructor, TA, and evaluators reviewing design rationale in slides or report form.
- **Structure**: Organize output into design issues observed, suggested improvements, tradeoffs, and implementation impact.
- **Tone**: Academic, concise, design-review oriented, and presentation-ready.
- **Role**: Act as a software architect and design-review assistant for a Java-based Automated Banking Machine project named `iBank`.
- **Output format**: Markdown tables, bullet lists, and optional PlantUML or Mermaid syntax for small architectural illustrations.
- **Focus**: Improve architecture clarity, maintainability, feasibility, and metric-friendliness without overengineering.
- **Function**: Brainstorm and critique candidate design improvements, not generate final submission text automatically.

## Prompt

```markdown
You are assisting a student team in Software Measurement with the iBank project. Act as a software architect and design-review assistant.

Context:
- iBank is a simplified Automated Banking Machine (ABM) implemented as a Java GUI system.
- The project uses Java Swing for the interface and SQLite for persistence.
- The system should remain simple enough for a student team to explain, measure, and maintain.
- The purpose of this task is not to invent a new product scope, but to improve or critique the current design.
- The prompt must follow the CASTROFF framework: Constraints, Audience, Structure, Tone, Role, Output format, Focus, and Function.

Problem 4 task:
- Provide design suggestions for the current iBank architecture and class structure.
- Emphasize improvements that increase clarity, maintainability, and measurement readiness.
- Do not suggest unnecessary enterprise patterns or advanced infrastructure.

Design context to assume:
- MVC-like separation is already present.
- The project already contains user roles such as bank client, administrator, and technician.
- Navigation is GUI-based.
- Authentication, transactions, exchange rates, and persistence already exist conceptually.

Output requirements:
1. Start with a short diagnosis of the current design strengths.
2. Identify 3 to 6 design issues or improvement opportunities.
3. For each suggestion, provide:
   - issue observed
   - why it matters
   - recommended change
   - expected benefit
   - possible downside or tradeoff
4. Separate suggestions into categories such as:
   - architecture
   - class design
   - coupling/cohesion
   - UI interaction structure
   - persistence design
5. Mark each suggestion as one of:
   - high-priority
   - medium-priority
   - optional
6. Include a section named "Metric Impact" explaining how each design suggestion could influence later measures such as cyclomatic complexity, readability, coupling, cohesion, or SLOC.
7. If useful, include a small PlantUML or Mermaid snippet to illustrate a suggested improvement.

Important constraints:
- Do not assume a complete redesign from scratch.
- Do not suggest distributed systems, real ATM hardware, or production banking APIs.
- Keep recommendations realistic for a student project.
- Distinguish clearly between must-fix suggestions and nice-to-have suggestions.
```

---

# Problem 5 CASTROFF Prompt

## CASTROFF Mapping

- **Constraints**: Focus on identifying practical LLM-assisted tooling and a defensible logical-SLOC counting scheme for Java. Do not assume exact vendor support unless verifiable. Do not claim exact equivalence between tools unless justified. Prefer methods that are reproducible in a student environment.
- **Audience**: TA, instructor, and evaluators interested in why a given tool and scheme were selected.
- **Structure**: Organize output by tool candidates, comparison criteria, decision rationale, LSLOC counting strategy, validation approach, and risks.
- **Tone**: Analytical, evidence-based, and technically precise.
- **Role**: Act as a software measurement analyst and tooling evaluator.
- **Output format**: Markdown comparison tables, bullet lists, and optional diagrams for decision logic.
- **Focus**: Finding appropriate tools and justifying a logical SLOC scheme for Java.
- **Function**: Brainstorm tool choices, compare them, and propose an implementation/testing scheme.

## Prompt

```markdown
You are assisting a student team in Software Measurement with the iBank project. Act as a software measurement analyst and tooling evaluator.

Context:
- The project is a Java-based ABM application named iBank.
- The team must evaluate source code metrics in a development environment.
- Problem 5 focuses on finding appropriate tools and defining a scheme for measuring logical SLOC.
- The project requires use of one or more publicly available GAI tools relying on LLMs.
- The prompt must follow the CASTROFF framework.

Problem 5 task:
1. Identify candidate tools for Java SLOC analysis, especially logical SLOC.
2. Compare their strengths and weaknesses for a student project.
3. Propose a practical LSLOC counting scheme that can be explained and reproduced.
4. Explain how to validate the scheme against a reference implementation or benchmark.

Known context to consider:
- The codebase uses Java Swing and SQLite.
- The team may compare tools such as UCC, cloc, scc, or custom scripts.
- Exact logical SLOC is more difficult than physical SLOC because it depends on statement parsing.
- Comments, string literals, multi-line constructs, and control-flow keywords complicate counting.

Output requirements:
1. Provide a short candidate-tool list with 3 to 5 options.
2. Include a comparison table with columns such as:
   - tool
   - supports Java
   - physical SLOC support
   - logical SLOC support
   - strengths
   - weaknesses
   - ease of use for students
3. Recommend one primary reference tool and explain why.
4. Recommend whether a custom LSLOC script is acceptable and under what conditions.
5. Provide a detailed LSLOC scheme proposal covering:
   - comment handling
   - string/char literal handling
   - statement boundaries
   - control-flow keywords
   - compiler directives/imports/packages
   - data declarations vs executable instructions
   - multi-line statements
6. Include a section named "Validation Strategy" with:
   - how to compare script output to a reference tool
   - what kinds of mismatches to investigate
   - when a gap is acceptable and when it is not
7. Include a section named "Decision Rationale" explaining why the final tool/scheme choice is appropriate for the course project.
8. If helpful, include a small flowchart or PlantUML/Mermaid diagram showing the LSLOC decision pipeline.

Important constraints:
- Do not claim that a regex-only method is exact unless proven.
- Distinguish exact counting from approximation.
- Be explicit about tradeoffs between reproducibility, accuracy, and implementation effort.
- Keep the recommendations feasible for a student team.
```

---

# Problem 6 CASTROFF Prompt

## CASTROFF Mapping

- **Constraints**: Focus on an automated readability metric selection problem with no external human participants. The chosen metric must be static, objective, and defensible. Do not overclaim literature support; distinguish direct evidence from indirect evidence.
- **Audience**: Instructor and TA who will judge the soundness of the metric-selection reasoning.
- **Structure**: Organize output by candidate metrics, evaluation criteria, rejection reasons, final selection rationale, and reference support.
- **Tone**: Critical, evidence-based, and presentation-oriented.
- **Role**: Act as a software measurement analyst specializing in readability metrics.
- **Output format**: Markdown tables, bullet lists, and optional PlantUML or Mermaid decision diagrams.
- **Focus**: Brainstorming candidate readability metrics and defending one selection for the project.
- **Function**: Help construct an argument, not simply name a metric.

## Prompt

```markdown
You are assisting a student team in Software Measurement with the iBank project. Act as a software measurement analyst specializing in readability metrics.

Context:
- The team must choose a readability-related metric for source code evaluation.
- No external human participants are available.
- Therefore the metric must be automated, objective, static, and reproducible.
- Candidate metrics may include Cyclomatic Complexity (CC), Physical SLOC (PSLOC), Comment Ratio (CR), Blank Line Ratio (BLR), or similar static measures.

Problem 6 task:
1. Brainstorm candidate metrics for automated source-code readability evaluation.
2. Compare them based on how directly they measure readability.
3. Reject weaker candidates with explicit reasoning.
4. Select one final metric and defend the choice.

Output requirements:
1. Start by defining what "readability" means under the project constraints.
2. Explain why the absence of human experiments changes the metric-selection strategy.
3. Provide a candidate metric table with columns such as:
   - metric
   - what it measures
   - why it is useful
   - why it is weak as a primary readability metric
4. Clearly distinguish:
   - direct readability indicators
   - indirect readability indicators
5. Include a decision-oriented section that explains why the selected metric is closer to readability than the rejected candidates.
6. If BLR is selected, explicitly justify why it is more direct than CC, PSLOC, and CR.
7. Include a short literature-support section and a style-guide support section.
8. Provide one or two slide-friendly summary paragraphs.
9. If useful, include a PlantUML or Mermaid decision diagram showing the selection process.

Important constraints:
- Do not overstate literature support if the evidence is only indirect.
- Make the argument comparative: explain why the chosen metric is better than the alternatives, not just why it is good.
- Keep the final reasoning concise enough to turn into slides.
```

---

## Expected Output Use

The expected GAI output should be treated as brainstorming and decision-support material. Before use in slides or reports, the team should:

- rewrite the content in its own wording
- verify any references or claimed standards
- remove unsupported or exaggerated claims
- align the selected reasoning with the actual implemented project scope

