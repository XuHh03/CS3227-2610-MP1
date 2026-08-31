# Reflections on AI-Assisted Software Engineering

## 1. Code Review: Overall vs Specific Reviews

### Overall Project Review

**Prompt:** “Review the entire Java project for OOP and code quality issues. Check class responsibilities, encapsulation, inheritance, abstraction, naming conventions, code duplication, and general coding standards. Only report clear violations.”

This review was useful for identifying high-level issues in the project structure, such as class responsibilities, encapsulation, OOP design, and coding standards. However, it provided less detailed feedback on individual behaviours and edge cases.

### Specific Code Review

**Prompt:** “Review this specific class/method for correctness and edge cases. Identify potential bugs, invalid inputs, error handling issues, and unexpected behaviours. For each issue, explain why it is a problem and suggest a fix.”

Reviewing specific parts of the code gave more detailed feedback. The LLM identified issues such as invalid indexes, insufficient stock, malformed dates, null input, ambiguous names, and other edge cases that were not as apparent in the overall review.

This showed me that overall reviews are more useful for checking project structure and standards, while focused reviews are more effective for finding detailed errors and edge cases.



## 2. Developer guide: an unsuitable first draft and a specific template

**Prompt:** “Add a Developer Guide describing the design and software engineering process, including acknowledgements.”

The first response produced a broad developer guide with a structure chosen by the LLM. It was technically reasonable, but it did not match what I was thinking as it and included more general structure than I wanted. This showed that a broad documentation prompt leaves too much room for the LLM to invent a format.

Then, I provided a specific example template and asked the guide to follow its headings: acknowledgements, design and implementation, architecture, components, and feature implementation. Afterward, I requested a high-level architecture diagram and a few main description points. The prompt therefore evolved from “write a guide” to “adapt this exact structure to the current NutriByte release.”

I verified the result by comparing every class and feature description with the source code, checking the architecture diagram against the actual dependencies, and reviewing the acknowledgement list. The lesson is that providing a concrete reference document is more effective than describing the desired style abstractly.

## 3. Commits: one broad commit, then separate commits and a reusable skill

**Prompt:** “Commit the current changes, split into own commit if necessary" vs adding in "e.g. changes code that can be standalone.”

The first commit request resulted in too much being grouped together because “if necessary” left the splitting rule open to interpretation. I then clarified that standalone changes should be committed separately. This led to focused commits for validation, index preservation, quoted arguments, GUI handling, immutability, parser defensive copying, locale testing, GUI display extraction, GUI bridge extraction.

The LLM could create technically valid commit messages, but deciding the boundaries required understanding which changes could be reviewed, reverted, or tested independently. I checked each diff and ensured the commit message explained the reason for the change. I also used the repository's reusable Git convention skill to make commit organisation and messages faster and more consistent.

This experience showed that prompts should specify the desired unit of change, not just the desired final repository state. It also showed that a reusable process aid is worthwhile when the same review or commit task occurs repeatedly.

## 4. Keeping AI-Generated Code Aligned with Requirements

Throughout development, I asked the LLM to explain what it had changed and why after implementing each feature. This helped me check that the generated code matched the requirements and stayed within the intended pantry-management scope.

I used prompts such as: “Summarise what you changed and explain why each change was necessary.” I then reviewed the explanation against the actual code and project requirements.

This was useful because the LLM could sometimes make additional changes that were not directly requested. By requiring it to explain its changes, I could identify unnecessary modifications and ensure that the final implementation remained aligned with my intended design.

## 5. Overengineering: when the proposed solution is too large

Sometimes the LLM proposed a solution that was technically sound but too elaborate for the prompt or the project's size. Examples included the risk of turning a focused GUI separation into a broad redesign or introducing abstractions that were not needed by the pantry requirements.

The prompt often lacked an explicit complexity limit, so I had to add that constraint through follow-up decisions: preserve the existing public behavior, keep the pantry-only scope, prefer small cohesive classes. This human judgement was necessary because “better architecture” is not automatically better engineering when it adds complexity without solving a real problem.

The practical lesson is to ask for the smallest sufficient change and to review the proposed design before implementation. Next time, I would include the affected files, out-of-scope features, acceptance tests, and a “do not introduce unnecessary abstractions” constraint in the first prompt.

## Overall reflection

Overall, I found that AI worked best when my prompts clearly defined the scope, expected outcome, and constraints. When my prompts were too broad, the LLM was more likely to introduce unnecessary changes or structures. This taught me to give more specific instructions and use my own judgement to review and verify the results.
