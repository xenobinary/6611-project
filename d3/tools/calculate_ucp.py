#!/usr/bin/env python3
"""Reproduce the iBank Use Case Points and effort comparison for D3."""

from __future__ import annotations

import csv
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "eval"

ACTORS = [
    {
        "actor": "Bank Client",
        "interface": "Java Swing GUI",
        "complexity": "complex",
        "weight": 3,
    },
    {
        "actor": "System Administrator",
        "interface": "Java Swing GUI",
        "complexity": "complex",
        "weight": 3,
    },
    {
        "actor": "Technician",
        "interface": "Java Swing GUI",
        "complexity": "complex",
        "weight": 3,
    },
]

# Transactions are stimulus-response pairs in the numbered main-success
# scenarios in d2/software_design.md, following the lecture definition.
# Included sub-behaviors UC-S1 and UC-S2 are excluded to avoid double-counting
# transactions already present in their base use cases.
USE_CASES = [
    ("UC-01", "Authenticate User", 7, "average", 10),
    ("UC-02", "Withdraw Cash", 10, "complex", 15),
    ("UC-03", "Deposit Funds", 10, "complex", 15),
    ("UC-04", "Transfer Funds", 12, "complex", 15),
    ("UC-05", "Check Balance", 2, "simple", 5),
    ("UC-06", "View Transaction History", 4, "average", 10),
    ("UC-07", "End Session", 5, "average", 10),
    ("UC-08", "Update Exchange Rate", 8, "complex", 15),
    ("UC-09", "Unlock User", 7, "average", 10),
    ("UC-10", "Refill Cash Box", 4, "average", 10),
    ("UC-11", "Switch Language", 7, "average", 10),
]

TECHNICAL = [
    ("T1", "Distributed system", 2.0, 0, "Standalone desktop application"),
    ("T2", "Response-time objectives", 1.0, 3, "Interactive kiosk feedback"),
    ("T3", "End-user efficiency", 1.0, 5, "Core usability objective"),
    ("T4", "Complex internal processing", 1.0, 4, "Rules, limits, conversion, DB"),
    ("T5", "Reusable code", 1.0, 4, "MVC, base panel, numpad, i18n"),
    ("T6", "Easy to install", 0.5, 4, "Bundled scripts and local SQLite"),
    ("T7", "Easy to use", 0.5, 5, "ABM usability is central"),
    ("T8", "Portable", 2.0, 4, "Java plus three OS launch scripts"),
    ("T9", "Easy to change", 1.0, 4, "MVC and externalized messages"),
    ("T10", "Concurrent", 1.0, 0, "Single-user desktop process"),
    ("T11", "Security", 1.0, 5, "PIN hashing, lockout, role routing"),
    ("T12", "Third-party access", 1.0, 1, "Embedded JDBC only; no live network"),
    ("T13", "Training needs", 1.0, 1, "Familiar ABM interaction model"),
]

ENVIRONMENTAL = [
    ("E1", "Familiarity with use case domain", 1.5, 3, "Moderate use-case familiarity"),
    ("E2", "Part-time workers", -1.0, 5, "All three members are students"),
    ("E3", "Analyst capability", 0.5, 4, "Clear roles and design ownership"),
    ("E4", "Application experience", 0.5, 2, "Limited banking-domain experience"),
    ("E5", "Object-oriented experience", 1.0, 4, "Strong Java/OOP experience"),
    ("E6", "Motivation", 1.0, 5, "Course deliverable with shared ownership"),
    ("E7", "Difficult programming language", -1.0, 1, "Java 17 is familiar"),
    ("E8", "Stable requirements", 2.0, 4, "Course specification mostly stable"),
]

# No contemporaneous time sheet exists in the repository. This is a transparent
# retrospective baseline (3 members x 56 hours) that the team should replace if
# a more accurate work log is available.
ACTUAL_EFFORT = [
    ("Implementation and database", 78),
    ("Testing and cross-platform scripts", 30),
    ("Design and measurement artifacts", 30),
    ("Slides, review, and rehearsal", 30),
]


def write_csv(name: str, rows: list[dict[str, object]]) -> None:
    path = OUTPUT / name
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)

    actor_rows = [
        {**actor, "weighted_points": actor["weight"]} for actor in ACTORS
    ]
    use_case_rows = [
        {
            "id": identifier,
            "use_case": name,
            "transactions": transactions,
            "complexity": complexity,
            "weight": weight,
            "weighted_points": weight,
        }
        for identifier, name, transactions, complexity, weight in USE_CASES
    ]
    technical_rows = [
        {
            "id": identifier,
            "factor": factor,
            "weight": weight,
            "rating": rating,
            "weighted_score": weight * rating,
            "rationale": rationale,
        }
        for identifier, factor, weight, rating, rationale in TECHNICAL
    ]
    environmental_rows = [
        {
            "id": identifier,
            "factor": factor,
            "weight": weight,
            "rating": rating,
            "weighted_score": weight * rating,
            "rationale": rationale,
        }
        for identifier, factor, weight, rating, rationale in ENVIRONMENTAL
    ]
    actual_rows = [
        {"activity": activity, "person_hours": hours}
        for activity, hours in ACTUAL_EFFORT
    ]

    write_csv("ucp_actors.csv", actor_rows)
    write_csv("ucp_use_cases.csv", use_case_rows)
    write_csv("ucp_technical_factors.csv", technical_rows)
    write_csv("ucp_environmental_factors.csv", environmental_rows)
    write_csv("actual_effort_reconstruction.csv", actual_rows)

    uaw = sum(int(row["weighted_points"]) for row in actor_rows)
    uucw = sum(int(row["weighted_points"]) for row in use_case_rows)
    uucp = uaw + uucw
    tfactor = sum(float(row["weighted_score"]) for row in technical_rows)
    efactor = sum(float(row["weighted_score"]) for row in environmental_rows)
    tcf = 0.6 + 0.01 * tfactor
    ecf = 1.4 - 0.03 * efactor
    ucp = uucp * tcf * ecf
    productivity_factor = 20
    estimated_effort = ucp * productivity_factor
    actual_effort = sum(hours for _, hours in ACTUAL_EFFORT)

    summary = {
        "uaw": uaw,
        "uucw": uucw,
        "uucp": uucp,
        "technical_factor_sum": tfactor,
        "tcf": tcf,
        "environmental_factor_sum": efactor,
        "ecf": ecf,
        "adjusted_ucp": ucp,
        "productivity_factor_hours_per_ucp": productivity_factor,
        "estimated_person_hours": estimated_effort,
        "actual_person_hours_reconstructed": actual_effort,
        "estimate_minus_actual_hours": estimated_effort - actual_effort,
        "estimate_to_actual_ratio": estimated_effort / actual_effort,
        "actual_effort_caveat": (
            "Retrospective baseline only: 3 members x 56 hours. "
            "Replace with a team time sheet if available."
        ),
        "interpretation": (
            "The uncalibrated Karner productivity factor materially "
            "overestimates this small, reusable student prototype. UCP is "
            "dependable as a comparative size model, not an exact prediction."
        ),
    }
    (OUTPUT / "ucp_summary.json").write_text(
        json.dumps(summary, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
