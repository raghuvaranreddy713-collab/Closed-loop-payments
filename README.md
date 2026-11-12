# PayAI — Combined Hackathon Demo

**PayAI: Pay + AI Super-App for Merchants & Customers**  
PayPal Hackathon 2026 • Closed-loop demo

This package combines the supplied PayAI concept, the supplied standalone HTML interface, and the supplied Spring Boot service/controller design into one consistent project.

## Project layout

```text
PayAI_Combined_Hackathon_Project/
├── index.html                  # Standalone frontend
├── frontend/index.html         # Frontend mirror for Live Server
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/paypal/closedloop/
│       ├── ClosedLoopApplication.java
│       ├── controller/
│       │   ├── PaymentController.java
│       │   ├── MerchantController.java
│       │   └── RedTeamController.java
│       └── service/
│           ├── RiskAiService.java
│           ├── HabitAiService.java
│           ├── RedTeamAiService.java
│           └── MerchantService.java
└── README.md
```

## Run the frontend

1. Open the project folder in VS Code.
2. Open `frontend/index.html`.
3. Use Live Server, or open the HTML file in Chrome/Edge.
4. The supplied HTML is a standalone interactive prototype. Its displayed balances, transaction outcomes and several analytics are demo/mock data.

## Run the backend

Requirements: Java 17+ and Maven 3.8+.

```bash
cd backend
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

Health check: `GET http://localhost:8080/` returns the backend status. The main API endpoints are listed below.

### API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/payments/create-order` | Validate amount, apply supported demo coupon, create 3-minute demo order token and risk assessment |
| POST | `/api/payments/verify-and-pay` | Demo PIN/step-up verification and simulated capture |
| POST | `/api/payments/risk-check` | Explainable risk assessment |
| POST | `/api/payments/refund` | Returns 501; real PayPal refund is not connected |
| GET | `/api/payments/feed` | Demo transaction feed |
| GET | `/api/payments/habits` | Demo habit history |
| DELETE | `/api/payments/habits` | Clear demo habit history |
| GET | `/api/merchant/summary?period=today` | Merchant summary (`today`, `week`, `month`) |
| GET | `/api/merchant/inventory` | Inventory list |
| POST | `/api/merchant/inventory/decrement` | Decrement a stock item |
| GET | `/api/merchant/coupons` | Published coupons |
| POST | `/api/merchant/coupons` | Publish a coupon |
| GET | `/api/merchant/udhaar` | Demo customer credit ledger |
| POST | `/api/merchant/voice-sale` | Parse a basic voice-sale text and update demo stock |
| POST | `/api/merchant/udhaar/reminder` | Generate a reminder message (does not send it) |
| GET | `/api/ai/red-team/suite` | Run the four deterministic security test cases |
| POST | `/api/ai/red-team/test` | Test a supplied adversarial payload |

### Example requests

Create an order:

```json
{"amount":120,"couponCode":"CHAI20","merchantId":"MERCH-CHAI-01","merchantName":"Chai Point","isTrusted":true}
```

Verify the returned `orderToken`:

```json
{"orderToken":"TKN-...","pin":"1234","biometricToken":"DEMO-BIOMETRIC-OK"}
```

For amounts above ₹10,000 or when the daily cumulative amount crosses ₹25,000, the demo requires a non-empty `biometricToken`. Amounts above ₹50,000 are marked high-value.

## Important honesty and security notes

- This is a **hackathon prototype**, not a production payment system.
- The current backend does **not** call PayPal APIs, create real PayPal orders, capture real money, or issue real refunds. Transaction IDs are clearly marked as demo IDs.
- The demo PIN `1234` is hard-coded for local demonstration only. Do not use it for real accounts.
- The frontend supplied in the attachment is a standalone mock prototype and is not yet wired to these backend APIs. The API contract is provided so the next integration step can connect its buttons and screens.
- Backend state is held in memory. Restarting the server resets the demo data; no database persistence, user login, JWT, WebAuthn, OTP, real QR/NFC, or distributed replay protection is implemented.
- The risk engine is a deterministic, explainable rules engine, not a trained machine-learning model. Red Team results are simulated policy tests, not a formal security certification. Do not claim a 100% security guarantee.
- Web NFC/WebAuthn require browser/device support and secure contexts. The supplied UI labels several interactions as simulations.
- Never place PayPal client secrets or other credentials in frontend code.

## Suggested next implementation phase

1. Wire frontend checkout to `/api/payments/create-order` and `/api/payments/verify-and-pay`.
2. Add PayPal Sandbox OAuth, order creation, approval and capture on the backend.
3. Add persistent storage (MongoDB or PostgreSQL), real authentication, hashed PIN handling and user-specific rolling 24-hour ledgers.
4. Add real WebAuthn/passkey challenge verification and server-side nonce storage.
5. Add tests, rate limits, audit logging, authorization and deployment configuration.


## Demo sign-in accounts

| Account | Login ID | Password |
|---|---|---|
| Customer | `customer@payai.demo` | `Customer@123` |
| Merchant | `merchant@payai.demo` | `Merchant@123` |

Select Customer or Merchant on the sign-in page and enter the matching credentials. The **Use customer login** and **Use merchant login** buttons auto-fill the credentials.

**Security note:** These are hard-coded frontend demo credentials for the hackathon prototype. They are visible in the HTML source and are not suitable for production. Production authentication must be moved to the backend with password hashing and secure sessions/tokens.


## Merchant dashboard layout update

