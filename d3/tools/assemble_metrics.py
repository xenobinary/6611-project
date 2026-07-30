#!/usr/bin/env python3
"""Merge UCC and AST results into the reproducible D3 measurement tables."""

from __future__ import annotations

import argparse
import csv
import json
import math
from pathlib import Path


def read_ucc_sloc(path: Path) -> dict[str, dict[str, int]]:
    result: dict[str, dict[str, int]] = {}
    with path.open(newline="", encoding="utf-8") as handle:
        for row in csv.reader(handle):
            if len(row) > 10 and row[0].isdigit() and row[9] == "CODE":
                name = Path(row[10]).stem
                result[name] = {
                    "total_lines": int(row[0]),
                    "blank_lines": int(row[1]),
                    "comment_lines": int(row[2]),
                    "logical_sloc": int(row[7]),
                    "physical_sloc": int(row[8]),
                }
    return result


def read_ucc_complexity(
    path: Path,
) -> tuple[dict[str, int], list[dict[str, str | int]]]:
    rows = list(csv.reader(path.open(newline="", encoding="utf-8")))
    headers = [i for i, row in enumerate(rows) if row and row[0] == "CC1"]
    if not headers:
        raise ValueError(f"Could not find function complexity section in {path}")

    file_wmc: dict[str, int] = {}
    for row in rows:
        if (
            len(row) > 5
            and row[0].isdigit()
            and row[-1].strip().endswith(".java")
        ):
            file_wmc.setdefault(Path(row[-1]).stem, int(row[0]))

    methods: list[dict[str, str | int]] = []
    for row in rows[headers[-1] + 1 :]:
        if len(row) > 5 and row[0].isdigit() and row[-1].strip().endswith(".java"):
            methods.append(
                {
                    "cc": int(row[0]),
                    "function": ",".join(row[4:-1]).strip(),
                    "type": Path(row[-1]).stem,
                }
            )
    return file_wmc, methods


