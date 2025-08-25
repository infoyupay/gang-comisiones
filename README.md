# Gang-Comisiones

Desktop app for the **Gang Gaming Company** that allows control and auditory of comissions from banking terminal.  
The system tries to make comissions transparent and comply with peruvian regulations that enforces to inform the end
consumer about said comissions.

---

## 📌 Problem to Solve
Currently, the banking terminal used by Gang Gaming prints tickets with customer paid ammount,
but **does NOT show the comission perceived by the company from the bank.**
This creates difficulties on accountability control and customers transparency.

The app **Gang Comisiones** shall allow:
- Register and compute automatically the comissions for each transaction.
- Print the comission ticket.
- Manage banks, concepts and users.
- Generate accountability and auditory reports.

---

## 🎯 Main Use Cases
(By classified by user role)
- **ROOT**
    - Users management.
    - Global configuration.

- **ADMIN**
    - Management of Banks.
    - Management of concepts. (Fixed or rate)
    - Financial reports.
    - Reversions approval.
    - Movements auditory.

- **CASHIER**
    - Transaction registration.
    - Retrieving and printig of transactions.
    - Reverssions request.
    - Daily report.

---

## 🛠️ Technologies
- **Language:** Java 24 (OpenJDK)
- **UI:** JavaFX 24
- **Persistence:** JPA (EclipseLink) + PostgreSQL
- **Concurrence:** Virtual threads (Project Loom)
- **Logging:** SLF4J + Logback
- **Testing:** JUnit 5 + Mockito

---

## 🚀 How to run
### Pre-requisites
- JDK 24
- PostgreSQL 15+
- Gradle 9.0+

### Steps
```bash
# Clone repo
git clone https://github.com/yupay/gang-comisiones.git
cd gang-comisiones

# Compile
./gradlew build

# Run (while developing).
./gradlew run
```

## Developer Documentation
See [Model Conventions](model/MODEL-CONVENTIONS.md) for details on how domain entities are structured.
