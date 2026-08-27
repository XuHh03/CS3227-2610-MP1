# Project context

All Java code in this repository must follow the project skill
`skills/seedu-java-coding-standard/SKILL.md`, based on the SE-EDU Java coding standard.
Apply it when adding, modifying, or reviewing Java code.

All future commits in this repository must follow the project skill
`skills/seedu-git-standard/SKILL.md`, based on the SE-EDU Git conventions.

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: Intermediate, familiar with fundamentals to data structures and algorithms
* IDE and level of expertise: IntelliJ IDEA or VS Code, intermediate proficiency.

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.

# NutriByte project

NutriByte is a Java desktop pantry inventory and expiry-management application.
Its purpose is to help users track pantry items, quantities, and expiry dates.

## Scope

The main domain is pantry management. Do not add recipes, meal planning, diet
tracking, calorie tracking, OCR, barcode scanning, chatbot functionality, LLM
API integration, cloud synchronisation, or mobile functionality unless the user
explicitly requests it.

## Incremental development

Implement and verify one milestone at a time:

1. Basic greeting and exit
2. Add and list pantry items
3. Consume and restock
4. Validation and error handling
5. Delete and search
6. Categories and expiry dates
7. Persistence
8. JUnit tests and Gradle
9. JavaFX GUI
10. Documentation, CI, and visual polishing

Do not implement future milestones prematurely. When a milestone is complete,
run relevant tests or a representative manual workflow before moving on.

### Test coverage target

Maintain JUnit tests for approximately the highest-value 50% of methods. Prioritize
complex, core, and business-critical methods rather than aiming for uniform coverage
of trivial accessors. Update the relevant JUnit tests after every code change, and
run the test suite before completing each increment, so the tests remain compliant
with this coverage target.

## Architecture

Use separate packages and keep business logic independent of the user interface:

- `nutribyte.model` — domain objects and enums
- `nutribyte.service` — pantry operations and business logic
- `nutribyte.storage` — persistence and file handling
- `nutribyte.ui` — command-line and JavaFX interfaces

Prefer small cohesive classes, meaningful encapsulation, and the simplest design
that satisfies the current milestone. Avoid god classes and unnecessary abstractions.

## Current project direction

The application is being developed incrementally from the existing pantry domain
foundation. Preserve the pantry-only scope and explain significant design choices
briefly so the student can understand and review the implementation.
