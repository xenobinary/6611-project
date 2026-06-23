# D1 Feedback Revisions & D2 Java Implementation Plan

Based on your comments, we have refined the D1 feedback responses and established the architectural plan for the D2 Java implementation.

## 1. Revised Scope Enhancements

We are adjusting the functionalities to ensure applicability for the 21st century without adding unrealistic ABM interactions (like typing emails).

*   **UC-09: Multi-Currency Accounts & Exchange (Bank Client)**
    *   Clients will have sub-accounts for different currencies (e.g., CAD, USD, EUR).
    *   They can transfer funds between their own accounts, which will automatically apply the current exchange rate.
*   **UC-10: Update Exchange Rates & Unlock Cards (System Admin)**
    *   **New Actor: System Administrator.**
    *   The Admin can log into a special maintenance screen to update the static exchange rates and manually unlock user accounts that were locked due to too many incorrect PIN attempts.
*   **UC-11: Ad-hoc Multilingual Support (Bank Client & Admin)**
    *   Instead of just English/French, the UI will be fully localized using configuration/properties files (e.g., `messages_en.properties`, `messages_fr.properties`, `messages_zh.properties`).
    *   Users can switch languages dynamically at any point on the screen using a language toggle button.
*   *(Dropped)*: External e-Transfers are removed as typing emails on an ABM keypad is inconvenient.

## 2. Revised SMART Goal (Aligned with D2)

**Your Comment:** *We haven't learned CC and CF yet. How can we make this goal at the beginning?*
**Correction:** You are entirely correct. Since Deliverable 2 specifically asks us to measure **SLOC** (Problem 5) and a **Readability Metric** (Problem 6), we must tie our D1 SMART goal to these exact D2 concepts!

**New Goal Statement:**
> **Purpose:** To evaluate the source code readability and modularity of the iBank ABM prototype in order to reduce future maintenance effort.
> **Perspective:** Examine structural maintainability from the viewpoint of the development team.
> **Measurable Target:** Ensure that the overall codebase achieves a positive **Buse & Weimer Readability Metric** score, and that 100% of methods are kept concise by not exceeding a maximum of **30 Logical SLOC (Source Lines of Code)** per method.

**Why this works:** It is completely measurable because you will mathematically calculate SLOC and Readability in D2, satisfying the TA's demand for a non-vague goal without jumping ahead to D3 concepts.

---

## 3. D2 Code Structure Plan (Java Implementation)

For Deliverable 4(a) and 4(b), we will build the iBank application using **Java Swing** (as it is standard, robust, and doesn't require complex external build tools like JavaFX might).

### Architecture (Model-View-Controller)
We will strictly follow the OOP paradigm and the MVC pattern to ensure high readability and external/internal reuse.

**1. `models/` (Data & Logic)**
*   `User.java` (Abstract base class for actors)
*   `BankClient.java` (Inherits User; contains a list of Accounts)
*   `SystemAdmin.java` (Inherits User; specific Admin privileges)
*   `Technician.java` (Inherits User; refills physical cash box)
*   `Account.java` (Handles balance, currency type)
*   `ExchangeRateManager.java` (Singleton handling currency conversion rates)
*   `DatabaseManager.java` (Singleton handling SQLite database persistence)
*   `CashBox.java` (Singleton handling physical ABM cash limits)
*   `TransactionRecord.java` (Data class for persisting transaction history)

**2. `views/` (Java Swing GUI)**
*   `MainFrame.java` (The root window containing CardLayout and router logic)
*   `BaseViewPanel.java` (Abstract class for standardizing layout and side buttons)
*   `LoginPanel.java` (Card selection and PIN pad simulation)
*   `ClientDashboardPanel.java` (Shows balances, withdraw, transfer options)
*   `AdminDashboardPanel.java` (Update rates, unlock users)
*   `TechnicianPanel.java` (Cash box refill operations)
*   `NumpadPanel.java` (Reusable numpad component used across screens)
*   `LanguageSelectorPanel.java` (Full-screen overlay to switch languages)
*   `Router.java` (Interface defining navigation methods)

**3. `controllers/` (Action Handlers)**
*   `AuthenticationController.java` (Validates PIN, handles lockouts)
*   `TransactionController.java` (Processes withdrawals, transfers, exchange math)
*   `I18nController.java` (Loads the correct strings from the language config files)

**4. `exceptions/` (Error Handling)**
*   D2 requires explicit support for handling exceptions. We will create custom exceptions:
*   `InsufficientFundsException.java`
*   `AccountLockedException.java`
*   `InvalidAmountException.java`

**5. `resources/` (Configuration Files)**
*   `messages_en.properties` (English strings)
*   `messages_fr.properties` (French strings)
*   `messages_zh.properties` (Chinese strings)

## Open Questions for User Review

> [!IMPORTANT]  
> Are you satisfied with switching the SMART Goal to **Logical SLOC** and **Readability Metric**? This perfectly bridges D1 and D2. 

> [!NOTE]  
> Do you approve of using **Java Swing** and the **MVC architecture** outlined above for the code structure?

If you approve, I will begin writing the actual Java codebase, starting with the core Models, Exceptions, and the I18n Language Controller!
