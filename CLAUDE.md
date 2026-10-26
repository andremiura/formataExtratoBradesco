# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Standalone Java CLI tools that normalize Bradesco bank statement CSV exports (`;`-separated) and print the result to stdout. There is no build system, package, dependency, or test suite. Each `.java` file in the root is its own program in the default package. Code, comments, and messages are in Portuguese.

## Commands

```bash
javac *.java                                        # compile everything (HashUtils is needed by FormataExtratoBradescoSHA256)
java FormataExtratoBradesco extrato.csv             # run a single tool; output goes to stdout
java geraSQL extrato.csv > extrato.sql              # generate INSERT statements
./bradesco.py -b|-di|-md5|-sha256 extrato.csv       # Python wrapper that dispatches to the compiled classes (needs .class files in cwd)
```

`*.csv`, `*.sql` and `*.class` are gitignored, so no sample data is committed. To test changes, compare output against a real statement export.

## Input format and parsing model

Per the README, the input CSV must have the bank's header removed, and everything from the "total" line down must be removed too. The format has changed before, so check the file by eye before running the tools on it.

All tools share the same copy-pasted parsing loop (`main` → `BufferedReader` → `do/while`):
- A transaction line has fields `data;descricao;codigo;credito[;debito[;saldo]]`, with dates as `dd/mm/yy`.
- A line with exactly **2 fields** continues the previous transaction's description. Its second field is appended to `descricao`, and the loop reads one line ahead to detect this. Only one continuation line per record is handled.
- `debito` defaults to `"0"` when the 5th field is missing.

## Variants (differ only in output)

| Program | Output |
|---|---|
| `FormataExtratoBradesco` | Joined CSV line, values unchanged |
| `FormataExtratoBradescoMD5` | Same, plus an MD5 hash of the concatenated fields as a record ID |
| `FormataExtratoBradescoSHA256` | Strips `.` thousands separators, empty → `0`, appends SHA-256 via `HashUtils.sha256Hex` |
| `FormataExtratoBradescoDataInvertida` | Date printed as `20yy-mm-dd`, plus a hash. Despite the README saying MD5, it uses **SHA-256**, and it hashes the *original* `dd/mm/yy` date |
| `geraSQL` | `INSERT INTO bank_statements (date, historic, code, credit, debit, sha256, created_at, updated_at)` with the ISO date, amounts converted to `1234.56` form, and SHA-256 over the *inverted* date. It has its own `calculateSHA256` (UTF-8) rather than using `HashUtils` |

The hash is the record's deduplication ID. Its input differs between variants (date format, amount normalization, byte encoding), so hashes from different tools are **not** interchangeable. Changing what gets concatenated or how amounts are normalized changes the IDs of records already loaded. `geraSQL` builds SQL by string concatenation with no escaping, so a `'` in a description will break the statement.

When changing parsing logic, apply the change to every variant, because the loop is duplicated in each file.
