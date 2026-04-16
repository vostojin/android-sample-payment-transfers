# Sample Payment Transfers //to self ;)

A production-quality **Payment Transfer Service** for a digital banking platform, built with modern Android architecture.

---

## Architecture Overview

The project follows **Clean Architecture** combined with **MVVM**, organized into three strict layers that depend only inward:

```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                 │
│  Composables · ViewModels · UiState · NavGraph      │
├─────────────────────────↓───────────────────────────┤
│                   Domain Layer                      │
│  Use Cases · Domain Models · Repository Interfaces  │
├─────────────────────────↓───────────────────────────┤
│                    Data Layer                       │
│  Room · DAOs · Entities · Repository Impls · DI     │
└─────────────────────────────────────────────────────┘
```

### Why Clean Architecture?

| Concern | Benefit |
|---|---|
| Domain has **zero Android imports** | Business logic is testable without a device/emulator |
| Repository interfaces in domain | Data sources can be swapped (Room → network) without touching use cases or ViewModels |
| Use cases = single responsibility | Each business operation is isolated and independently testable |

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Declarative UI |
| **MVVM** | Presentation pattern |
| **Hilt** | Dependency injection |
| **Room** | Local persistence |
| **Kotlin Coroutines + Flow** | Async & reactive streams |
| **MockK** | Mocking in unit tests |

---

## Module Structure

```
app/src/main/java/com/fintech/payment/
│
├── domain/
│   ├── model/
│   │   ├── ***.kt               # Account, Transfer, TransferRequest, TransferStatus
│   │   └── TransferException.kt # Sealed exception hierarchy
│   ├── repository/
│   │   └── *Repository.kt       # AccountRepository, HistoryRepository, TransferRepository
│   └── usecase/
│       ├── TransferFundsUseCase.kt
│       └── Get*Case.kt          # GetAccountsUseCase, GetHistoryUseCase
│
├── data/
│   ├── local/
│   │   ├── entity/
│   │   │   ├── Entities.kt     # Room @Entity classes
│   │   │   └── Mappers.kt      # Entity ↔ Domain conversion
│   │   ├── dao/
│   │   │   └── ***.kt          # AccountDao, TransferDao
│   │   ├── Converters.kt       # TypeConverters (BigDecimal)
│   │   └── PaymentDatabase.kt  # RoomDatabase + seed data
│   └── repository/
│       ├── *RepositoryImpl.kt  # AccountRepositoryImpl, HistoryRepositoryImpl, MoneyTransferRepositoryImpl 
│
├── di/
│   └── AppModule.kt            # Hilt modules
│
└── presentation/
    ├── common/
    │   ├── Theme.kt            # MaterialTheme, colors, typography
    │   ├── Components.kt       # AccountCard, TransferItem, shared Composables
    │   └── Navigation.kt       # NavHost + BottomNavigationBar
    ├── accounts/
    │   ├── AccountsViewModel.kt
    │   └── AccountsScreen.kt
    ├── transfer/
    │   ├── TransferViewModel.kt # UiState, TransferAction, TransferEvent
    │   └── TransferScreen.kt
    └── history/
        ├── HistoryViewModel.kt
        └── HistoryScreen.kt
```

---

## Core Feature: Fund Transfer Flow

```
User taps "Send Transfer"
        │
        ▼
TransferScreen.onAction(SubmitTransfer)
        │
        ▼
TransferViewModel.submitTransfer()
  ├─ Local validation (amount > 0, accounts differ)
  └─ TransferFundsUseCase(request)
            │
            ▼
      PaymentRepositoryImpl.executeTransfer()
        1. Load source account     → AccountNotFoundException if missing
        2. Load destination account → AccountNotFoundException if missing
        3a. Check source.isActive   → AccountInactiveException if frozen
        3b. Check dest.isActive     → AccountInactiveException if frozen
        3c. Check balance ≥ amount  → InsufficientFundsException if not
        3d. Check currency equal    → InncompatibleCurrencyException if not
        4. Debit source balance
        5. Credit destination balance
        6. Persist account updates (AccountDao.update × 2)
        7. Insert transaction record (TransferDao.insert)
        8. Return Transfer(status=SUCCESS)
            │
            ▼
  Result<Transfer> returned to ViewModel
        │
   ┌────┴────┐
Success    Failure
   │           │
   ▼           ▼
Navigate  Show Snackbar
to History  with error
```

---

## Error Handling

All domain errors are expressed as a **sealed class hierarchy** (`PaymentException`), making error handling exhaustive:

```kotlin
sealed class TransferException(message: String) : Exception(message) {
    data class AccountNotFoundException(val accountId: String)     : TransferException(...)
    data class AccountInactiveException(val accountId: String)     : TransferException(...)
    data class InvalidAmountException(val amount: BigDecimal)      : TransferException(...)
    data class SameAccountTransferException(val accountId: String) : TransferException(...)
    data class IncompatibleCurrencyException(...)                  : TransferException(...)
    data class InsufficientFundsException(...)                     : TransferException(...)
    data class TransferFailedException(override val cause: ...)    : TransferException(...)
}
```

The ViewModel maps each case to a human-readable message without exposing internals to the UI.

---

## Testing

```
app/src/test/
├── domain/usecase/
│   ├── TransferFundsUseCaseTest.kt         # happy path + all failure branches
│   └── TransferViewModelTest.kt            # state changes + event emission via Turbine
└── data/repository/
    └── MoneyTransferRepositoryImplTest.kt  # cases: balance mutations + all exception paths
```

**Run tests:**
```bash
./gradlew test
```

**Coverage highlights:**
- Happy-path transfer with verified balance mutations
- All `TransferException` subtypes triggered and asserted
- ViewModel event channel tested with Turbine (no timing hacks)
- No account/transaction DB calls made when validation fails

---

## Getting Started

**Requirements:** Android Studio / IntelliJ IDEA with Android plugin, JDK 17, API 26+ device/emulator

```bash
git clone <repo-url>
cd PaymentApp
./gradlew assembleDebug          # build
./gradlew test                   # unit tests
./gradlew connectedAndroidTest   # instrumentation tests (device required)
```

The app seeds **5 demo accounts** on the first launch (one is frozen, one has negative balance, one has different currency to demonstrate error handling).

---

## Design Decisions & Trade-offs

**BigDecimal for money** — `Double`/`Float` have rounding errors that are unacceptable for financial data. `BigDecimal` is stored as a plain string in Room via a `TypeConverter`.
> This could be represented by Long instead – keeping track of cents instead of currency values.

**Room over network** — The task specifies an in-app transfer within the same platform. Room provides transactional guarantees without network latency. A real production service would call a backend API and rely on server-side ledger consistency.
> This would be network-first in real use, with Room database as backup/cache for quick data display, but non-functional without real data from the network.

**`withTransaction` note** — For true atomicity, the two `accountDao.updateAccount` calls and the `transactionDao.insertTransaction` are wrapped in `database.withTransaction { }`. This requires injecting the `PaymentDatabase` instance directly into the repository; the current implementation is correct for the scope of this task.

**Hilt over manual DI** — Hilt reduces boilerplate and is the Google-recommended approach. The `@Binds` pattern in `RepositoryModule` keeps the interface → implementation binding explicit and compile-time verified.
