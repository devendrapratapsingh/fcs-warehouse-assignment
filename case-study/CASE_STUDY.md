# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

The fundamental challenge is that costs in fulfillment are multi-dimensional and often shared:
a single warehouse serves multiple stores, a single transportation route touches multiple products,
and overhead (rent, utilities) does not map cleanly to any one SKU or order.

Key challenges:
  • Shared cost attribution — how do you split the cost of a warehouse that fulfills both Store A
    and Store B? Headcount-based? Volume-based? Revenue-based? Each method produces different
    incentives and can be gamed.
  • Real-time vs. batch — labor costs accrue by the minute, but most ERP systems post them in
    daily or weekly batches, creating a lag that makes in-period decisions unreliable.
  • Granularity vs. overhead — tracking to the line-item level requires more data collection
    effort than the accuracy it provides at low volumes. You need to decide at what grain
    cost allocation is actually actionable.
  • Data quality — if the Warehouse entity does not capture when it was operational (only
    archivedAt), cost periods cannot be accurately reconstructed for a replaced warehouse.

Questions I would ask before designing the system:
  1. Which cost categories are already captured in the financial system, and which require
     new instrumentation (e.g., IoT sensors for energy, time-tracking for labor)?
  2. At what frequency do finance teams need cost reports — daily close, weekly, or monthly?
  3. Is the allocation method mandated by regulation (e.g., IFRS 16 for leases) or is it
     an internal management decision?
  4. How does the legacy store manager system currently handle cost data, and can it be the
     authoritative source or does it need to be a consumer of our data?

Consideration from experience: the most common failure mode is building a highly precise
allocation model before validating that the underlying data (meter readings, shift logs,
carrier invoices) is actually reliable. I would start with a simple volume-weighted allocation,
validate it against known actuals for 2–3 periods, and only add complexity where the delta
from actuals justifies the engineering investment.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

Identification — I would start with a cost-per-order waterfall broken down by warehouse and store,
then rank each cost category by total spend and variability. High-spend, high-variability categories
are the best optimization candidates because both the saving potential and the confidence interval
on the estimate are large.

Candidate strategies and expected outcomes:
  1. Warehouse consolidation — the system already enforces a maximum of 3 warehouses per store.
     Analysing fulfillment patterns may reveal that one warehouse consistently handles >80% of
     volume; consolidating to fewer, better-positioned warehouses reduces transportation cost
     and overhead, at the risk of reduced redundancy.
  2. Capacity right-sizing — the Location entity exposes maxCapacity. Warehouses operating at
     <50% of their capacity are paying for space they don't use. Replacing them (using the
     existing replace operation) with smaller-capacity units at the same location cuts rent.
  3. Demand-driven stock balancing — if a warehouse's stock is consistently near its capacity
     ceiling, transportation costs spike (emergency replenishment). Monitoring stock/capacity
     ratio over time and pre-positioning stock reduces expediting costs.
  4. Carrier consolidation — grouping shipments from warehouses serving the same store on the
     same transport route lowers per-unit transport cost.

Prioritisation framework:
  • Impact × Confidence × Speed-to-implement — quick wins (carrier consolidation, right-sizing)
    come first; structural changes (warehouse consolidation) require longer analysis and
    stakeholder alignment.

Implementation approach:
  • Instrument first — ensure cost and volume data flows into a reporting layer before any
    physical change so that the baseline is captured.
  • Run one change at a time with a holdout (A/B) group where possible, so the impact is
    attributable.
  • Define a rollback criterion upfront — if service-level agreement metrics (delivery time,
    fill rate) degrade beyond a threshold within 30 days, revert.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

Why integration matters:
  Without it, cost data lives in two places and diverges. Finance reports one set of numbers;
  operations sees another. Decisions made on either dataset carry hidden reconciliation risk,
  and the month-end close becomes a manual fire-drill to align the two.

Benefits of tight integration:
  • Single source of truth — the financial system (ERP/GL) becomes the authoritative record;
    the Cost Control Tool reads from it rather than maintaining a parallel ledger.
  • Real-time visibility — warehouse managers can see cost accruals as operations happen,
    not weeks later when the invoice arrives.
  • Audit trail — every cost entry in the tool maps to a journal entry in the ERP, making
    external audits straightforward.
  • Automated reporting — dashboards and forecasts draw from live GL data, eliminating
    spreadsheet-based monthly consolidation.

