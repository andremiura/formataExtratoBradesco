# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Standalone Java CLI tools that normalize Bradesco bank statement CSV exports (`;`-separated) and print the result to stdout. There is no build system, package, dependency, or test suite. Each `.java` file in the root is its own program in the default package. Code, comments, and messages are in Portuguese.

## Commands

```bash
javac *.java                                        # compile everything (ExtratoBradesco and HashUtils are shared helpers)
java FormataExtratoBradesco extrato.csv             # run a single tool; output goes to stdout
java geraSQL extrato.csv > extrato.sql              # generate INSERT statements
./bradesco.py -b|-di|-md5|-sha256 extrato.csv       # Python wrapper that dispatches to the compiled classes (needs .class files in cwd)
```

`*.csv`, `*.sql` and `*.class` are gitignored, so no sample data is committed. To test changes, compare output against a real statement export (all variants should emit the same number of records).

## Input format and parsing model

The input grammar and the rules for header, continuation and Total lines are in `GRAMATICA.md`.

The tools read the CSV exactly as exported by the bank (`Bradesco_*.csv`, ISO-8859-1, CR line endings). Parsing lives in one place, `ExtratoBradesco.le(fileName)`, which returns `{data, descricao, codigo, credito, debito}` per transaction:
- Lines 1 and 2 are checked against a known header pattern (`ExtratoBradesco.LINHA1` / `LINHA2`). On mismatch it prints expected vs. found to stderr and exits 1. This is how a new layout change gets noticed.
- The body runs from line 3 to the line before the first `;Total;`. Everything after it is ignored.
- A line starting with `dd/mm/yy;` starts a transaction. A line starting with `;` continues the previous description (`" " + field 2`), and there can be several. `SALDO ANTERIOR` is skipped. Any other line is an error.
- Field values are passed through as-is (quoted, debits negative). `debito` defaults to `"0"` when the 5th field is missing.

## Variants (differ only in output)

| Program | Output |
|---|---|
| `FormataExtratoBradesco` | Joined CSV line, values unchanged |
| `FormataExtratoBradescoMD5` | Same, plus an MD5 hash of the concatenated fields as a record ID |
| `FormataExtratoBradescoSHA256` | Strips quotes and `.` thousands separators, empty → `0`, appends SHA-256 via `HashUtils.sha256Hex` |
| `FormataExtratoBradescoDataInvertida` | Date printed as `20yy-mm-dd`, plus a hash. Despite the README saying MD5, it uses **SHA-256**, and it hashes the *original* `dd/mm/yy` date |
| `geraSQL` | `INSERT INTO bank_statements (date, historic, code, credit, debit, sha256, created_at, updated_at)` with the ISO date, amounts unquoted and converted to `1234.56` form, and SHA-256 over the *inverted* date. It has its own `calculateSHA256` (UTF-8) rather than using `HashUtils` |

The hash is the record's deduplication ID. Its input differs between variants (date format, amount normalization, byte encoding), so hashes from different tools are **not** interchangeable. Changing what gets concatenated or how amounts are normalized changes the IDs of records already loaded. `geraSQL` builds SQL by string concatenation with no escaping, so a `'` in a description will break the statement.

Parsing changes go in `ExtratoBradesco.java`. Output and hashing are still per-variant.
