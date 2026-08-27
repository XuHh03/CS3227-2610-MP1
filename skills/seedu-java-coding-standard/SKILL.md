---
name: seedu-java-coding-standard
description: Apply the SE-EDU basic and intermediate Java coding conventions to NutriByte Java code.
---

# NutriByte Java coding standard

Apply the SE-EDU Java coding standard (basic and intermediate rules) from:
https://se-education.org/guides/conventions/java/intermediate.html

For topics not covered there, follow the Google Java Style Guide.

## Required rules

- Keep every class in a lower-case package named after the project and its logical area.
- Use PascalCase nouns for classes and enums, camelCase for methods and variables, and
  SCREAMING_SNAKE_CASE for constants.
- Name methods with verbs. Test methods may use
  `featureUnderTest_testScenario_expectedBehavior()`.
- Use four spaces for indentation, K&R braces, and braces around every control-flow body.
- Keep lines at or below 120 characters; wrap longer lines at readable boundaries with
  eight-space continuation indentation.
- Use explicit imports, no wildcard imports, and keep import ordering consistent.
- Initialize variables at declaration where practical and keep them in the smallest useful
  scope. Do not expose mutable fields publicly.
- Add descriptive Javadoc to public classes and non-trivial public methods. Getters,
  setters, and test methods may omit it when their purpose is obvious.
- Write comments in clear American English.

## Development workflow

Before completing Java changes, inspect touched files for these rules, update relevant
JUnit tests after behavior changes, and run the project's Gradle test task.
