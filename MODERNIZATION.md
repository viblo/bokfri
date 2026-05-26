# Bokfri Modernization Backlog

This file tracks only modernization work that is still incomplete.

Completed work belongs in `CHANGELOG.md` and git history, not here.

## Current Snapshot

| Area | Current state |
|------|---------------|
| Java target | 21 |
| Tests | JUnit 5 + integration tests in place |
| Logging | SLF4J + Logback in place |
| Build tooling | Checkstyle, SpotBugs, JaCoCo, and CI are configured |
| Date/time migration | `Calendar` and `SimpleDateFormat` are gone; legacy `Date` boundaries remain |
| Persistence | Still based on Java serialization stored in HSQLDB `OBJECT` columns |

## Remaining Work

### 1. Finish Date/Time Modernization

Status: partially complete

Current repo state:
- `java.util.Calendar` usage has been eliminated from production code
- `SimpleDateFormat` usage has been eliminated from production code
- production `new Date()` runtime calls have been eliminated
- core date math/month helpers and focused voucher/invoice tests now prefer `LocalDate`
- more payment, credit-note, and periodic-invoice calculations now compare `LocalDate` values directly
- more revenue and report-period printers now use `LocalDate` month boundaries and comparisons directly
- more stock and inventory-related period filters now compare `LocalDate` values directly
- more accounting-year and report-setup flows now keep year boundaries as `LocalDate` until legacy UI/report APIs require `Date`
- more payment, inventory, and periodic-invoice panels now read and write `LocalDate` values directly from date choosers where supported
- more company and domain aggregate helpers now evaluate monthly membership against `LocalDate` values directly
- more product pricing, inpayment lookup, and main-book calculations now compare `LocalDate` values directly and bridge back to `Date` only for legacy return types
- more periodic-invoice generation and pending-invoice flows now keep schedule boundaries and next-invoice calculations as `LocalDate` values internally
- more invoice due-date table and sales print flows now read `LocalDate`/`LocalDate` due dates directly and only convert to `Date` at Jasper/table boundaries
- more list, journal, and debt printers now format `LocalDate` values directly and only bridge to `Date` for `DateFormat`-based rendering
- more import flows now parse incoming dates into `LocalDate` values before handing them to bookkeeping models and vouchers
- more in- and out-delivery flows now expose `LocalDate` directly in domain objects and keep table, panel, math, and list-printer paths on local dates until display boundaries
- more order, tender, purchase-order, and inventory report/import paths now use `LocalDate` accessors directly and bridge to `Date` only at XML or Jasper boundaries
- more payment journal, reminder, main-book, and transaction-cleanup flows now read local dates directly and only bridge to `Date` where report rendering still requires it
- more supplier-payment export flows now consume `LocalDate` values directly from payment models and only bridge back to `Date` when persisting config values
- more supplier-payment file-export posts now take `LocalDate` values from application models and only bridge to `Date` at the LB transfer file boundary
- more Excel export helpers now accept `LocalDate` values directly so voucher exports no longer detour through deprecated `Date` accessors before formatting
- more app dialogs now expose `LocalDate` values directly where callers immediately convert legacy `Date` values back to local dates for business logic
- more report dialogs now expose `LocalDate` directly for single-date flows and read local date ranges directly from chooser widgets in list dialogs
- legacy `Date` imports and bridge methods still remain at Swing, JasperReports, import/export, and persistence boundaries

Remaining tasks:
- Replace remaining `Date`-based APIs with `java.time` types where practical
- Remove or isolate deprecated bridge methods that still exist only for legacy callers
- Re-evaluate whether GUI date widgets can move fully to `java.time` without `Date` adapters

Done when:
- production code no longer depends on `Calendar`
- legacy `Date` usage is either removed or clearly constrained to unavoidable framework boundaries

### 2. Replace Serialization-Based Persistence

Status: not started

Current repo state:
- `46` production classes still implement `Serializable`
- storage is still built around serialized objects in HSQLDB
- HSQLDB is still `1.8.0.10`

Remaining tasks:
- Decide the target persistence strategy
  - normalized SQL schema
  - JSON/text document storage
  - another transitional approach on newer HSQLDB
- Design a migration path from existing user databases
- Build a migration tool that can read old serialized-object data and write the new format
- Incrementally remove `Serializable` from domain and backup models once the storage layer no longer depends on it
- Keep backup/restore working across the migration

Done when:
- new persistence format is implemented
- existing user data can be migrated safely
- domain model evolution no longer depends on Java serialization compatibility

### 3. Replace or Remove Obsolete Dependencies

Status: not started

Current repo state:
- `javax.mail` still present in `pom.xml` and referenced from mail/report code
- `jxl` still powers Excel import/export code
- IntelliJ GUI Designer runtime/plugin is still required
- `javax.help:javahelp` is still present and actively referenced
- Spring dependencies remain in `pom.xml`, but there are `0` `org.springframework` references in `src/main/java`
- there are `111` IntelliJ `.form` files under `src/main`

