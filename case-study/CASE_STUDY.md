# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

The tricky part here is that most costs don't belong cleanly to one warehouse or one store. A truck makes three stops, a staff member splits their day between two areas — how do you split that fairly? Before anything else I'd want to know what level of detail the business actually needs. Per warehouse? Per order? Per product line? Because the answer changes everything about how you design the tracking.

I'd also want to know what's already being captured somewhere — payroll systems, fuel cards, WMS logs — vs. what's still on paper or in someone's spreadsheet. In my experience the data quality problem is usually bigger than the modelling problem. You can have a perfect allocation formula but if the inputs are being entered two weeks late it's not worth much.

A few things I'd want to nail down early:
- Who owns the cost data? Finance? Operations? Both? If it's both, you need a clear rule about which system wins when they disagree.
- Are there shared costs between warehouses at the same location that need splitting? And if so, by what driver — volume handled, floor space, headcount?
- When a warehouse gets replaced and archived, what happens to its cost history? It needs to stay visible and attributed to that period, not get rolled into the new warehouse's numbers.

The fixed vs. variable split is also something I'd push to define upfront. Rent is fixed, overtime isn't. Mixing them in the same bucket makes trend analysis useless and budget conversations frustrating.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

First thing I'd want to know is where the money is actually going. Is it labour? Transport? Holding costs from slow-moving stock? Because "reduce costs" without knowing the biggest driver is just guessing. I've seen teams spend months optimising transport routing only to find out labour was 3x the cost.

I'd also ask what's contractually fixed. If a warehouse lease runs for another 4 years, consolidation isn't really on the table right now regardless of how good the numbers look on paper.

Once I understand the cost structure, the place I'd start is utilisation. If a warehouse is sitting at 40% of its capacity most of the time, that's the most obvious waste — you're paying for space and staff to manage space you're not using. The system already tracks capacity and stock per warehouse, so you can see this pretty quickly without needing a big data project.

After that I'd look at whether the right stock is in the right place. A warehouse in Tilburg serving stores in Amsterdam when there's capacity in Amsterdam-001 is just adding transport cost and lead time for no reason. That's usually fixable without any capital spend.

The bigger structural moves — consolidation, replacement, opening a new location — I'd put those later. They take longer, cost more upfront and need proper financial modelling before anyone commits. But the data from the quick wins feeds directly into making those decisions more confidently.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

The first thing I'd want to understand is what financial system is already in place and who owns the data. Because if finance has an ERP that's the system of record, the Cost Control Tool needs to feed into that — not replace it. Getting that boundary wrong causes a lot of pain later when the two systems show different numbers and nobody knows which one to trust.

I'd also ask how quickly the business needs the data. Real-time is a very different problem than a nightly batch. Most finance teams are fine with a few hours of lag for cost postings, but operational managers often want something closer to live so they can react the same day.

The main risk I've seen with these integrations is double-entry and data drift. Someone manually enters something in the ERP, the same event comes in from the operational system, and suddenly the cost centre is double-counted. The only reliable fix is treating the integration as event-driven — operational events (warehouse created, stock moved, warehouse archived) trigger automatic postings — and making sure those postings are idempotent so retries don't cause duplicates.

The other thing worth raising early is the reconciliation question. Even with a well-designed integration, totals will drift over time due to timing differences, corrections, and edge cases. Having a scheduled reconciliation report that compares the two systems and flags gaps is the safety net that keeps finance comfortable and catches issues before month-end becomes a fire drill.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Before designing anything here I'd want to know two things: how much historical data exists, and how stable demand actually is. A forecast built on six months of data during an unusual period is worse than no forecast at all — it gives false confidence.

I'd also want to know who owns the budget. If it's all centrally managed by finance, you build one thing. If individual warehouse managers are accountable for their own numbers, you need something they can actually use day-to-day, not just a report that finance looks at once a quarter.

The thing I'd push back on is building a complex forecasting model too early. The most valuable thing upfront is just getting clean actuals — what did each warehouse actually cost last month, broken down in a way that makes sense. Once you have that for 6-12 months, patterns emerge on their own. You can see seasonality, you can see which locations are consistently over, and you can start building a sensible baseline from that.

One thing worth thinking about with this system specifically: the max capacity and max warehouses per location are hard operational ceilings. Those need to be inputs to the capacity planning side of budgeting. If Amsterdam-001 is already at its warehouse limit, the budget for growth in that area has to account for either a location change or a replacement — you can't just add more units indefinitely. That constraint is easy to miss if forecasting is done purely in a spreadsheet disconnected from the operational data.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

The first thing I'd want to know is what triggered the replacement. End of lease is straightforward. But if it's because the old warehouse was too expensive or had operational problems, that context matters — because if you don't understand why it failed, the new one might have the same issues.

I'd also want to know whether there's a transition period where both warehouses are running at the same time. That overlap is often invisible in the budget because nobody planned for it explicitly, and then suddenly you're paying rent and staff for two locations for three months and wondering why costs spiked.

On the cost history side — this is actually where the archiving approach in this system is really valuable. When you archive rather than delete, the old warehouse's record stays intact with its full history. That's not just good for compliance and audit (though it is), it's genuinely useful for setting a realistic budget for the new warehouse. If the old one consistently ran 15-20% over on labour, you need to understand why before you sign off on the new one's budget. Otherwise you're just repeating the same mistake with a new building.

The business unit code reuse is also interesting from a cost perspective. Over time you build up a lineage — you can look back across multiple replacements at the same area and see a real trend. That's something you completely lose if you treat each warehouse as a fresh entity with no connection to what came before.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
