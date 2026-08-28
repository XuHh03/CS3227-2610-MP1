# NutriByte command-line UI test plan

## Personality notes

Byte is NutriByte's upbeat, practical pantry sidekick. User-facing greetings,
help text, confirmations, and the GUI use friendly shelf and freshness language.
The personality supports the pantry workflow without adding unnecessary chatter.

These tests run each scenario as a separate process using Java 25. Output must
match exactly, including line order and punctuation. UI-001 through UI-007 use
an isolated temporary data file; remove it before each case.

## Maintenance rule

Update this test plan after every implementation increment. Existing cases must
continue to pass as regression tests, and each new user-facing feature must
have at least one corresponding test case before the increment is considered
complete.

The add command supports these forms:

```text
add <name> <quantity>
add <name> <quantity> <category>
add <name> <quantity> expiry <YYYY-MM-DD>
add <name> <quantity> <category> expiry <YYYY-MM-DD>
```

## Manual GUI compatibility checks

The JavaFX GUI is manually tested on Ubuntu, macOS, and Windows using the
supported Java 25 runtime. For each platform, verify the minimum window size,
normal resizing, pantry background visibility, custom item cells, avatars,
search/filter result display, and Enter-key command submission. Repeat the
visual checks with an English locale and one non-English OS locale to catch
layout or date-formatting problems.

## UI-010 — Display help

Aim: Verify that `help` displays usage information for all available commands.

Input:

```text
help
bye
```

Expected output includes:

```text
Available commands:
  add <name> <quantity> [category] [expiry YYYY-MM-DD]
  list
  consume <name> <quantity>
  restock <name> <quantity>
  delete <index>
  search <text>
  filter category <category>
  filter expiry-before <date>
  filter expiry-between <start> <end>
  help
  bye or exit
```

## UI-011 — Edit an item by index

Aim: Verify that an item can be corrected by its one-based list index, including
when two pantry entries have the same name.

Input:

```text
add milk 2
add milk 3 dairy 2026-01-01
edit 2 quantity 7
edit 2 category grains
edit 2 expiry none
list
bye
```

Expected output includes:

```text
Added: milk (2)
Added: milk (3)
Edited item 2.
Edited item 2.
Edited item 2.
Pantry items:
1. milk (2)
2. milk (7) [grains]
Goodbye! Keep your pantry fresh.
```

## GUI-002 — Enter commands and view pantry items

Aim: Verify that commands can be entered in the GUI using the same CLI syntax.

Run:

```text
./gradlew run
```

Input in the GUI command field:

```text
add rice 3
list
help
```

Expected result:

- Command status and help output appear in the compact status area.
- Pantry items remain visible in the scrollable pantry list.
- The command field accepts both the Run button and the Enter key.
- The conversation panel shows only the most recently submitted command and its response.
- Search and filter results, including their numbered items, appear in the conversation panel.
- Search and filter results replace the pantry view while retaining their original pantry indexes.
- Byte explains that `list` restores the complete pantry view.

## GUI-003 — Distinguish user commands and errors

Aim: Verify that the polished GUI makes the conversation easy to scan and
draws attention to invalid input.

Run:

```text
./gradlew run
```

Input in the GUI command field:

```text
add rice 3
wat
```

Expected result:

- The pantry section remains the largest area and expands when the window is resized.
- Each pantry row displays its one-based index for use with `delete <index>` and `edit <index>`.
- Each row separates the item name and quantity from colored category and expiry badges.
- Expiry badges are green for later dates, orange for dates within seven days, and red for expired items.
- The submitted commands appear as right-aligned blue messages.
- Each command's multi-line response appears in one wrapped neutral message box.
- Indentation in structured responses such as `help` is preserved for readability.
- The unknown-command error appears in one distinct red-tinted message box.
- An unrecognized command remains in the input field so it can be corrected and resubmitted.
- A recognized command with invalid arguments, such as `add 3 milk`, also remains in the input field.
- Leading/trailing or repeated whitespace does not crash the parser and is handled consistently.
- Invalid names, non-positive quantities, malformed dates, and unknown categories show a clear error.
- A reversed expiry range shows an error instead of silently returning an empty result.
- Submitting another command replaces the previous command and response in the conversation panel.
- Long responses automatically scroll to keep the latest output visible.
- The window remains usable after resizing it to a smaller supported size.

## UI-012 — Reject invalid edit targets and values

Aim: Verify that invalid edit indexes, fields, quantities, and dates are rejected
without changing the selected pantry item.

Input:

```text
add milk 2
edit 0 quantity 4
edit 1 colour white
edit 1 quantity 0
edit 1 expiry tomorrow
list
bye
```

