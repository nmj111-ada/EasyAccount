# EasyAccount Monorepo Integration Design

## Goal

Keep the existing Android client and add the related web client and Spring Boot API to the same repository without committing credentials, dependencies, build outputs, or local IDE state.

## Repository Layout

```
EasyAccount/
├── app/                 Android client (existing)
├── web/                 React + Vite management client
├── server/              Spring Boot + MySQL API
├── README.md            Project overview and setup instructions
└── .gitignore           Shared repository exclusions
```

The Android project remains the Gradle root. The web and server modules keep their native build files inside their own directories, so they can be started independently.

## Configuration And Security

Tracked files must never contain live service credentials. The Android AI key is a documented placeholder only. The server reads database connection values from `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, while a committed example configuration demonstrates their use without real values.

Root ignore rules exclude Node dependencies, Maven and Gradle output, IDE metadata, Android local properties, environment files, and common credential containers. Existing public Git history is retained as explicitly requested.

## Documentation And Verification

The README documents every module, prerequisites, database setup, environment variables, and startup commands. Validation consists of web production build, server Maven test/package, Git ignore checks, source-policy checks for Android AI configuration, and staged-content credential scanning.
