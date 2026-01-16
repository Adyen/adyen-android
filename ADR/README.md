# ADR - Any Decision Record

This directory contains documentation of decision making for this project.

## ADR lifecycle

### 1. Created

The number of an ADR is determined by the previous ADR number + 1. The document should follow the
naming structure: `ADR-XXXX-YYYY.md`, where `XXXX` is the number (should always be 4 numbers to 
create a better overview of the directory, use `0` as padding if needed) and `YYYY` is the title. 
For example: `ADR-0001-some-problem.md`. Newly created ADRs should at least contain a 
**motivation**. Follow the `ADR_TEMPLATE.md` to get started.

### 2. In progress

During the "in progress" phase the ADR is expanded with more details until a final decision can be
made.

### 3. Final decision

Once agreed a final decision can be made. The decision can either be accepted or declined. When an
ADR is accepted development can start.

The status of an ADR must be kept up-to-date by changing the metadata table in the header of the
document. Use the following statuses:

- Created
- In progress
- Accepted
- Declined
- Released in X.X.X
- Superseded by ADR-XXXX
