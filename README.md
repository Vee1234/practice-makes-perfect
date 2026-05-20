# Digital ID Platform

[![CI](https://github.com/Vee1234/practice-makes-perfect/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/Vee1234/practice-makes-perfect/actions/workflows/maven-ci.yml)

This project implements a hexagonal architecture for a console-based digital identity backend system.

The project follows hexagonal (ports and adapters) architecture. The domain has no dependencies on any adapter or framework.

## FILE STRUCTURE

src/main/java/uk/ac/qmul/digitalid/
├── domain/                          # Core model: DigitalId, Restriction, IdentityStatus, WelfareBand
├── application/
│   ├── port/in/                     # Inbound ports (CreateIdentity, ChangeStatus, VerifyIdentity, …)
│   ├── port/out/                    # Outbound ports (DigitalIdRepository, AuditSink, AuditQueryPort)
│   ├── service/management/          # DigitalIdManagementService — identity lifecycle operations
│   ├── service/consumption/         # DigitalIdConsumptionService — identity verification
│   └── auth/                        # AuthorisationService, Organisation, OrganisationRole
├── adapter/in/
│   ├── console/                     # ConsoleCommand implementations and CLI runner
│   └── portal/                      # Organisation-specific portals and eligibility policy rules
└── adapter/out/
├── persistence/                 # InMemoryDigitalIdRepository
└── audit/                       # InMemoryAuditSink

## Main components
The portal functionalities were determined by creating [User Stories](./UserStories.md).

┌───────────┬──────┐
│ Component │ Role │
├───────────┼──────┤
│ DigitalIdManagementService  │ Handles identity lifecycle: create, change status, add restrictions, set welfare band │
├─────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────┤
│ DigitalIdConsumptionService │ Handles verification requests from consuming organisations                            │
├─────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────┤
│ EmployerPortal              │ Right-to-work checks — verifies identity is active                                    │
├─────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────┤
│ BankPortal                  │ KYC checks — verifies status and absence of financial review restrictions             │
├─────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────────┤
│ DrivingLicencePortal        │ Licence eligibility — verifies status, minimum age, and absence of driving bans       │
├─────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────────────────────┤
│ WelfarePortal               │ Benefit eligibility — verifies status, welfare band, and absence of welfare review restrictions │
├─────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────────────────────┤
│ AuditorPortal               │ Provides audit event summaries to the auditing organisation                                     │
├─────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────────────────────┤
│ CompositeEligibilityPolicy  │ Composes EligibilityRule instances; each rule contributes a failing reason code                 │
├─────────────────────────────┼─────────────────────────────────────────────────────────────────────────────────────────────────┤
│ InMemoryDigitalIdRepository │ In-memory persistence (no external database required)                                           │
└─────────────────────────────┴─────────────────────────────────────────────────────────────────────────────────────────────────┘



## Agile, Version Control & Git
The project is developed using an agile approach, with iterative development and continuous integration. Git is used for version control, with a branching strategy that includes feature branches, a main branch for stable releases, and pull requests for code reviews.

A [kanban board was] (./KanbanBoard.md) used to manage priorities and track progress, with tasks being created as issues that were solved using pull requests.

Pull requests were accompanied by descriptions of the changes made, following a template.

Branches, which allowed for experimentation and code isolation, and commits were named meaningfully, consistently and frequently.