How I would ensure seamless sync:
  1. Event-driven over polling — financial systems that support webhooks or message queues
     (e.g., SAP IDocs, Oracle Business Events) should push changes rather than having the
     tool poll. This mirrors what was already done in this codebase: StoreEventHandler uses
     AFTER_SUCCESS to guarantee events reach the legacy system only after a confirmed commit.
     The same pattern applies here — cost events should fire only after the transaction
     that creates them is durable.
  2. Idempotent consumers — the integration layer must handle duplicate events (at-least-once
     delivery) by deduplification on a business key (invoice number, journal entry ID).
  3. Schema versioning — financial system schemas change with upgrades. The integration
     contract should be versioned (via an OpenAPI or AsyncAPI spec) so both sides can
     evolve independently.
  4. Reconciliation job — a nightly batch compares Cost Control Tool totals against ERP
     totals per cost centre. Any discrepancy above a tolerance threshold raises an alert
     rather than silently diverging.

Questions I would ask:
  • Does the ERP expose a real-time API or only batch file exports?
  • What is the acceptable latency between an operational event (warehouse created) and
    the corresponding GL posting?
  • Who owns the chart of accounts mapping — Finance or Engineering?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Why it matters in fulfillment specifically:
  Fulfillment costs are semi-variable — rent and headcount are fixed in the short run, but
  transport and consumables scale with order volume. A forecast that treats all costs as
  fixed will drastically underestimate costs during peak periods (e.g., holiday season),
  and a purely variable model will overestimate them in slow periods.

What I would design for:
  1. Driver-based model — instead of extrapolating last year's spend by a flat percentage,
     identify the operational drivers (orders per day, SKU count, warehouse count, location)
     and model cost as a function of those drivers. This makes the forecast self-correcting:
     if the business adds a new warehouse in AMSTERDAM-001, the cost forecast updates
     automatically because the driver (warehouse count at that location) changed.

  2. Scenario planning — at minimum three scenarios: base (expected volume), optimistic
     (+20% volume), and stress (peak like Q4 + one warehouse outage). The Warehouse entity's
     capacity and stock fields provide the inputs for stress scenario modelling.

  3. Rolling forecast vs. annual budget — an annual budget becomes stale by Q2. A rolling
     13-week forecast updated weekly gives operations teams a decision-relevant view.

  4. Actuals vs. budget variance reporting — the system should surface variances above a
     configurable threshold (e.g., >5% over budget for any cost centre) in near-real-time
     so managers can respond within the period, not after month-end close.

Key design considerations:
  • Data history — accurate forecasting requires at least 12–24 months of actuals at the
    same granularity as the forecast. The Warehouse archivedAt field is essential here:
    it lets us reconstruct which warehouses were active in which periods.
  • Seasonality — fulfillment demand is rarely flat. The model must separate trend from
    seasonal components (e.g., using a simple Holt-Winters decomposition) to avoid
    over-forecasting in low seasons and under-forecasting in high seasons.
  • Ownership — a forecast nobody believes will not be acted on. Finance and Operations
    must co-own the assumptions; the system provides the calculation engine, not the
    business judgement.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

How replacement works in this system:
  The ReplaceWarehouseUseCase archives the existing warehouse (sets archivedAt) and creates a
  new one with the same businessUnitCode. Both records share the same code but are distinguished
  by archivedAt: the archived record carries all historical context, the new record starts fresh.

Why cost history preservation is critical:
  1. Audit and compliance — financial regulations (IFRS, local GAAP) require that cost records
     for a closed asset (the archived warehouse) remain auditable for several years. Deleting
     or overwriting the record would create a compliance gap.
  2. Variance analysis — the budget for the new warehouse is typically based on the cost
     performance of the old one. If historical data is lost, the baseline for "what should
     this location cost?" disappears, and any budget deviation becomes unexplainable.
  3. Depreciation and lease obligations — the old warehouse may have outstanding lease
     commitments or depreciation schedules in the ERP. The archivedAt timestamp is the
     legal end date of those obligations; losing it means the ERP cannot correctly close
     the asset.

How this relates to keeping the new warehouse within budget:
  • The replacement event is a natural re-baselining point. Finance should update the
    budget for the business unit code to reflect the new warehouse's capacity and expected
    throughput, using the old warehouse's actuals as the starting reference.
  • The stock-matching constraint in the replacement operation (new stock must equal old
    stock) ensures no inventory is created or destroyed in the transition — a silent
    inventory gain/loss would distort cost-of-goods-sold in the period of replacement.
  • Capacity change (old cap → new cap) should trigger an automatic budget adjustment
    for capacity-driven costs (rent per m², insured value). The integration with the
    financial system (Scenario 3) makes this automatic rather than a manual journal.

Questions I would ask:
  • Is the archived warehouse's GL cost centre closed immediately on archiving, or does
    it remain open until outstanding invoices (last month's rent, utilities) are settled?
  • How should in-flight fulfillment orders that were assigned to the old warehouse be
    re-attributed — to the new warehouse or to the old warehouse's final period?
  • What is the retention policy for archived warehouse records? (GDPR and local finance
    law may require different retention periods.)

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