Expected output includes:

```text
Item number is out of range.
Editable fields are name, quantity, category, and expiry.
Invalid value for quantity.
Invalid value for expiry.
Pantry items:
1. milk (2)
Goodbye! Keep your pantry fresh.
```

## Build and run command

```text
rm -rf /tmp/nutribyte-classes && mkdir -p /tmp/nutribyte-classes && javac -encoding UTF-8 -d /tmp/nutribyte-classes $(rg --files src/main/java -g '*.java')
rm -f /tmp/nutribyte-ui-test-data.txt
java -Dnutribyte.dataFile=/tmp/nutribyte-ui-test-data.txt -cp /tmp/nutribyte-classes nutribyte.ui.NutriByte
```

## UI-001 — Start and exit

Aim: Verify that NutriByte greets the user and exits with `bye`.

Input:

```text
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Goodbye! Keep your pantry fresh.
```

## GUI-001 — Launch JavaFX pantry view

Aim: Verify that the JavaFX interface launches and displays persisted pantry items.

Setup: Add at least one item to `data/pantry.txt` using the CLI, then run:

```text
./gradlew run
```

Expected result:

- A window titled `NutriByte` opens.
- The window displays the `NutriByte Pantry` heading.
- The persisted pantry items appear in the list.
- The window can be closed normally.

## UI-009 — Filter by category and expiry range

Aim: Verify that filtering uses pantry metadata rather than item-name search.

Input:

```text
add milk 2 dairy 2026-09-15
add rice 3 grains 2026-12-01
filter category dairy
filter expiry-between 2026-09-01 2026-09-30
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          restock <name> <quantity>, delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Added: milk (2)
Added: rice (3)
Filtered items:
1. milk (2) [dairy, expires 2026-09-15]
Filtered items:
1. milk (2) [dairy, expires 2026-09-15]
Goodbye! Keep your pantry fresh.
```

## UI-008 — Persist pantry data between runs

Aim: Verify that an item saved by one process is loaded by the next process.

First process input:

```text
add oats 2 grains 2026-12-01
bye
```

Second process input:

```text
list
bye
```

Expected output from the second process:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Pantry items:
1. oats (2) [grains, expires 2026-12-01]
Goodbye! Keep your pantry fresh.
```

## UI-007 — Add category and expiry metadata

Aim: Verify that an item can be stored and displayed with category and expiry date metadata.

Input:

```text
add milk 2 dairy 2026-09-15
list
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Added: milk (2)
Pantry items:
1. milk (2) [dairy, expires 2026-09-15]
Goodbye! Keep your pantry fresh.
```

## UI-002 — Add and list an item

Aim: Verify that an added pantry item appears in the list with its quantity.

Input:

```text
add rice 3
list
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Added: rice (3)
Pantry items:
1. rice (3)
Goodbye! Keep your pantry fresh.
```

## UI-003 — Consume and restock an item

Aim: Verify that consuming decreases quantity and restocking increases it.

Input:

```text
add rice 3
consume rice 1
restock rice 4
list
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Added: rice (3)
Consumed: rice (1)
Restocked: rice (4)
Pantry items:
1. rice (6)
Goodbye! Keep your pantry fresh.
```

## UI-004 — Reject invalid quantities and stock operations

Aim: Verify that invalid quantities, insufficient stock, and unknown items are
reported without corrupting the pantry state.

Input:

```text
add rice 3
consume rice 5
consume rice 0
restock rice -1
restock flour 2
list
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Added: rice (3)
Not enough stock to consume 5 rice.
Quantity must be greater than zero.
Quantity must be greater than zero.
Item not found: flour
Pantry items:
1. rice (3)
Goodbye! Keep your pantry fresh.
```

## UI-005 — Search pantry items

Aim: Verify that search finds matching item names without changing the pantry.

Input:

```text
add rice 3
add brown 2
add milk 1
search rice
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Added: rice (3)
Added: brown (2)
Added: milk (1)
Matching items:
1. rice (3)
Goodbye! Keep your pantry fresh.
```

## UI-006 — Delete a pantry item by index

Aim: Verify that delete removes the exact item selected by its displayed index
and reports invalid indexes.

Input:

```text
add rice 3
add milk 1
add milk 4
delete 3
delete 9
list
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <index>, search <text>,
          list, bye
Added: rice (3)
Added: milk (1)
Deleted item 3: milk (4)
Item number is out of range.
Pantry items:
1. rice (3)
2. milk (1)
Goodbye! Keep your pantry fresh.
```
