# Delivery 1 CASTROFF-Based GAI Prompt

## CASTROFF Mapping

- **Constraints**: Follow the D1 project scope only, assume Canadian legal context, list assumptions, avoid unsupported claims, do not copy text verbatim, include citation needs, keep the ABM simple enough for a three-student team to implement later in Java Swing or JavaFX, and avoid features that would make D2/D3 metrics unnecessarily complex.
- **Audience**: Course instructor and evaluators in a Zoom presentation.
- **Structure**: Organize output by D1 Problem 1, Problem 2, Problem 3, plus assumptions, citations, and validation checklist.
- **Tone**: Academic, concise, evidence-based, and presentation-ready.
- **Role**: Act as a software measurement analyst and requirements analyst for an Automated Banking Machine project named `iBank`.
- **Output format**: Markdown tables and lists; include PlantUML or Mermaid for the use case diagram.
- **Focus**: ABM selection, SMART GQM goal, questions and candidate metrics, and use case model.
- **Function**: Brainstorm and organize a first draft, not produce final unverified submission content.

## Prompt To Use

```markdown
You are assisting a student team in Software Measurement with Delivery 1 for a project named iBank. Act as both a software measurement analyst and a requirements analyst.

Context:
- iBank is an Automated Banking Machine (ABM) product.
- An ABM is an automated self-service machine that enables bank clients to carry out transactions.
- The project process includes elements of agile methodologies and DevOps.
- Every problem must use one or more publicly available GAI tools that rely on LLMs.
- The prompt must be based on the CASTROFF prompt engineering framework: Constraints, Audience, Structure, Tone, Role, Output format, Focus, and Function.
- All non-original work must be cited and referenced appropriately.
- Claims must be evidence-based. Do not copy external text verbatim.

Delivery 1 tasks:
1. Problem 1: Select a specific ABM suitable for use in Canada in the 21st century, and give a brief description. It must be legal in the applicable Canadian context. Include explicit assumptions.
2. Problem 2: Using the Goal-Question-Metric (GQM) approach or an extension, present one goal specific to iBank. The goal should aim to be SMART: Specific, Measurable, Attainable, Realistic, and Timely. Articulate exactly 2N (N is team size) questions related to that goal. Discuss whether metrics help answer those questions.
3. Problem 3: Construct a use case model for iBank. The model must be both graphical and textual and must include definitions of actors and use cases. Use user stories to organize the use cases.

Team information:
- Team size N: 3
- Therefore, for Problem 2, generate exactly 2N = 6 questions.
- Scope preference: Keep iBank simple, straightforward, and easy to implement in D2 (implementation phase) as a Java GUI application, while still being rich enough for D3 (evaluation phase) metrics such as cyclomatic complexity, WMC, CF, LCOM*, UCP, Logical SLOC, and correlation analysis. Card reading must be simulated by manual entry or GUI selection of a sample card number; do not assume real magnetic stripe, chip, NFC, or other card-reader hardware.

Course concepts to apply:
- Measurement assigns numbers or categories to attributes of entities according to clearly defined rules.
- Distinguish metric, measure, and indicator.
- A metric should be traceable to a goal and relevant to the attribute being measured.
- A single measure without context is not enough for interpretation.
- GQM is top-down: goals lead to questions, and questions lead to metrics.
- Good questions should be neither too general nor too specific.
- Metrics should be selected with rationale, feasibility, objectivity, and usefulness in mind.
- Avoid metric misuse, such as using metrics without context or using one metric as the only basis for judgment.

Output requirements:
1. Start with a short recommended ABM concept for iBank, including why it is suitable for Canada.
2. Recommend a deliberately simple scope for iBank that avoids advanced or high-risk features such as real payment-network integration, real card-reader hardware, biometric authentication, cryptocurrency, live interbank transfers, fraud scoring, or hardware-level cash management.
3. Provide a slide-by-slide outline for D1 with suggested slide titles and bullet points.
4. For Problem 1, include:
   - Selected ABM type.
   - Brief description.
   - Primary users.
   - Supported transaction categories.
   - Canadian context and legal/regulatory assumptions.
   - Explicit project assumptions.
5. For Problem 2, include:
   - One SMART GQM goal using this template:
     Purpose: To <mission of measurement> the <entity of measurement> in order to <purpose of measurement> it.
     Perspective: Examine <quality focus> from the viewpoint of <stakeholder>.
     Environment: In the context of <environment>.
   - A SMART check table with columns: SMART element, evidence in the goal, possible weakness.
   - Exactly 2N questions (or 2N+ candidate questions) related to the goal.
   - For each question, list candidate metric(s), whether each metric is objective or subjective, entity, attribute, unit or scale, collection method, and why it helps answer the question.
   - Clearly identify any question that cannot be answered well by a metric alone and explain why.
6. For Problem 3, include:
   - Actor definitions.
   - Use case definitions.
   - A user-story-first table for each use case, with columns: use case, user story, acceptance notes for prototype.
   - A textual use case model table with columns: use case, primary actor, supporting actor, preconditions, main success scenario, exceptions, postconditions.
   - A graphical use case diagram in PlantUML or Mermaid syntax.
   - Notes explaining any include, extend, or generalization relationships.
7. Include a section named "Future Deliverables Fit" with:
   - Why the selected ABM scope can be implemented by a three-student team in Java Swing or JavaFX.
   - Which simple classes might exist later in D2, without writing code.
   - Why the scope supports later SLOC, readability, cyclomatic complexity, WMC, CF, LCOM*, UCP, and correlation measurements without becoming too large.
8. Include a section named "GAI Use Explanation" with:
   - What this prompt was intended to obtain.
   - How the output should be reviewed and modified before submission.
   - What parts require external citation or verification.
9. Include a section named "Potential References To Verify" with credible source categories, such as Canadian banking regulation, accessibility standards, ATM security guidance, GQM literature, and UML/use case modeling references. Do not invent exact bibliographic details. If you provide a citation, it must be real and verifiable.

Important constraints:
- Do not present generated content as final truth.
- Do not invent Canadian laws, standards, or references.
- If uncertain, state uncertainty and suggest what source should be checked.
- Keep language concise enough for slide conversion.
- Use original wording rather than copying from source material.
- Prefer fewer, clearer actors and use cases over a comprehensive banking-system model.
```

## Expected Output Use

The expected GAI output should be treated as brainstorming and organization support. Before using it in slides, the team should verify Canadian banking/legal claims, revise all text into the team's own wording, cite sources, and ensure the GQM questions and metrics are consistent with a simple iBank scope that remains feasible for D2 and D3.

## Reference For CASTROFF Definition

Tavakoli, H. (2026). The CASTROFF Framework: A Professional Standard for Prompt Engineering. In *Prompt Engineering for Everyone*. Apress. https://doi.org/10.1007/979-8-8688-2338-1_6
