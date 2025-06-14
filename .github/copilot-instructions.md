# GitHub Copilot Instructions for EzQuiz Project

## Project Overview
EzQuiz is an Android quiz application built with Java. The app allows users to create, take, and share quizzes on various topics.

## Technology Stack
- **Language**: Java
- **Build System**: Gradle (with Kotlin DSL)
- **Android Components**:
  - Android Jetpack libraries
  - AndroidX 
  - Material Design components

## Code Structure
- `app/src/main/java/dev/` - Contains all Java source files
- `app/src/main/res/` - Contains Android resources (layouts, drawables, etc.)
- `app/src/androidTest/` - Contains Android instrumentation tests
- `app/src/test/` - Contains unit tests

## Coding Conventions
- Follow Java style guide
- Use camelCase for variables and methods
- Use PascalCase for classes and interfaces
- Organize imports alphabetically
- Use descriptive variable and function names
- Include KDoc comments for public functions and classes

## Architecture
The app follows MVVM (Model-View-ViewModel) architecture:
- **Model**: Data classes and repositories
- **View**: Activities, Fragments, and XML layouts
- **ViewModel**: ViewModels that handle UI state and business logic

## Common Tasks
- When implementing new features, create appropriate View and ViewModel classes
- For database operations, use Room persistence library
- For network requests, use Retrofit with coroutines
- For dependency injection, use Hilt
- For UI components, utilize Material Design 3 components
- For navigation between screens, use the Navigation Component

## Testing (optional, include if specified)
- Write unit tests for ViewModels and Repositories
- Write UI tests for critical user flows
- Ensure tests are run before committing changes

## Project-Specific Guidelines
- Prioritize offline-first approach where possible
- Ensure accessibility features are implemented
- Support both light and dark themes
- Follow Material Design guidelines for consistent UI
- Support multiple screen sizes

## Gradle Configuration
- Use the version catalog in `gradle/libs.versions.toml` for dependency management
- Add new dependencies to the appropriate category in the version catalog
