# Data Model for Jirani App

## Entities
- User: id, name, contact
- Dispute: id, title, description, parties, status
- Mediation: id, dispute_id, mediator, date, outcome
- Agreement: id, mediation_id, terms, signatures

## Relationships
- User has many Disputes
- Dispute has one Mediation
- Mediation has one Agreement

## Storage
- Local: Room database
- Sync: Custom sync mechanism