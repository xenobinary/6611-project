# iBank Delivery 3

This directory completes Problems 7–10 for SOEN 6611 using the latest repository
baseline:

```text
Commit: e616e598ece800477163988abdfc4f3ab0757cb4
Measurement date: 2026-07-24
Production Java types: 25 (23 classes, 2 interfaces)
Tests: 46 / 46 passing
```

## Final presentation

`D3_GROUP-C_40109362_40167688_40300671.pptx`

The deck follows the visual style and layout system of the provided D2
presentation. It contains calculation definitions, result tables, qualitative
conclusions, CASTROFF prompt examples, speaker-note source blocks, and
references.

Complete D3 CASTROFF prompt:
https://github.com/xenobinary/6611-project/blob/master/d3/prompts/delivery3_castroff_prompt.md

The final synthesis revises the D1 GQM goal to match the evidence actually
collected in D2 and D3: readability, logical simplicity, coupling, cohesion,
scope/effort, and the relationship between size and class complexity.

## Problem 7 — Cyclomatic complexity

USC CSSE Unified Code Counter—Java (UCC-J) 2020.01 is the tool used. Its
output identifies the engine as the U.S. Government Edition based on
UCC-G v.1.3.1; CC1 is the counting scheme.

```text
Methods/constructors measured: 239
ΣV(G): 474
Mean V(G) per method: 1.9833
Maximum: 12 (ClientDashboardPanel.onOk)
1–9: 236 methods
=10: 2 methods
=12: 1 method
>20: 0 methods
```

The lecture threshold is applied at the method/module level:

```text
1-10 low band; values of 10 or more are a concern; 11-20 moderate;
21-50 high; >50 very high
```

The project sum of 474 is additive, but is not compared with a per-method
threshold.

## Problem 8 — Object-oriented metrics

### WMC

For class `C`:

```text
WMC(C) = Σ CC1(m), for each declared method/constructor m in C
```

WMC is unnormalized, as required. The two interfaces have WMC 0 and are not
included in class-only summaries.

### Coupling Factor

The analysis uses a per-type adaptation of MOOD CF:

```text
CF(C) = outgoing non-inheritance project-type couplings / (N - 1)
System CF = total directed couplings / (N × (N - 1))
N = 25 top-level project types
```

Java-library types, inheritance, self-coupling, and duplicate references are
excluded.

```text
Directed couplings: 71
System CF: 0.118333
Highest: ClientDashboardPanel = 0.416667
```

### Henderson-Sellers LCOM*

```text
LCOM* = (m - (Σ μ(Aj) / a)) / (m - 1)
```

Where `m` is the number of declared instance methods, `a` is the number of
declared instance fields, and `μ(Aj)` is the number of methods accessing field
`Aj`. Constructors, static methods, inherited members, and abstract methods are
excluded. The result is `N/A` when `m < 2` or `a = 0`.

The qualitative interpretation now follows the course lecture:

```text
WMC: lower is better; WMC > 100 indicates Broken Modularization
CF: lower is better; the lecture gives no universal numeric threshold
LCOM*: LCOM* >= 0.8 indicates Multifaceted Abstraction
```

No iBank class exceeds the WMC threshold: the maximum is 99 for
`ClientDashboardPanel`. System CF is 0.118333; the per-type values are a
transparent adaptation required to satisfy the assignment's “for each class”
wording. Eight classes have LCOM* at or above 0.8.

## Problem 9 — Use Case Points

Transactions are counted as actor-system stimulus-response pairs, rather than
blindly equating each textual step with a transaction. The two included
sub-use cases are excluded to avoid double-counting transactions already
represented in their base use cases.

```text
UAW = 9
UUCW = 125
UUCP = 134
TCF = 0.995
ECF = 0.845
Adjusted UCP = 112.66385
Productivity factor = 20 person-hours/UCP
Estimated effort = 2,253.277 person-hours
```

### Actual-effort caveat

