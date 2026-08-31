# NutriByte Development Interaction Log

This log records the significant prompts and follow-up interactions from the NutriByte development process. Each entry states what was requested, what was done, and how the result was checked.

## 1. Initial project review

**Request:** Review the entire Java project for correctness, encapsulation, class responsibilities, OOP, methods, Java standards, error handling, persistence, testing, and code quality.

**Action:** Inspected all Java source and test files and reported only clear violations with file/line references, severity, explanations, and fixes.

**Findings recorded:** Mutable `PantryItem` state, parser array exposure, default-locale formatting, GUI responsibility concentration, and missing GUI bridge/display tests.

**Verification:** Compared every finding with the implementation and existing test behavior before selecting fixes.

## 2. Prioritising fixes

**Request:** Decide which findings should be fixed.

**Action:** Prioritised correctness, encapsulation, portability, and testability issues. Optional design preferences and unnecessary abstractions were excluded.

**Decision:** Keep the pantry-only scope and avoid introducing a command framework, database, or broad redesign.

## 3. Commit organisation

**Request:** Commit current changes and split standalone changes into separate commits where appropriate.

**Action:** Created focused commits for null input validation, original-index preservation, quoted command arguments, and GUI command handling.

**Verification:** Reviewed each commit's diff and ran Gradle tests after the implementation changes.

## 4. Incremental implementation guidance

**Request:** Explain how to start the fixes one by one, then implement them immediately.

**Action:** Used one milestone per change, explaining the rationale and updating tests alongside the code.

**Changes:** Made `PantryItem` immutable; added replacement methods; updated `PantryService`; added defensive copies to `ParsedCommand`; made formatting locale-safe; extracted `PantryDisplayProvider`; extracted `GuiCliBridge`.

**Verification:** Ran `./gradlew test check` after each milestone and checked that the public command behavior remained compatible.

## 5. Windows Gradle failure

**Request:** Diagnose `NutriByteTest.main_searchAndFilterCommands_preserveOriginalIndexes()` failing on Windows because `true` was expected but `false` was returned.

**Action:** Traced the failure to output comparison using Unix `\n` while Windows `println` output contains `\r\n`.

**Fix:** Normalised `\r\n` to `\n` in the test helper.

**Verification:** Ran the full Gradle suite and committed the fix as `da50ee2 Normalize CLI test line endings`.

## 6. Runtime verification

**Request:** Check whether everything was running properly.

**Action:** Confirmed Java 25, ran `./gradlew test check shadowJar`, inspected the generated JAR, and exercised a CLI workflow covering add, consume, restock, search, filter, invalid index, and persistence.

**Result:** Tests and Checkstyle passed; the JAR contained NutriByte classes, resources, and JavaFX classes. GUI visual launch was not performed in the headless environment.

## 7. First User Guide draft

**Request:** Add a User Guide in `docs/README.md` covering all important features and GitHub Pages use.

**Action:** Replaced placeholder text with setup instructions, command syntax, examples, validation behavior, and persistence details.

**Verification:** Checked examples against `NutriByte`, `Parser`, `Ui`, and `PantryStorage` rather than relying on generic command conventions.

## 8. User Guide refinement

**Request:** Make the User Guide systematic, add a contents page, link to each feature, and include more examples.

**Action:** Renamed the file to `docs/UserGuide.md`, added a linked contents section, numbered each feature, and added expected output for normal and invalid commands.

**Additional update:** Added explicit setup and `./gradlew test check` instructions for peer testers.

## 9. Developer Guide template

**Request:** Use the supplied Developer Guide example as the minimal template.

**Action:** Structured `docs/DeveloperGuide.md` with acknowledgements, design and implementation, architecture, components, feature implementation, and testing/development process sections.

**Verification:** Checked that the descriptions match current classes, commands, persistence behavior, and the latest release.

## 10. High-level architecture diagram

**Request:** Generate a write-up that could be used by diagram software, then provide only a high-level architecture description.

**Action:** Created `docs/Architecture.puml` with four layers: User Interface, Service, Model, and Storage, plus the pantry data file. Added a concise four-point explanation to the Developer Guide.

**Verification:** Inspected the rendered `docs/Architecture-NutriByte_Architecture.png` and confirmed that it matches the PlantUML source.

## 11. Submission deliverables

**Request:** Review the submission requirements and add all necessary items.

**Action:** Added `docs/Reflections.md`, `logs/DevelopmentSummary.md`, and `release/nutribyte.jar`; confirmed source, guides, architecture files, and logs are present.

**Verification:** Ran `./gradlew test check shadowJar`, confirmed `release/nutribyte.jar` is 8.1 MB with `nutribyte.ui.NutriByteGui` as its main class, and ran the structure checker successfully:

```text
All required items present.
```
