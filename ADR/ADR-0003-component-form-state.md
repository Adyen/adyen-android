# Component and form state management

| Field         | Value        |
|---------------|--------------|
| Author(s)     | Joseph Reij  |
| Status        | Accepted     |
| Creation date | 2026-09-11   |

## Abstract

Components use unidirectional intent and reducer state management. Components that render forms additionally derive an ordered `FormState` from their component state. The form is the source of truth for visibility, visual order, validity, focus targets, and keyboard actions.

This ADR supersedes ADR-0001, retaining its MVI and reducer decision while replacing its obsolete field-state, focus, and view-state details.

## Motivation

ADR-0001 selected MVI with reducers, but its detailed design described types and responsibilities that no longer exist in v6. It also left form behavior spread across reducers, view-state producers, and composables:

- Layout order came from conditional UI blocks
- Validation focus priority came from property order in reducers
- Keyboard actions were decided per field
- Visibility was represented separately from rendering
- Programmatic focus was stored as a persistent field flag

These independent representations could disagree. A hidden or reordered field could receive the wrong keyboard action or focus priority.

## Solutions

### Keep component-specific state only

Each component could continue deriving layout, focus, validity, and keyboard behavior independently.

**Pros:**

- No shared form abstraction
- Component code remains locally flexible

**Cons:**

- Order and visibility remain duplicated across state and UI
- Focus priority and keyboard behavior can drift from visual order
- The same focus and error rules must be reimplemented per component

### Register fields from the UI

Composable fields could register themselves into a UI-owned form controller.

**Pros:**

- The controller observes what is composed
- Components need no explicit canonical list

**Cons:**

- Composition timing becomes part of state behavior
- Focus and validation decisions become difficult to test without Compose
- The state layer cannot determine keyboard actions before rendering
- Reordering returns to the view layer

### Derive an ordered form from component state

Each component defines its element IDs and derives a `FormState` containing only visible elements in shopper order. Each element carries the validity fact needed by form decisions.

**Pros:**

- One list defines visibility and order
- Validation focus and keyboard actions follow visual order
- Form behavior is testable with pure unit tests
- Component-specific state and element types remain type-safe
- The UI renders the same ordered elements used by state decisions

**Cons:**

- Each component maps IDs to its concrete state and view elements
- Similar element types are repeated across components
- Migration requires coordinated state and UI changes

## Final decision

Use MVI with component-specific intents, reducers, validators, component state, and view-state producers. For form-based components, derive an ordered `FormState` from the component state.

The model follows these rules:

1. `ComponentState` remains the single mutable source of truth.
2. `FormState.elements` contains only visible elements, in shopper order.
3. `FormElementState` carries an ID and validity; it does not duplicate field content.
4. Component-specific view-state producers map the ordered form into renderable elements.
5. Composables render that element list using stable element IDs as keys.
6. `FocusRequest` represents a one-time state-layer request. The UI consumes it and reports completion.
7. Focus gain hides an error unless the request explicitly preserves it; focus loss shows an existing error.
8. Keyboard `Next` or `Done` is derived from the next visible text input in the form.
9. Whole-form and single-element validity are read from `FormState`, not directly from text-input state.
10. Shared core element types are introduced only when multiple components have the same concrete need without weakening exhaustive component mappings.

## Concerns and follow-up actions

- A focus target can leave composition before its effect runs. The request may remain until another request replaces it.
- Non-text invalid elements must eventually support both focus and scrolling; no current v6 component needs this.
- Compose stability of list-based view states should be measured and guarded separately.
- Autofill, accessibility, address forms, and auto-advance remain separate follow-up work.

## Details

The state flow remains:

1. UI dispatches a component intent.
2. The reducer creates the next component state.
3. The validator updates field errors.
4. The component state derives its ordered form.
5. The view-state producer maps form elements to component-specific renderable elements.
6. The UI renders those elements and reports value, focus, and request-consumption intents.
7. Submission reads form validity; invalid submission highlights errors and requests the first invalid visible element.

The form abstraction deliberately does not own field values, labels, transformations, or component-specific rendering data. Those remain on component state and component-specific view elements.