def read_oo(path: Path) -> list[dict[str, str]]:
    with path.open(newline="", encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def pearson(xs: list[float], ys: list[float]) -> float:
    mean_x = sum(xs) / len(xs)
    mean_y = sum(ys) / len(ys)
    numerator = sum((x - mean_x) * (y - mean_y) for x, y in zip(xs, ys))
    denominator = math.sqrt(
        sum((x - mean_x) ** 2 for x in xs)
        * sum((y - mean_y) ** 2 for y in ys)
    )
    return numerator / denominator


def average_ranks(values: list[float]) -> list[float]:
    """Return 1-based average ranks, including the lecture-prescribed tie handling."""
    order = sorted(range(len(values)), key=lambda index: values[index])
    ranks = [0.0] * len(values)
    start = 0
    while start < len(order):
        end = start
        while (
            end + 1 < len(order)
            and values[order[end + 1]] == values[order[start]]
        ):
            end += 1
        average_rank = (start + end + 2) / 2
        for position in range(start, end + 1):
            ranks[order[position]] = average_rank
        start = end + 1
    return ranks


def skewness(values: list[float]) -> float:
    """Population-moment skewness, used only to describe distribution shape."""
    mean = sum(values) / len(values)
    second = sum((value - mean) ** 2 for value in values) / len(values)
    third = sum((value - mean) ** 3 for value in values) / len(values)
    return third / (second**1.5)


def regression(xs: list[float], ys: list[float]) -> tuple[float, float]:
    mean_x = sum(xs) / len(xs)
    mean_y = sum(ys) / len(ys)
    slope = sum((x - mean_x) * (y - mean_y) for x, y in zip(xs, ys)) / sum(
        (x - mean_x) ** 2 for x in xs
    )
    return slope, mean_y - slope * mean_x


def wmc_band(value: int) -> str:
    return "threshold_exceeded" if value > 100 else "within_threshold"


def cf_band(value: float) -> str:
    return "lower_is_better"


def lcom_band(value: str) -> str:
    if value == "N/A":
        return "not_applicable"
    number = float(value)
    return "threshold_exceeded" if number >= 0.80 else "within_threshold"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sloc", type=Path, required=True)
    parser.add_argument("--complexity", type=Path, required=True)
    parser.add_argument("--oo", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, required=True)
    args = parser.parse_args()

    sloc = read_ucc_sloc(args.sloc)
    wmc, methods = read_ucc_complexity(args.complexity)
    oo_rows = read_oo(args.oo)
    args.output_dir.mkdir(parents=True, exist_ok=True)

    combined: list[dict[str, str | int | float]] = []
    for row in oo_rows:
        simple_name = row["type"].split(".")[-1]
        if simple_name not in sloc:
            raise KeyError(f"No UCC SLOC row for {simple_name}")
        wmc_value = wmc.get(simple_name, 0)
        cf_value = float(row["cf"])
        combined.append(
            {
                "type": simple_name,
                "kind": row["kind"],
                **sloc[simple_name],
                "wmc": wmc_value,
                "wmc_band": wmc_band(wmc_value),
                "instance_fields": int(row["instance_fields"]),
                "instance_methods": int(row["instance_methods"]),
                "lcom_star": row["lcom_star"],
                "lcom_band": lcom_band(row["lcom_star"]),
                "coupling_count": int(row["coupling_count"]),
                "cf": cf_value,
                "cf_band": cf_band(cf_value),
                "coupled_types": row["coupled_types"],
            }
        )

    combined.sort(key=lambda row: str(row["type"]))
    combined_path = args.output_dir / "class_metrics.csv"
    with combined_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(combined[0]))
        writer.writeheader()
        writer.writerows(combined)

    class_rows = [row for row in combined if row["kind"] == "class"]
    xs = [float(row["logical_sloc"]) for row in class_rows]
    ys = [float(row["wmc"]) for row in class_rows]
    pearson_correlation = pearson(xs, ys)
    x_ranks = average_ranks(xs)
    y_ranks = average_ranks(ys)
    spearman_correlation = pearson(x_ranks, y_ranks)
    slope, intercept = regression(xs, ys)
    for row, x_rank, y_rank in zip(class_rows, x_ranks, y_ranks):
        row["logical_sloc_rank"] = x_rank
        row["wmc_rank"] = y_rank
        fitted = slope * float(row["logical_sloc"]) + intercept
        row["fitted_wmc"] = fitted
        row["residual"] = float(row["wmc"]) - fitted

    correlation_path = args.output_dir / "correlation_data.csv"
    with correlation_path.open("w", newline="", encoding="utf-8") as handle:
        fields = [
            "type",
            "logical_sloc",
            "wmc",
            "logical_sloc_rank",
            "wmc_rank",
            "fitted_wmc",
            "residual",
        ]
        writer = csv.DictWriter(handle, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(class_rows)

    max_method = max(methods, key=lambda row: int(row["cc"]))
    method_bands = {
        "low_1_to_10": sum(int(row["cc"]) <= 10 for row in methods),
        "moderate_11_to_20": sum(11 <= int(row["cc"]) <= 20 for row in methods),
        "high_21_to_50": sum(21 <= int(row["cc"]) <= 50 for row in methods),
        "very_high_over_50": sum(int(row["cc"]) > 50 for row in methods),
    }
    directed_couplings = sum(int(row["coupling_count"]) for row in combined)
    type_count = len(combined)
    system_cf = directed_couplings / (type_count * (type_count - 1))

    summary = {
        "measurement_baseline": {
            "commit": "e616e598ece800477163988abdfc4f3ab0757cb4",
            "date_measured": "2026-07-24",
            "java_files": len(combined),
            "classes": len(class_rows),
            "interfaces": len(combined) - len(class_rows),
        },
        "cyclomatic_complexity": {
            "total_wmc_all_classes": sum(int(row["wmc"]) for row in class_rows),
            "method_count": len(methods),
            "mean_per_method": sum(int(row["cc"]) for row in methods) / len(methods),
            "maximum_method": max_method,
            "method_bands": method_bands,
        },
        "oo_metrics": {
            "system_cf": system_cf,
            "directed_couplings": directed_couplings,
            "wmc_over_100_classes": [
                row["type"]
                for row in class_rows
                if row["wmc_band"] == "threshold_exceeded"
            ],
            "cf_highest_types": [
                row["type"]
                for row in sorted(
                    combined, key=lambda item: float(item["cf"]), reverse=True
                )[:3]
            ],
            "lcom_at_or_above_0_8_classes": [
                row["type"]
                for row in class_rows
                if row["lcom_band"] == "threshold_exceeded"
            ],
        },
        "correlation": {
            "n_classes": len(class_rows),
            "primary_coefficient": "Spearman rank correlation",
            "spearman_rho": spearman_correlation,
            "pearson_r_sensitivity": pearson_correlation,
            "pearson_r_squared_sensitivity": pearson_correlation**2,
            "logical_sloc_skewness": skewness(xs),
            "wmc_skewness": skewness(ys),
            "selection_rationale": (
                "Both variables are strongly right-skewed; the lecture recommends "
                "Spearman for non-normally distributed attribute values."
            ),
            "slope": slope,
            "intercept": intercept,
            "largest_positive_residuals": [
                row["type"]
                for row in sorted(
                    class_rows, key=lambda item: float(item["residual"]), reverse=True
                )[:3]
            ],
            "largest_negative_residuals": [
                row["type"]
                for row in sorted(
                    class_rows, key=lambda item: float(item["residual"])
                )[:3]
            ],
        },
        "interpretation_bands": {
            "cyclomatic_per_method": (
                "1-10 low; 11-20 moderate; 21-50 high; >50 very high"
            ),
            "wmc_lecture_threshold": (
                "WMC > 100 indicates Broken Modularization; lower is better"
            ),
            "cf_lecture_guidance": (
                "Lower is better; the lecture provides no universal numeric threshold"
            ),
            "lcom_star_lecture_threshold": (
                "LCOM* >= 0.8 indicates Multifaceted Abstraction"
            ),
            "correlation_strength": (
                "abs(coefficient) in [0.9, 1) is labelled almost perfect"
            ),
        },
    }

    (args.output_dir / "metrics_summary.json").write_text(
        json.dumps(summary, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
