# Model Conventions

This document defines the conventions for entity modeling in **Gang-Comisiones**.
Applies to all **domain model** (`src/main/java/com/yupay/gangcomisiones/model`) classes.

---

## 1. Naming

- Tables use **snake_case** (ej: `audit_log`, `bank`).
- Classes use **PascalCase** (ej: `AuditLog`, `Bank`).
- Fields use **camelCase** (ej: `createdAt`, `conceptType`).
- Sequences named like `sq_<table>_id`.

---

## 2. Lombok Annotations

All entities **MUST** include:

```java
@Getter
@Setter(onMethod_ = {@Deprecated}) // Only in fields, not in id
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "table_name")
public class EntityName {...}
```

### Rules:
- `@Setter` only in editable fields.
- Never use `@Setter` in `id`.
- Use `@Builder` always, facilitates testing and inmutable objects creation.

---

## 3. ID Strategy
- Always `@Id` with `@GeneratedValue(strategy = GenerationType.SEQUENCE)`.
- `@SequenceGenerator` with `allocationSize = 1`.

Example:

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_id_gen")
@SequenceGenerator(name = "bank_id_gen", sequenceName = "sq_bank_id", allocationSize = 1)
@Column(name = "id", nullable = false)
private Integer id;
```
---
## 4. Documentation
Each class and field must be documented with **javadoc**.

Paragraphs must be separated with `<br/>` to avoid IDE warnings.

Field example:

```java
/**
 * Represents the meta-status of the Bank entity.  
 * If false, it means the bank has been deleted, but kept in DB for historical consistency.<br/>
 * Required field (cannot be null).
 */
@Column(name = "active", nullable = false)
@Setter
private Boolean active;
```

---

## 5. Equals & HashCode

### Rules:
- `equals` only compares by `id`.
- If `id` is `null` in any of the two objects, shall return `false`.
- `hashCode` uses `id` if assigned, otherwise `System.identityHashCode(this)`.

Example:

```java
@Override
@Contract(pure = true, value = "null -> false")
public final boolean equals(Object o) {
if (o == this) return true;
if (!(o instanceof Bank bank)) return false;
if (getId() == null || bank.getId() == null) return false;
return Objects.equals(getId(), bank.getId());
}

@Override
public int hashCode() {
return getId() != null ? Objects.hashCode(getId()) : System.identityHashCode(this);
}
```

## 6. Constraints
- Use `@Column(nullable=false)` when applies.
- Additional validations are made with **check constraints** in the database.
- Example: limit a percentage between 0 and 100.

```postgresql
ALTER TABLE commission
ADD CONSTRAINT chk_commission_percentage
CHECK (
(concept_type = 'PERCENTAGE' AND value BETWEEN 0 AND 100)
OR (concept_type = 'AMOUNT')
);
```

---

## 7. Code Snippets
Use `@snippet` in Java $\ge$ 21 when the explanation requires large examples.

Example:

```java
/**
* Value of the commission.
* <br/>
* Can represent either a percentage (0–100) or a fixed monetary amount,
* depending on {@code conceptType}.
*
* {@snippet :
* // Example of percentage commission
* Commission c1 = Commission.builder()
*     .conceptType(ConceptType.PERCENTAGE)
*     .value(5.0)
*     .build();
*
* // Example of fixed amount commission
* Commission c2 = Commission.builder()
*     .conceptType(ConceptType.AMOUNT)
*     .value(50.0)
*     .build();
* }
*/
@Column(name = "value", nullable = false)
private Double value;
```

