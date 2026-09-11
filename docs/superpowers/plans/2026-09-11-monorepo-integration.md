# EasyAccount Monorepo Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate the existing Android app, React web client, and Spring Boot API into one safe, documented repository.

**Architecture:** Preserve the Android Gradle project at `app/`, add independent `web/` and `server/` modules, and keep shared documentation and ignore rules at the repository root. Store no live credentials in tracked files: Android uses a placeholder and the server resolves secrets from environment variables.

**Tech Stack:** Android/Java/Gradle, React 18/TypeScript/Vite, Spring Boot 3.2/Maven/MySQL.

**Spec:** `docs/superpowers/specs/2026-09-11-monorepo-integration-design.md`

## Global Constraints

- Preserve all Android files and Git history; do not force-push or rewrite history.
- Add the web client under `web/` and API under `server/`.
- Do not commit keys, passwords, environment files, dependencies, build artifacts, or IDE metadata.
- Retain the `/api` web base path and server port `8080`.

---

### Task 1: Safe Configuration Boundary

**Files:**
- Modify: `.gitignore`, `server/src/main/resources/application.yml`
- Create: `server/src/main/resources/application.example.yml`

**Interfaces:**
- Consumes: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Produces: Spring configuration using environment variables with documented non-secret defaults.

- [ ] **Step 1: Check current exclusions**

Run: `git check-ignore -v web/node_modules server/target app/local.properties`

Expected: Android exclusions are reported; web and server paths are not yet covered.

- [ ] **Step 2: Add root ignore rules and server configuration**

Ignore web dependencies/output, Maven output, IDE data, `.env` files, Android local properties, and private-key containers. Use `${DB_URL:jdbc:mysql://localhost:3306/easy_account?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Shanghai}`, `${DB_USERNAME:root}`, and `${DB_PASSWORD}` in `application.yml`; add only placeholders to the example file.

- [ ] **Step 3: Verify the boundary**

Run: `git check-ignore -v web/node_modules server/target app/local.properties .env`

Expected: Every local/generated path is ignored.

### Task 2: Add Web And Server Modules

**Files:**
- Create: `web/**` from `D:\account_web`, excluding `node_modules`
- Create: `server/**` from `D:\account_server`, excluding `target` and `.idea`

**Interfaces:**
- Consumes: `/api` requests and server port `8080`.
- Produces: independently buildable web and API modules.

- [ ] **Step 1: Copy source and build-definition files only**

Copy web source, `package.json`, lockfile, Vite/TypeScript configuration, and HTML entry point. Copy server source and `pom.xml`. Exclude generated/IDE directories.

- [ ] **Step 2: Verify new paths**

Run: `git status --short`

Expected: New paths are only under `web/` and `server/`.

- [ ] **Step 3: Build the web client**

Run: `npm run build` from `web/`.

Expected: TypeScript and Vite complete successfully.

- [ ] **Step 4: Test and package the server**

Run: `mvn test package` from `server/`.

Expected: Maven tests and packaging complete successfully.

### Task 3: Android Credential Policy

**Files:**
- Modify if needed: `app/src/main/java/com/easyaccount/app/ai/AiConfig.java`

**Interfaces:**
- Produces: `AiConfig.API_KEY` equal to `YOUR_ZHIPU_API_KEY_HERE`.

- [ ] **Step 1: Run a source-policy check**

Run: `rg -n 'API_KEY = "(?!YOUR_ZHIPU_API_KEY_HERE")' app/src/main/java/com/easyaccount/app/ai/AiConfig.java`

Expected: No non-placeholder key assignment.

- [ ] **Step 2: Replace a live key only if the check reports one**

Use `YOUR_ZHIPU_API_KEY_HERE` while retaining the model and endpoint constants.

- [ ] **Step 3: Re-run the source-policy check**

Expected: No matches.

### Task 4: README And Final Validation

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: module locations and configuration variables above.
- Produces: accurate setup documentation for all three modules.

- [ ] **Step 1: Document the modules, prerequisites, configuration, and startup**

List Java 17, Maven, Node.js/npm, MySQL, and Android Studio as applicable. Explain secure variable-based server configuration, startup commands, `/api`, port `8080`, and Android AI-key safety.

- [ ] **Step 2: Scan staged content**

Run: `git diff --cached | rg -n -i '(api[_-]?key|password|secret|token)'`

Expected: Only placeholders, environment-variable references, or safety documentation appear.

- [ ] **Step 3: Commit and ordinary-push**

Run: `git commit` followed by `git push origin master`.

Expected: The remote advances without force options.
