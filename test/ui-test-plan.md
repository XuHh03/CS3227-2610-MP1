# NutriByte command-line UI test plan

These tests run each scenario as a separate process using Java 25. Output must
match exactly, including line order and punctuation. UI-001 through UI-007 use
an isolated temporary data file; remove it before each case.

## Maintenance rule

Update this test plan after every implementation increment. Existing cases must
continue to pass as regression tests, and each new user-facing feature must
have at least one corresponding test case before the increment is considered
complete.

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
          delete <name>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Goodbye! Keep your pantry fresh.
```

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
          restock <name> <quantity>, delete <name>, search <text>,
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
          delete <name>, search <text>,
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
          delete <name>, search <text>,
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
          delete <name>, search <text>,
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
          delete <name>, search <text>,
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
          delete <name>, search <text>,
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
          delete <name>, search <text>,
          filter category|expiry-before|expiry-between, list, bye
Added: rice (3)
Added: brown (2)
Added: milk (1)
Matching items:
1. rice (3)
Goodbye! Keep your pantry fresh.
```

## UI-006 — Delete a pantry item

Aim: Verify that delete removes the named item and reports unknown items.

Input:

```text
add rice 3
add milk 1
delete rice
delete flour
list
bye
```

Expected output:

```text
Hello! I'm NutriByte.
What can I do for you?
Commands: add <name> <quantity> [category] [expiry YYYY-MM-DD],
          consume <name> <quantity>, restock <name> <quantity>,
          delete <name>, search <text>,
          list, bye
Added: rice (3)
Added: milk (1)
Deleted: rice
Item not found: flour
Pantry items:
1. milk (1)
Goodbye! Keep your pantry fresh.
```
