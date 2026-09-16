# Smart Solar Microgrid Trading System

SE4040 Enterprise Application Development - Assignment 1

## Project Structure

- `backend/` - C# Web API and business logic
- `web/` - Backoffice and Grid Operator web application
- `mobile/` - Native Android application
- `deployment/` - IIS and MongoDB deployment notes
- `docs/` - diagrams, database design, testing, screenshots and report materials

## Architecture

Web Application -> REST API -> MongoDB

Android Application -> REST API -> MongoDB

Android also uses SQLite for local persistence.

## Team

- Member 1 - Backend / API / Integration
- Member 2 - Web Application
- Member 3 - Android Prosumer
- Member 4 - Android Operator / Maps / QR

## Branches

- `main` - stable version
- `develop` - integration branch
- `feature/backend-api`
- `feature/web-client`
- `feature/android-prosumer`
- `feature/android-operator`