# Agent Instructions

## Release Versioning

Apply these rules when preparing a commit for the deployment branch or when a
release is explicitly requested.

- Treat production behavior changes, API changes, database migrations, runtime configuration changes, dependency upgrades, security fixes, and production bug fixes as release-worthy.
- Do not bump a version for documentation-only changes, tests-only changes, comments, or internal refactors that do not change released behavior.
- Bump the version in the same commit as the release-worthy change. Do not create a separate version-only commit.
- Use semantic versioning: increment the patch version by default; use a minor version for a backward-compatible public feature and a major version only for a breaking change.
- For backend changes, update the `project.version` value in `pom.xml`.
- Do not change the native-image profile or build a native Docker image unless explicitly requested. The deployment workflow uses `Dockerfile` and the regular JAR build.
- Run `./mvnw test` after changing the backend.

## Frontend Coordination

- If a release-worthy change also modifies the frontend in `../todoApp`, bump its `package.json` version in the same release commit.
- If only the backend changes, bump only the backend version. If only the frontend changes, bump only the frontend version.
- If both projects change, bump both versions independently. They do not need to share the same numeric version.
- The frontend workflow reads only `package.json` for its release version. Do not update the legacy `package-lock.json` unless an explicit npm migration is requested; this project uses pnpm.

## Verification

- Run the relevant tests and builds before considering a release commit complete.
- For changes spanning both projects, run `./mvnw test` in this repository and `pnpm test` plus `pnpm build` in `../todoApp`.
- Do not commit generated directories or local artifacts such as `target/`, `dist/`, `effective-pom.xml`, or local environment files.