- Shop Summary & Analytics is displayed directly in the Merchant view and is no longer a separate navigation tab.
- The remaining merchant sections are listed vertically: Live POS & Terminal Feed, Stock & Inventory Management, Coupon Builder & AI Optimizer, and Udhaar Khata & Voice Sale.
- Selecting one of those sections displays its existing panel; the Shop Summary & Analytics content remains visible above it.


## New sign-in, marketplace and safe demo

Open `index.html` using VS Code Live Server (`http://127.0.0.1:5500/...`).
- Customer login: `customer@payai.demo` / `Customer@123`
- Merchant login: `merchant@payai.demo` / `Merchant@123`
- Merchant safe demo PIN: `6967`

**Merchant:** Overview displays summary and recent marketplace orders. Sidebar provides POS, inventory, coupon creation, marketplace listing, Udhaar, and a PIN-locked safe.

**Customer:** Home shows quick actions, recent marketplace orders, and the marketplace. Buy Now creates a simulated order, decrements listed stock, and updates the merchant's order history and safe balance.

**Cross-tab updates:** Marketplace products/orders are stored in browser `localStorage` and update between tabs/windows opened on the same origin and browser profile. For a two-role demo, open two tabs on the same Live Server address, sign in as Merchant in one and Customer in the other. This is **not internet-wide real-time sync**; a shared backend/database and WebSocket/SSE are needed for cross-device synchronization.

**Security and payments:** Demo IDs/passwords and safe PIN are in the HTML/JS source. This is a hackathon UI prototype, not real authentication or a secure vault. Marketplace purchases are simulated and **do not process actual PayPal payments**. The safe balance reflects simulated marketplace orders only; original historical figures in other dashboard sections are seeded demo data.


## Coupon activation and customer visibility

- The merchant coupon panel lists coupons belonging to the demo merchant (Chai Point).
- Each coupon has an Activate/Deactivate control.
- Active coupons are displayed in the customer Coupon Wallet and payment coupon picker.
- Inactive coupons are removed from customer-facing coupon lists and cannot be applied.
- Coupon states are stored in browser localStorage and propagated to other tabs on the same origin/browser profile.

## Merchant Safe walkthrough

1. Sign in with the Merchant demo account and choose **My Shop Safe** in the sidebar.
2. Enter the demo safe PIN `6967` and select **Unlock safe**.
3. Review **Demo safe balance**, **Orders received**, and **Products listed**.
4. Scroll to **Shop transaction history** to see each successful marketplace demo order and the amount credited.
5. Review **About your shop** for the demo shop profile.
6. Select **Lock safe** when finished. Logging out also locks the safe.

The safe balance is a simulated marketplace ledger total, not a bank balance or withdrawable PayPal balance. It currently includes purchases placed through this prototype marketplace; it does not reconcile real POS payments, bank transfers, refunds, fees, or settlement. The safe PIN is hard-coded for demonstration and is not secure authentication. Do not enter real financial credentials.

## Merchant shop access code (new)

1. Sign in using the Merchant demo account and open **My Marketplace**.
2. In **Your shop access code**, create a code containing 4–20 letters, numbers, hyphens or underscores, then select **Save shop code**.
3. Share that code with customers.
4. A customer signs in using the Customer account, opens **Shop Marketplace**, enters the code and selects **Open shop**. The merchant’s currently published products and stock are then displayed, with the existing demo Buy now flow.
5. If the merchant changes the code, customers must enter the new code again. Logging out also closes the customer’s opened shop.

The current bundled demo has one merchant account/shop (Chai Point). The shop code and marketplace data are stored in browser localStorage, so this demo works in tabs/windows on the same browser profile and origin. It does not provide cross-device or internet-wide sharing; that requires a shared backend/database and real authentication. The code is a demo storefront gate, not secure authorization.

## PayAI marketplace update
- Merchant marketplace data is now separated by merchant account key in the browser's `payai-shops-v3` registry.
- Each merchant account receives a persistent Shop ID and can set a unique optional access code.
- Customers can open a shop by entering its Shop ID or access code; products and demo orders are associated with the selected shop.
- Merchant revenue, products, orders, and Safe ledger now read from that merchant's own shop record.
- Existing single-demo-shop data is migrated from the earlier localStorage keys on first load.
- This remains a browser-local prototype. Data is shared only between tabs using the same browser profile and origin. Cross-device/public multi-user access requires the Spring Boot backend and a shared database; demo payments are not real payments.

## Multiple merchant registration (new prototype feature)

1. Open the website and select **Merchant** only when signing in to an existing merchant account.
2. For a new merchant, click **Create merchant account** on the sign-in screen.
3. Enter the shop name, email address and a password with at least 8 characters.
4. Register, then sign in using the new merchant email and password.
5. Open **My Marketplace** to copy the automatically generated Shop ID and create a separate customer access code.
6. Sign out, sign in as the customer demo account, open **Shop Marketplace**, and enter that merchant's Shop ID or access code.

Each registered merchant gets a separate local shop record with its own product listings, orders and demo marketplace balance. This is still a browser-local hackathon prototype: account passwords and shop data are stored in localStorage, so do not use real passwords or personal data. Accounts are not shared across different browsers or devices. Production deployment requires server-side authentication, password hashing, a shared database and API authorization.


## Additional marketplace improvements
- Merchant stock watch highlights products with 5 or fewer units.
- Merchants can restock a listed product by 5 units from the listing card.
- Customers can search products in an opened shop and sort by price or name.
- Merchant dashboard greeting uses the registered shop name.

These browser-only demo features use localStorage and are not cross-device or production payment/authentication services.
