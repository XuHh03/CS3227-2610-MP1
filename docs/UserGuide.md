# NutriByte User Guide

NutriByte is a pantry inventory assistant for tracking items, quantities, categories, and expiry dates. Byte accepts the same commands in the desktop window and command-line interface.

## Contents

1. [Getting started](#1-getting-started)
2. [Testing the application](#2-testing-the-application)
3. [Adding pantry items](#3-adding-pantry-items)
4. [Viewing the pantry](#4-viewing-the-pantry)
5. [Consuming and restocking](#5-consuming-and-restocking)
6. [Searching items](#6-searching-items)
7. [Filtering items](#7-filtering-items)
8. [Editing items](#8-editing-items)
9. [Deleting items](#9-deleting-items)
10. [Validation and error handling](#10-validation-and-error-handling)
11. [Saving and loading data](#11-saving-and-loading-data)
12. [Command summary](#12-command-summary)

## 1. Getting started

NutriByte requires JDK 25. From the project folder, start the desktop application with:

```shell
./gradlew run
```

Alternatively, run `nutribyte.ui.NutriByte` from your IDE for the command-line interface. Type `help` at any time for command syntax. Type `bye` or `exit` to save and quit.

To run the packaged release, use Java 25:

```shell
java -jar release/nutribyte.jar
```

## 2. Testing the application

Run the automated tests and style checks from the project folder:

```shell
./gradlew test check
```

The command should end with `BUILD SUCCESSFUL`. It verifies pantry operations, invalid-input handling, persistence, command parsing, and GUI support classes without depending on an existing `data/pantry.txt` file.

## 3. Adding pantry items

Use `add` with a name and a positive quantity. Names containing spaces must be enclosed in double quotes. Categories are `general`, `grains`, `dairy`, `produce`, `meat`, `canned`, `snacks`, and `other`.

### Syntax

```text
add <name> <quantity>
add <name> <quantity> <category>
add <name> <quantity> expiry <YYYY-MM-DD>
add <name> <quantity> <category> expiry <YYYY-MM-DD>
```

### Examples

```text
> add rice 3
Nice! Added rice (3) to the pantry.

> add "Fresh Milk" 2 dairy
Nice! Added Fresh Milk (2) to the pantry.

> add apples 6 produce expiry 2026-09-15
Nice! Added apples (6) to the pantry.
```

An expiry date without a category uses `general`. Dates must use the `YYYY-MM-DD` format.

## 4. Viewing the pantry

Use `list` to display every item. Each item receives an index for use with commands such as `consume index`, `edit`, and `delete`.

```text
> list
There's everything in your pantry above
1. rice (3)
2. Fresh Milk (2) [dairy]
3. apples (6) [produce, expires 2026-09-15]
```

## 5. Consuming and restocking

Use `consume` to reduce stock and `restock` to increase it. Use a name when it is unique, or the index shown by `list`.

### Syntax

```text
consume <name> <positive quantity>
consume index <index> <positive quantity>
restock <name> <positive quantity>
restock index <index> <positive quantity>
```

### Examples

```text
> consume "Fresh Milk" 1
Marked as consumed Fresh Milk (1).

> restock index 1 2
Topped up item 1 (2).
```

NutriByte prevents consuming more units than are available. If a name matches multiple items, use the indexed form.

## 6. Searching items

Use `search` to find text in item names, categories, or expiry dates. Search terms containing spaces may be quoted or entered as separate words.

```text
> search milk
Matching items:
2. Fresh Milk (1) [dairy]
```

Search results retain the original pantry indexes, so the displayed index can still be used for an update or deletion.

## 7. Filtering items

Use filters to view one category or an expiry-date window.

### Syntax and examples

```text
filter category <category>
filter expiry-before <YYYY-MM-DD>
filter expiry-between <start YYYY-MM-DD> <end YYYY-MM-DD>

> filter expiry-between 2026-09-01 2026-09-30
Filtered items:
3. apples (6) [produce, expires 2026-09-15]
```

Both ends of an `expiry-between` range are inclusive. Items without an expiry date are excluded from date filters. Filter results preserve original pantry indexes.

## 8. Editing items

Use `edit` with an item index, one editable field, and its replacement value.

```text
edit <index> name <new name>
edit <index> quantity <positive quantity>
edit <index> category <category>
edit <index> expiry <YYYY-MM-DD or none>
```

Examples:

```text
> edit 1 name "Wholegrain Rice"
Updated item 1—looking good!

> edit 1 expiry none
Updated item 1—looking good!
```

Use `none` to clear an expiry date.

## 9. Deleting items

Use the index shown by `list`, `search`, or `filter`:

```text
> delete 1
Cleared item 1: Wholegrain Rice (5) [grains]
```

Deletion removes the item from the pantry and is saved automatically.

## 10. Validation and error handling

NutriByte rejects invalid input without terminating or changing the existing pantry. Quantities must be positive whole numbers; indexes must refer to an item currently shown by `list`; categories and dates must be valid.

```text
> consume rice 0
Quantity must be a positive whole number. You entered 0.

> filter expiry-before 2026-02-30
Invalid expiry date. Use YYYY-MM-DD and a real calendar date, such as 2026-09-15.
```

## 11. Saving and loading data

NutriByte automatically loads and saves `data/pantry.txt` after successful commands and when you exit. A missing file starts an empty pantry and is created when the first change is saved. Keep the file in the project directory. If the file is malformed or cannot be written, Byte reports the problem instead of silently overwriting existing data.

## 12. Command summary

| Command | Purpose |
| --- | --- |
| `add` | Add an item with quantity, category, and optional expiry date |
| `list` | Display all items and their indexes |
| `consume` | Reduce an item's quantity |
| `restock` | Increase an item's quantity |
| `search` | Find matching item details |
| `filter` | Filter by category or expiry date |
| `edit` | Change an item's name, quantity, category, or expiry |
| `delete` | Remove an item by index |
| `help` | Show command help |
| `bye` / `exit` | Save and close NutriByte |
