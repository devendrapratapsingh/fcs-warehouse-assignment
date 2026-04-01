# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

The tricky part here isn't the technology — it's agreeing on *what a cost actually belongs to*.
A warehouse in Amsterdam serves three stores. Do you split its rent by order volume? By headcount?
By revenue? Each method gives a different answer, and whichever one you pick, someone in Finance
will push back on it. I've seen teams spend more time arguing about allocation methodology than
building the actual tracking system.

The questions I'd want answered first:
- What's already in the financial system today, and what's missing? There's no point
  re-instrumenting things that are already tracked — I'd start from the gaps.
- How often do operations teams actually need this data? Daily close is very different
  from a monthly management report, and the architecture changes a lot depending on that.
- Is the legacy store manager system (the one in this codebase) expected to be the source
  of truth, or just a consumer?

One thing I'd flag specific to this domain: a replaced warehouse and its successor share
the same Business Unit Code. If cost history isn't explicitly tied to the warehouse record
(including archivedAt), you lose the ability to say "what did this location cost *before*
the replacement". That seems like an obvious requirement but it's easy to overlook when
you're focused on the operational side.

My instinct would be to start simple — volume-weighted allocation, validated against
a couple of known periods — rather than building a sophisticated model on data quality
that hasn't been verified yet. I've been burnt by that before.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

First thing I'd want is visibility before anyone talks about optimization. You can't confidently
cut costs in something you can't measure, and in my experience "let's optimize" conversations
often start before the baseline data is even reliable.

Assuming data is in decent shape, the two areas I'd look at first for this kind of system:

**Capacity utilization** — the domain already has `stock` and `capacity` on each warehouse,
and `maxCapacity` on a location. If a warehouse is consistently running at say 40% capacity,
that's a concrete conversation to have: can you replace it with a smaller one at the same
location (which the replace operation already supports), or consolidate its stock into a
neighbouring warehouse? This is low-hanging fruit because the data is already there.

**Warehouse count per store** — the business rule caps it at 3, but fewer is often cheaper.
I'd look at whether any store is running 3 warehouses where 2 would cover the same volume.
That said, there's usually a redundancy reason for that third warehouse that isn't visible
in the data, so I'd want to talk to operations before recommending consolidation.

On implementation: I'd be cautious about running multiple changes at the same time.
If you consolidate warehouses *and* change carriers in the same quarter, you can't tell
which one caused a service dip. One thing at a time, with a clear before/after comparison.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

The core problem with *not* integrating is that you end up with two versions of the truth.
Operations sees one number, Finance sees another, and every month-end becomes a reconciliation
exercise. I've been in those meetings — they're not fun and they're not cheap.

What I'd actually want to know before designing anything:
- Does the financial system expose an API, or are we talking flat-file exports over SFTP?
  That alone determines half the architecture.
- How stale is "acceptable"? Near-real-time is a very different problem from end-of-day.
- Who owns the mapping between warehouse business units and GL cost centres? That sounds
  boring but it causes the most integration bugs in practice.

One thing I'd borrow from this codebase is the event pattern used in `StoreEventHandler` —
it fires the event only after the transaction commits (`AFTER_SUCCESS`). That matters a lot
for financial data: you don't want a cost event reaching the ERP for a warehouse creation
that then gets rolled back. Same principle applies here.

I'd also build a reconciliation check from day one — something that compares totals on both
sides daily and alerts on discrepancies above a threshold. Not because I expect bugs, but
because integration points drift over time (schema changes, timezone issues, rounding) and
you want to catch it early rather than during an audit.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Honestly, forecasting in fulfillment is hard mostly because the costs aren't cleanly variable
or fixed — rent is fixed, labour is semi-fixed, transport scales with volume, consumables are
variable. A flat "last year + 5%" budget doesn't work well because you'll be wrong in both
directions depending on the quarter.

The thing I'd push for first is separating the *operational drivers* from the cost line items.
For this system specifically: warehouse count, location, and capacity are the main drivers.
If you model cost as a function of those rather than just extrapolating history, the budget
automatically adjusts when a warehouse is added or replaced. That's actually achievable with
relatively simple logic given the data that already exists in this domain.

A few things I'd flag as important but often underestimated:
- **Seasonality** — fulfillment almost always has peaks. If you don't account for that,
  your forecast will look wrong every Q4 even if the annual total is accurate.
- **Ownership** — a forecast that only Engineering built tends to get ignored by Finance.
  The assumptions need to be co-owned, otherwise it's just a number nobody believes.
- **Frequency** — an annual budget reviewed once is less useful than a rolling 12-week
  view updated monthly. The latter at least lets you course-correct mid-year.

I'd be less focused on sophistication early on and more focused on getting Finance to
actually use the output. A simple model they trust is worth more than a complex one they don't.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

The replace operation in this system is a good example of a pattern that looks simple but
has financial consequences that aren't immediately obvious. Archiving the old warehouse and
creating a new one with the same Business Unit Code means the code is continuous, but the
cost history belongs to two different records. If you don't preserve that clearly, you'll
eventually have someone asking "what did this BUC cost last year" and the answer will be
spread across two rows in the database — or worse, only one of them if historical data
gets cleaned up at some point.

Why I think preservation actually matters in practice:
- **Audits** — if a warehouse operated for 18 months and is now archived, that period's
  costs still need to be traceable. The archivedAt timestamp is effectively the closing
  date for that cost centre.
- **Budget setting for the new warehouse** — the most natural reference for "what should
  this replacement warehouse cost" is what the previous one cost. If that history is gone,
  you're budgeting blind.
- **The stock-matching constraint** — the replace operation requires new stock to equal old
  stock. That's not just an operational check; it also means no phantom inventory gain/loss
  in the transition, which matters for cost of goods calculations.

Things I'm less sure about without knowing more:
- Whether the archived warehouse's cost centre in the ERP should be closed immediately on
  `archivedAt`, or stay open until outstanding invoices clear. That's probably a Finance
  policy question, not an engineering one, but it affects what we need to store.
- How in-flight orders assigned to the old warehouse get re-attributed. That feels like
  it needs an explicit business decision rather than a technical default.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