Remaining tasks:
- Replace `javax.mail` with `jakarta.mail`
- Replace `jxl` with Apache POI or another maintained Excel library
- Decide whether to keep or eliminate IntelliJ GUI Designer as a build dependency
- Replace or remove JavaHelp
- Remove unused Spring dependencies after confirming nothing depends on them indirectly

Done when:
- obsolete libraries are removed from `pom.xml`
- code paths using them have been migrated and verified

### 4. Add a Headless Developer/Test CLI

Status: not started

Current repo state:
- Bokfri is primarily a Swing application
- many important workflows are only easy to exercise through the GUI
- report and print bugs are difficult to reproduce in CI because preview/print flows assume UI entry points
- existing code has reusable lower-level pieces (`SSDB`, report printers, backup helpers), but they are not exposed through a stable command-line entry point

Goal:
- add a small CLI for automation, diagnostics, and agent/developer testing without creating a second user-facing product
- keep the Swing app as the primary application
- use the CLI to make database, backup, and report behavior reproducible in CI and local debugging

Suggested CLI shape:
- add a new entry point such as `org.fribok.bookkeeping.cli.BokfriCli`
- keep commands thin and call existing services/printers directly
- avoid creating Swing frames/dialogs from CLI commands
- support Maven/fat-jar execution first; packaged launchers can come later

Initial command candidates:
- `version` — print app version/build metadata
- `paths` — print resolved app/user/config/data paths
- `db status` — open a configured database and report basic health/counts
- `company list` and `year list` — inspect available company/year IDs for scripting
- `invoice list --company-id ... --year-id ...` — inspect invoice IDs and status
- `invoice print --company-id ... --year-id ... --invoice ... --lang sv --out invoice.pdf` — generate an invoice PDF headlessly
- `invoice sample-pdf --out target/invoice-sample.pdf` — create a deterministic sample invoice PDF for CI smoke tests
- `backup create --out backup.zip` — smoke-test backup creation without navigating the UI

Testing opportunities:
- add CI smoke tests that generate a sample invoice PDF and assert it exists, is non-empty, and contains expected text/metadata where practical
- use CLI commands to reproduce report/printing bugs on Linux, Windows, and macOS runners
- use CLI commands for agent-driven verification before opening PRs

Packaging opportunities:
- eventually ship two launchers from the same codebase:
  - `Bokfri` for the Swing application
  - `bokfri-cli` for command-line diagnostics/automation
- keep official user workflows in Swing unless a CLI command is explicitly promoted to supported user-facing behavior

Done when:
- a CLI entry point exists and can run from Maven or the assembled jar
- at least one headless report/PDF smoke test runs in CI
- common diagnostic commands can inspect version, paths, database state, companies, years, and invoices without opening Swing UI

### 5. Tighten Build and Quality Gates

Status: partially complete

Current repo state:
- Checkstyle is configured, but `failOnViolation` is disabled
- SpotBugs is configured, but `failOnError` is disabled
- JaCoCo reports are generated
- PR CI runs `mvn clean install` on Linux, Windows, and macOS
- Linux CI runs Checkstyle as `continue-on-error`
- CI does not currently enforce SpotBugs or a coverage threshold

Remaining tasks:
- Reduce the existing Checkstyle baseline until violations can fail the build
- Reduce the SpotBugs baseline until issues can fail the build
- Decide whether to enforce a minimum coverage threshold
- Add any missing CI checks needed to make tooling enforcement consistent

Done when:
- style and static analysis checks can block regressions in CI
- coverage policy is explicit and enforced if desired

### 6. Optional API Cleanup After Core Modernization

Status: partially complete

Current repo state:
- `245` `return null` sites remain in production code
- many of the remaining sites are GUI, table-model, print, importer, or framework-boundary methods

Remaining tasks:
- Review remaining `return null` sites and separate intentional framework contracts from avoidable legacy API design
- Introduce `Optional<T>` only where it improves correctness and API clarity
- Avoid forcing `Optional` into Swing/table-model patterns where `null` is part of the expected contract

Done when:
- remaining `null` returns are either removed or intentionally documented by category

## Suggested Order

1. Finish the remaining date/time cleanup
2. Add the first small headless CLI commands (`version`, `paths`, and an invoice sample PDF smoke test) so future modernization work is easier to verify
3. Remove clearly unused dependencies such as Spring, if confirmed safe
4. Tackle library migrations with the smallest blast radius first (`javax.mail`, `jxl`, `javahelp`)
5. Decide the persistence migration strategy before changing storage-related models
6. Tighten CI quality gates after the dependency and persistence work stops moving the baseline

## Out of Scope for This File

These may be worthwhile, but they are broader architecture work rather than backlog items for the current modernization pass:

- breaking up `SSDB.java`
- replacing Swing with another UI framework
- introducing dependency injection across the app
- switching build tools
- adopting JPMS