The repository contains no contemporaneous time sheet. To make the required
comparison explicit without pretending Git timestamps are work hours, D3 uses
a **retrospective baseline of 168 person-hours (3 members × 56 hours)**. Replace
this value in `tools/calculate_ucp.py` if the team has a more accurate work log,
then rerun the analysis and update slides 16 and 19.

At the current baseline, the UCP estimate is 13.41 times actual effort. This
indicates that the default 20 h/UCP productivity factor is not calibrated for a
small student prototype that reuses Swing, SQLite, MVC scaffolding, and shared
components.

## Problem 10 — Logical SLOC and WMC

The correlation uses the 23 top-level classes. Both distributions are strongly
right-skewed, so the primary coefficient is Spearman's rank correlation,
following the lecture's recommendation for non-normal data:

```text
Spearman rho = 0.954862
Pearson r (sensitivity check) = 0.980541
Pearson R² (sensitivity check) = 0.961461
WMC = 0.230596 × Logical SLOC + 1.388999
```

The lecture labels a coefficient in `[0.9, 1)` “almost perfect.” This is an
almost-perfect positive monotonic association, not a causal result: WMC
accumulates method complexity, so class size is a confounding structural factor.

Logical SLOC follows the lecture/UCC convention: it is the total number of
source statements. Counting details are kept explicit because logical SLOC can
otherwise be ambiguous.

## Lecture alignment

The final slides and speaker-note citations use the supplied course material:

- Pankaj Kamthan (2026a), `source_code_control_flow_structure.pdf`,
  especially pp. 13-20
- Pankaj Kamthan (2026b), `effort_estimation_use_cases.pdf`,
  especially pp. 5-17
- Pankaj Kamthan (2026c), `software_measurement_data_analysis.pdf`,
  especially pp. 42 and 46-53
- Pankaj Kamthan (2026d), `source_code_length.pdf`, especially pp. 16-20
- Pankaj Kamthan (2026e), `ood_metrics_classes.pdf`,
  especially pp. 11-16, 32, and 36-42

Visible content-slide citations use the author-year form requested by the team,
for example `(Kamthan, 2026a)`. The year suffixes are assigned alphabetically
by lecture title across the five Kamthan sources cited in D3.

The final deck also makes the measurement levels explicit: Problem 7 reports
method-level CC1, while Problem 8 uses those CC1 values as the unnormalized
method weights summed into class-level WMC. Project-specific qualitative
conclusions explain the main UI hotspots, recommended refactorings, UCP weight
rationale, and retrospective actual-effort formula.

Speaker notes on the main result slides include concise presenter talking
points: measurement level, calculation, project-specific meaning, recommended
action, and common clarification questions. Each slide still retains its
separate `[Sources]` block.

## Reproducing the results

Requirements:

- JDK 17 or later
- Python 3

On macOS/Linux:

```bash
chmod +x d3/run_analysis.sh
./d3/run_analysis.sh
```

The script reruns UCC-J 2020.01 (based on UCC-G v.1.3.1), compiles the Java AST
analyzer, regenerates all CSV/JSON results, calculates UCP and correlation
outputs, and runs all JUnit tests.

## Output files

```text
d3/
├── D3_GROUP-C_40109362_40167688_40300671.pptx
├── README.md
├── run_analysis.sh
├── eval/
│   ├── actual_effort_reconstruction.csv
│   ├── class_metrics.csv
│   ├── correlation_data.csv
│   ├── metrics_summary.json
│   ├── oo_raw.csv
│   ├── ucc_cyclomatic_complexity.csv
│   ├── ucc_sloc.csv
│   ├── ucp_actors.csv
│   ├── ucp_environmental_factors.csv
│   ├── ucp_summary.json
│   ├── ucp_technical_factors.csv
│   └── ucp_use_cases.csv
├── prompts/
│   └── delivery3_castroff_prompt.md
└── tools/
    ├── D3MetricsAnalyzer.java
    ├── assemble_metrics.py
    └── calculate_ucp.py
```
