# Delivery 3 — GAI Use with the CASTROFF Framework

## Tool and verification policy

Publicly available LLM used: **OpenAI Codex (GPT-5)**.

The LLM was used to critique measurement definitions, identify ambiguous
counting choices, propose reproducible output structures, and improve the
presentation narrative. It was **not** treated as the source of measured values.
Every value in D3 was generated from commit
`e616e598ece800477163988abdfc4f3ab0757cb4` by UCC-J 2020.01 (based on
UCC-G v.1.3.1) or the source-level
analysis scripts under `d3/tools/`.

## Problem 7 — Cyclomatic complexity

### CASTROFF prompt

> **Constraints:** Analyze only the Java production source at commit
> `e616e59`. Use UCC-J 2020.01 CC1 results. Do not infer complexity from line counts or
> screenshots. Keep method, class, and project aggregation distinct.
>
> **Audience:** The SOEN 6611 instructor and classmates.
>
> **Structure:** (1) define cyclomatic complexity, (2) state the counting
> scheme, (3) calculate the project and method distribution, (4) compare with a
> defensible threshold, and (5) state a qualitative conclusion and one focused
> improvement.
>
> **Tone:** Academic, concise, evidence-based, and cautious.
>
> **Role:** Act as a software measurement analyst reviewing a Java ABM.
>
> **Output:** A calculation checklist, a result-table schema, and
> presentation-ready conclusions. Use the threshold table in
> `source_code_control_flow_structure.pdf`, p. 19.
>
> **Focus:** Explain why the sum of method complexities is not interpreted
> using a per-method threshold.
>
> **Function:** Critique the proposed analysis and identify any unsupported
> claim; do not invent measurements.

### Output use and explanation

The response correctly emphasized that the lecture's thresholds are
module-level: 1-10 low, 11-20 moderate, 21-50 high, and above 50 very high.
This prevented the project total (`474`) from being mislabeled as a single
high-risk method. The final result therefore shows the distribution: 238
methods in the 1-10 low band, including two methods equal to 10; one method is
12, and none are above 20.
The proposed method/class/project distinction was retained; all numeric output
was replaced by the generated UCC-J 2020.01 data.

## Problem 8 — WMC, CF, and LCOM*

### CASTROFF prompt

> **Constraints:** WMC must use unnormalized method weights, where each method
> weight is UCC-J 2020.01 CC1. Define CF as directed source-level coupling to another
> project type divided by `N-1`; exclude inheritance, self-coupling, and Java
> library types. Use Henderson-Sellers LCOM*. State how constructors, static
> methods, interfaces, and classes with too few fields or methods are handled.
>
> **Audience:** A technical course evaluator who must reproduce every row.
>
> **Structure:** Give formulas and conventions first, then a per-class schema,
> then qualitative thresholds and limitations. Use only the rules stated in
> `ood_metrics_classes.pdf`: WMC > 100, LCOM* >= 0.8, and “lower is better”
> for CF when no universal numeric CF threshold is supplied.
>
> **Tone:** Precise, transparent, and non-prescriptive.
>
> **Role:** Act as a static-analysis reviewer.
>
> **Output:** A CSV field specification, validation checklist, and concise
> interpretation rules for WMC, CF, and LCOM*.
>
> **Focus:** Detect ambiguities that could make two tools report different
> values.
>
> **Function:** Explain non-applicable LCOM* cases and reject unsupported
> project-specific bands.

### Output use and explanation

The response highlighted two important ambiguities: MOOD CF is system-oriented,
while the assignment asks for a value for each class; and LCOM variants differ
substantially across tools. The final analysis therefore reports:

- `CF_i = outgoing project-type couplings / (N-1)` for each type and the MOOD
  system CF as the directed-coupling total divided by `N(N-1)`;
- Henderson-Sellers LCOM* using declared instance fields and declared instance
  methods, excluding constructors and static methods;
- `N/A` when fewer than two eligible methods or no eligible instance fields
  exist.

Unsupported project-specific cutoffs were not accepted. The final deck uses the
lecture's WMC and LCOM* thresholds, states that CF has no supplied universal
numeric threshold, and retains all raw values.

## Problem 9 — Use Case Points

### CASTROFF prompt

> **Constraints:** Use the actor and use-case definitions in
> `d2/software_design.md`. Count transactions from the numbered main-success
> stimulus-response transactions in the main-success scenarios. Do not
> double-count included sub-use cases. Apply the original
> UCP actor/use-case weights, TCF, ECF, and a productivity factor of 20
> person-hours per UCP. The repository has no contemporaneous time sheet, so
> any actual-effort value must be labeled as a retrospective baseline.
>
> **Audience:** The course marker evaluating Problem 9.
>
> **Structure:** UAW → UUCW → UUCP → TCF → ECF → adjusted UCP → effort →
> comparison with actual.
>
> **Tone:** Transparent and appropriately skeptical of false precision.
>
> **Role:** Act as a software estimation analyst.
>
> **Output:** Detailed factor tables, formulas, sensitivity comments, and a
> presentation-ready comparison.
>
> **Focus:** Explain why an estimate can be dependable without being exact.
>
> **Function:** Identify double counting and unsupported actual-effort claims.

### Output use and explanation

The response recommended excluding UC-S1 and UC-S2 because their work is
already represented inside the numbered transactions of the base use cases.
That recommendation was retained. It also warned that commit timestamps cannot
recover person-hours. The final comparison therefore labels 168 hours as a
retrospective baseline (three members × 56 hours), not an audited time sheet.
The 20 h/UCP estimate is 2,253 hours, 13.41 times the baseline; the conclusion is
that the uncalibrated productivity factor is unsuitable for this small reusable
student prototype.

## Problem 10 — LSLOC/WMC correlation

### CASTROFF prompt

> **Constraints:** Use exactly one observation per top-level class. Exclude the
> two interfaces because WMC is a class metric in this analysis. Use Logical
> SLOC and WMC from the same commit and counting run. Follow
> `software_measurement_data_analysis.pdf`: inspect distribution shape, choose
> Spearman for non-normal attribute values, use average ranks for ties, and do
> not infer causation. Report Pearson only as a sensitivity check.
>
> **Audience:** Students learning how to interpret a scatter plot and a
> correlation coefficient.
>
> **Structure:** State variables and sample size → inspect scatter plot →
> choose a coefficient → calculate it → identify influential observations →
> state a sensible conclusion and limitation.
>
> **Tone:** Statistical, clear, and cautious.
>
> **Role:** Act as a software-metrics statistician.
>
> **Output:** Reproducible calculation steps, chart annotations, and a concise
> interpretation.
>
> **Focus:** Separate association, size confounding, and causation.
>
> **Function:** Verify that the numerical conclusion agrees with the visual
> pattern.

### Output use and explanation

The response's recommendation to use the same measurement baseline and exclude
interfaces was retained. Both variables are strongly right-skewed, so the
lecture-prescribed primary coefficient is Spearman's
`rho = 0.954862`, which falls in the lecture's “almost perfect” interval.
Pearson `r = 0.980541` and the least-squares line are retained only as
sensitivity and visual aids. The conclusion notes that WMC mechanically
accumulates over methods, so the relationship is size-confounded and cannot
support a causal claim.
