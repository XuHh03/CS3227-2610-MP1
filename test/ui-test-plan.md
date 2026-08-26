# NutriByte command-line UI test plan

These tests run each scenario as a separate process using Java 25. Output must
match exactly, including line order and punctuation.

## Maintenance rule

Update this test plan after every implementation increment. Existing cases must
continue to pass as regression tests, and each new user-facing feature must
have at least one corresponding test case before the increment is considered
complete.

## Build and run command

```text
rm -rf /tmp/nutribyte-classes && mkdir -p /tmp/nutribyte-classes && javac -encoding UTF-8 -d /tmp/nutribyte-classes $(rg --files src/main/java -g '*.java')
java -cp /tmp/nutribyte-classes nutribyte.ui.NutriByte
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
Commands: add <name> <quantity>, consume <name> <quantity>,
          restock <name> <quantity>, list, bye
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
Commands: add <name> <quantity>, consume <name> <quantity>,
          restock <name> <quantity>, list, bye
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
Commands: add <name> <quantity>, consume <name> <quantity>,
          restock <name> <quantity>, list, bye
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
Commands: add <name> <quantity>, consume <name> <quantity>,
          restock <name> <quantity>, list, bye
Added: rice (3)
Not enough stock to consume 5 rice.
Quantity must be greater than zero.
Quantity must be greater than zero.
Item not found: flour
Pantry items:
1. rice (3)
Goodbye! Keep your pantry fresh.
```
