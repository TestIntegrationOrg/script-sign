# Feature Intake — PS-SIGN-001

## Title
PowerShell Script Signing Microservice

## Description
Create a production-oriented microservice that accepts an unsigned PowerShell .ps1 script through a REST API and returns the same script with a verifiable Windows Authenticode signature. Use Java and Spring Boot. Keep private keys and credentials outside source control, support an external PKCS#12 signing identity for deployable environments, add trusted timestamping, validate input and payload size, reject unsupported files, avoid retaining script content, clean temporary files, return controlled errors, expose health checks, provide automated unit and integration tests including cryptographic signature verification, and document local setup, configuration, API usage, container execution, security assumptions, and verification on Windows. Do not require production credentials for automated tests.

## Requested Lifecycle
critical

## Source
manual

## Status
intake
