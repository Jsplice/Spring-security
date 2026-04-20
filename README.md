# 🛡️ Secure Java Enterprise Boilerplate (DevSecOps Focus)

> Spring Boot 3 · Java 21 · OWASP SCA · SonarQube SAST · Trivy · Gitleaks · Hardened Docker

[![DevSecOps Pipeline](https://github.com/Jsplice/Spring-security/actions/workflows/devsecops-pipeline.yml/badge.svg)](https://github.com/Jsplice/Spring-security/actions/workflows/devsecops-pipeline.yml)

---

## Pipeline Overview

Every push to `main` and every Pull Request triggers a fully automated security pipeline. Results are visible at a glance in three places:

| Where | What you see |
|---|---|
| **Actions tab → Run → Summary** | Markdown table: pass/fail per stage + coverage % |
| **Security tab → Code scanning alerts** | All CVEs and secrets, filterable by tool and severity |
| **Pull Request → Files changed** | Inline annotations on the exact lines with findings |

### Pipeline Stages

```
Push / PR
    │
    ▼
[1] Build & Test ──────────────────── mvn verify + JaCoCo coverage
    │
    ├──▶ [2] SCA — OWASP Dependency-Check   CVEs in Maven deps → SARIF → Security tab
    │
    ├──▶ [3] Container Scan — Trivy         CVEs in Docker image → SARIF → Security tab
    │
    └──▶ [4] Secret Scan — Gitleaks         Hardcoded secrets in git history → SARIF → Security tab
              │
              ▼
         [5] SAST — SonarQube               Code quality gate (runs as service container)
              │
              ▼
         Job Summary (pass/fail table)
```

### Required GitHub Secret

Before the first pipeline run, add the following in **Settings → Secrets and variables → Actions**:

| Secret | Value |
|---|---|
| `SONAR_TOKEN` | *(not needed — pipeline auto-generates an ephemeral token from the service container)* |

No secrets are required for this setup. SonarQube spins up inside the runner and the pipeline creates its own analysis token automatically.

---

## Professional Background & Motivation

Since 2019, my professional work has been conducted almost exclusively within NDA-protected enterprise environments. The nature of that work — spanning financial services, regulated industries, and large-scale distributed systems — means that production codebases, architectural decisions, and security implementations cannot be publicly shared.

This repository exists as a **technical showcase** to demonstrate hands-on expertise in three key areas that define modern enterprise Java engineering:

| Area | What this repo demonstrates |
|---|---|
| **Refactoring & Clean Architecture** | Layered package structure, single-responsibility controllers, externalized configuration |
| **Code Quality** | SonarQube integration, enforced quality gates, structured code conventions |
| **Security Engineering** | Software Composition Analysis (SCA) via OWASP Dependency-Check, hardened container builds, non-root runtime, minimal base images |

This boilerplate represents the *kind* of baseline I establish at the start of any greenfield enterprise project — not a toy app, but an opinionated foundation that treats security as a first-class citizen from day one.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.2 |
| Build tool | Apache Maven 3.9 |
| SCA (dependency scanning) | OWASP Dependency-Check 9.x |
| SAST / Code Quality | SonarQube Community |
| Containerisation | Docker (multi-stage), Docker Compose |
| Runtime image | `eclipse-temurin:21-jre` (minimal footprint) |

---

## Project Structure

```
.
├── src/
│   └── main/
│       ├── java/com/example/secureapi/
│       │   ├── SecureApiApplication.java   # Bootstrap entry point
│       │   └── SecureApiController.java    # REST endpoint /api/v1/status
│       └── resources/
│           └── application.properties
├── Dockerfile                              # Multi-stage, non-root build
├── docker-compose.yml                      # API + SonarQube services
├── owasp-suppressions.xml                  # Documented CVE false-positive suppressions
├── pom.xml                                 # Spring Boot 3 + OWASP plugin
└── README.md
```

---

## Quick Start

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

> Java 21 and Maven are **not required locally** — all build, test, and scan steps run inside Docker containers.

---

### 1. Run the full stack with Docker Compose

```bash
docker-compose up -d
```

This will:
1. Build the application JAR inside a Maven container (Stage 1).
2. Package it into a minimal JRE runtime image (Stage 2).
3. Start the **SonarQube** server at [http://localhost:9000](http://localhost:9000).
4. Start the **Spring Boot API** at [http://localhost:8080](http://localhost:8080) once SonarQube passes its health check.

> **Note:** SonarQube requires `vm.max_map_count >= 262144` on Linux hosts.
> Run: `sudo sysctl -w vm.max_map_count=262144`

Verify the API is live:

```bash
curl http://localhost:8080/api/v1/status
curl http://localhost:8080/actuator/health
```

---

### 2. Software Composition Analysis — OWASP Dependency-Check

Scans all Maven dependencies against the [National Vulnerability Database (NVD)](https://nvd.nist.gov/).
The build **fails automatically** if any dependency has a CVE with a CVSS score ≥ 7.

```bash
mvn dependency-check:check
```

The HTML report is generated at:

```
target/dependency-check-report/dependency-check-report.html
```

To suppress a confirmed false positive, add a documented entry to `owasp-suppressions.xml`.

---

### 3. Static Analysis — SonarQube Scan

Once SonarQube is running locally (via Compose), generate a token and run the scanner.

#### Generate a token

1. Open [http://localhost:9000](http://localhost:9000) and log in (`admin` / your password)
2. Top-right avatar → **My Account** → **Security** tab
3. Under **Generate Tokens**: name it anything, type = **Global Analysis Token**, click **Generate**

#### Run the scan

Because Maven is not required locally, the scan runs inside the same Docker image used to build the app. This keeps the local machine dependency-free.

> **Important for Windows / PowerShell users:** wrap the `mvn` call in `sh -c "..."` to prevent PowerShell from misinterpreting the `-D` property flags.

```bash
docker run --rm \
  --network spring-security_default \
  -v "$(pwd):/build" \
  -w /build \
  maven:3.9-eclipse-temurin-21 \
  sh -c "mvn compile sonar:sonar \
    -Dsonar.projectKey=secure-api \
    -Dsonar.host.url=http://sonarqube:9000 \
    -Dsonar.token=<YOUR_GLOBAL_ANALYSIS_TOKEN> \
    -Dsonar.scm.disabled=true \
    -B"
```

> **Note:** the host URL inside Docker is `http://sonarqube:9000` (the Compose service name), not `localhost:9000`.

Then open [http://localhost:9000/dashboard?id=secure-api](http://localhost:9000/dashboard?id=secure-api) to review:
- Code smells
- Bug detections
- Security Hotspots
- Duplications
- Test coverage (add JaCoCo to `pom.xml` to enable)

---

### 4. Tear down

```bash
docker-compose down -v
```

The `-v` flag removes the named volumes, giving you a clean SonarQube state on the next run.

---

## Security Design Decisions

### Container Hardening

| Practice | Implementation |
|---|---|
| Non-root execution | `useradd --system appuser` + `USER appuser` |
| Minimal attack surface | Runtime stage uses `eclipse-temurin:21-jre`, not a full JDK |
| No build tools in production | Multi-stage build discards Maven, source code, and intermediate layers |
| Explicit file ownership | `chown appuser:appgroup app.jar` before privilege drop |

### Dependency Security

| Practice | Implementation |
|---|---|
| Automated CVE scanning | `dependency-check-maven` plugin bound to the build lifecycle |
| Fail-fast on high severity | `<failBuildOnCVSS>7</failBuildOnCVSS>` |
| Documented suppressions | All false positives recorded in `owasp-suppressions.xml` with reviewer, date, and justification |

---

## License

MIT — feel free to use this as a starting point for your own enterprise projects.
