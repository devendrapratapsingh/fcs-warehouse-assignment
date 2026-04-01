---
applyTo: "**/*.java,**/pom.xml,**/*.yml"
---

# CI, Security & Quality Gates

This project enforces security and quality on every push / PR through two
automated gates defined in `.github/workflows/ci.yml`.

---

## 1. Vulnerability Scanning — OWASP Dependency-Check

**Plugin:** `org.owasp:dependency-check-maven` (declared in `pom.xml`)  
**Phase:** `verify` (runs after `test`)  
**Reports:** `target/dependency-check/dependency-check-report.html` and `.json`

### How it works
- Downloads the OWASP NVD (National Vulnerability Database) and cross-references
  every resolved Maven dependency against known CVEs.
- **Build fails** when any dependency has a CVSS score **≥ 7 (HIGH or CRITICAL)**.
- The threshold is controlled by `<failBuildOnCVSS>` in the plugin config in `pom.xml`.

### Run locally
```bash
./mvnw dependency-check:check
# report opens at: target/dependency-check/dependency-check-report.html
```

### Suppressing a false positive
Add an entry to `owasp-suppressions.xml`:
```xml
<suppress>
  <notes>Explain the reason — link to issue tracker entry.</notes>
  <gav regex="true">^groupId:artifactId:.*$</gav>
  <cve>CVE-YYYY-NNNNN</cve>
</suppress>
```
**Never suppress a real vulnerability without a linked ticket and an expiry plan.**

---

## 2. Coverage — JaCoCo

Coverage is generated during `mvn verify`.  
XML report: `target/site/jacoco/jacoco.xml`  
HTML report: `target/site/jacoco/index.html`

```bash
./mvnw verify
open target/site/jacoco/index.html
```

---

## CI Pipeline (`.github/workflows/ci.yml`)

```
push / PR
    │
    ▼
[build-and-test]     ─── ./mvnw verify  (compile + test + JaCoCo)
    │
    └──► [vulnerability-scan]  ─── dependency-check:check  (CVSS ≥ 7 → fail)
```

- `vulnerability-scan` runs after `build-and-test` succeeds.
- Reports are uploaded as GitHub Actions artefacts (14-day retention).
- `vulnerability-scan` uploads the HTML/JSON report even when the build fails,
  so the team can review findings without having to re-run locally.

### Optional secret
| Secret | Purpose |
|--------|---------|
| `NVD_API_KEY` | Eliminates NVD rate-limiting in OWASP scan — add via GitHub → Settings → Secrets → Actions |

---

## Coding rules for security

When generating new code, always:

1. **Validate all input at the REST layer** — use `@NotNull`, `@Size`, `@Pattern`
   (Bean Validation) before the value reaches any use case.
2. **Never concatenate SQL** — all queries must use JPA/Panache named parameters.
3. **Never log sensitive values** — passwords, tokens, PII must not appear in logs.
4. **Keep dependencies up-to-date** — prefer the version managed by the Quarkus BOM;
   only override a version to address a CVE (document it with a comment in `pom.xml`).
