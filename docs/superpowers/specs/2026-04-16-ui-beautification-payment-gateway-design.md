# Agroषधि — UI Beautification & Mock Payment Gateway Design

**Date:** 2026-04-16  
**Status:** Approved  

---

## 1. Overview

Two parallel deliverables built on the same design language:

1. **UI Beautification** — Full visual refresh of the home screen and all major screens using a Fresh Nature green palette and a Hero + Grid home layout.
2. **Mock Payment Gateway** — A `SubscriptionActivity` with monthly/annual billing toggle, plan summary, UPI/Card/Net Banking tabs, and animated success state. Wired to the existing Transactions footer tab.

---

## 2. Design Language — Fresh Nature

### Colour Palette

| Token | Hex | Usage |
|---|---|---|
| `green_dark` | `#1B5E20` | Toolbar gradient start, hero avatar |
| `green_primary` | `#2E7D32` | Toolbar gradient end, buttons, active states, Identify Leaf tile |
| `green_medium` | `#388E3C` | Carousel card border |
| `green_light` | `#66BB6A` | Hero badge text, price sub |
| `green_100` | `#C8E6C9` | Carousel background, avatar bg |
| `green_50` | `#E8F5E9` | Screen background, hero badge bg, method active bg |
| `green_pale` | `#F1F8E9` | Page background |
| `text_primary` | `#1B5E20` | Headings |
| `text_secondary` | `#757575` | Subtitles, labels |
| `white` | `#FFFFFF` | Cards, tiles, inputs |

Existing pastel service tile colours (`svc_bg_blue`, `svc_bg_purple`, `svc_bg_amber`, `svc_bg_teal`, `svc_bg_pink`) are kept — they sit inside the green framework without clashing.

> **Note:** `svc_bg_green` (`#C8E6C9`) is already defined in `colors.xml` and is identical to `green_100`. All tile references use `svc_bg_green` (not `green_100`) to avoid duplicate token confusion. The `green_100` entry in the palette table above is documentation-only.

### Typography
- Section labels: 9sp, `700`, uppercase, `#757575`, `1dp` letter-spacing
- Card titles: 13sp, `700`, `green_dark`
- Body / sublabels: 10sp, `400`, `text_secondary`
- Tile labels: 8sp, `600`
- Price: 26sp, `800`, `green_dark`

### Elevation & Shape
- Cards: `4dp` elevation, `16dp` corner radius, white background
- Tiles: `2dp` elevation, `12dp` corner radius
- Inputs: `1.5dp` stroke, `10dp` corner radius
- Buttons: `12dp` corner radius, green gradient fill

---

## 3. UI Beautification — Screen-by-screen

### 3.1 Home Screen (`activity_home_dummy.xml` + `HomeDummy.java`)

**Layout: Hero + Grid**

#### Toolbar (replaces current Toolbar)
- Full-bleed `AppBarLayout` + `Toolbar` with `linear-gradient(135deg, #1B5E20 → #2E7D32)`
- Left: leaf icon (30×30dp, `#A5D6A7` rounded square) + "Agroषधि" bold 15sp + "Towards Ethnic उपचार" 9sp `#A5D6A7`
- Right: language icon + profile icon (30×30dp pill buttons, `rgba(255,255,255,0.15)`)
- `SearchView` embedded inside toolbar area, `rgba(255,255,255,0.18)` rounded-rect, placeholder "Search plants, medicines…"
- Bottom padding `28dp` so hero card overlaps

#### Hero Card (NEW)
- White card, `16dp` radius, `4dp` elevation, overlaps toolbar by `14dp` via `marginTop="-14dp"`
- Left: circular avatar with `green_dark → green_light` gradient, leaf emoji
- Centre: "Good morning, [displayName]" 13sp bold `green_dark`; "Aarogya Mitra · Active" 10sp `text_secondary`
- Right: badge showing count of user's `drug_to_be_validated` entries — number 16sp bold `green_primary`, label "Pending" 7sp `text_secondary`
- Badge count fetched via `FirebaseManager.getDrugRef().orderByChild("Aarogya Mitra").equalTo(userName)` and counted in `onResume`. This mirrors the exact query already used in `DrugListActivity.retrieveDrugsForCurrentUser()` — same node path (`drug_to_be_validated`), same child key (`"Aarogya Mitra"`), same `userName = user.getDisplayName()`. If the DB schema changes, both places update together.

#### Quick Services Grid
- Section label "QUICK SERVICES" above
- 3×2 `GridLayout`, `8dp` gaps
- Each tile: white card `12dp` radius `2dp` elevation; 34×34dp pastel icon container (`10dp` radius); 8sp label below
- Tile colours: New User `svc_bg_green`, New Drug `svc_bg_purple`, Status `svc_bg_amber`, Drug List `svc_bg_teal`, Plants `svc_bg_pink`, Identify Leaf `green_primary` (accent, white label + icon)
- Click targets: entire tile `LinearLayout` (not just inner ImageView). The `android:id` for each QS tile moves from the inner `ImageView` to the outer `LinearLayout`. The following IDs are reassigned: `new_user_QS`, `new_drug_QS`, `status_icon_QS`, `drug_list_QS`, `list_of_plants_QS`, `identify_leaf_QS`. In `HomeDummy.java`, the corresponding field types change from `ImageView` to `View` (all six). Footer icon IDs (`users_list_icon`, `add_drug_icon`, `status_icon`, `newsletter_icon`, `transations_icon`) are not reassigned — their click areas are already the full `LinearLayout` cell.

#### Explore Carousel (replaces ViewPager card styling)
- Section label "EXPLORE" above
- `green_50` → `green_100` gradient card, `14dp` radius, `1dp` `green_medium` stroke
- Small uppercase tag, bold title, subtitle — same ViewPager data source, just re-skinned item layout
- Dots indicator stays, reskinned to `green_primary`/`green_100`

#### Footer Nav (reskin only)
- White background, `1dp` `green_50` top border
- Active item: `green_primary` colour + bold label
- No structural change to click listeners

### 3.2 Other Screens (global reskin)

The following changes apply across all activities:
- `colors.xml`: add new green tokens above; keep existing pastel svc tokens
- `themes.xml`: set `colorPrimary = green_primary`, `colorPrimaryDark = green_dark`
- Toolbar background: `green_primary` (all non-home toolbars)
- Button background: `green_primary` gradient (replaces `#FF4081`)
- `activity_drug_list.xml`, `activity_people_list.xml`: toolbar green; empty-state text already wired
- `activity_profile.xml`: avatar circle with green gradient; name/email populated dynamically (already implemented)
- `activity_leaf_prediction.xml`: toolbar already uses `?attr/colorPrimary` — inherits green automatically

---

## 4. Mock Payment Gateway

### 4.1 Architecture

Single activity: `SubscriptionActivity`  
Single layout: `activity_subscription.xml`  
Three visual states managed by explicit `View.VISIBLE` / `View.GONE` toggling on three root `LinearLayout` containers (`layoutPlans`, `layoutPayment`, `layoutSuccess`). `ViewSwitcher` is not used — it only supports two children. `ViewFlipper` would add unnecessary animation overhead for this flow.

| State | Trigger |
|---|---|
| `STATE_PLANS` | Default on launch |
| `STATE_PAYMENT` | User taps "Continue" |
| `STATE_SUCCESS` | User taps "Pay" (mock — no real transaction) |

### 4.2 Entry Point

`TransactionsActivity` replaced by a redirect:  
- On launch, immediately starts `SubscriptionActivity` if user is not subscribed  
- Stores subscription state in `SharedPreferences` key `"is_subscribed"` (boolean)  
- If already subscribed, shows a simple "You're on Pro 🎉" screen with subscription details

### 4.3 Plans State

- Monthly / Annual `ToggleButton` pair in a rounded container
  - Monthly: `₹199/month`
  - Annual: `₹1,499/year` — "Save 37%" badge, pre-selected
- Plan card: `"BEST VALUE"` chip, price, per-month breakdown, 6-feature grid
- "Continue" button → transitions to Payment state

### 4.4 Payment State

Three tab options: **UPI** (default) · **Card** · **Net Banking**

**UPI tab:**
- Text input: UPI ID (e.g. `name@upi`), with inline verify icon
- Quick-tap row: GPay · PhonePe · Paytm (icon + label, tap to pre-fill mock ID)
- "Pay ₹X,XXX Securely" green gradient button

**Card tab:**
- Card number field (auto-formats with spaces every 4 digits)
- Row: Expiry MM/YY · CVV (3 digits, masked)
- Cardholder name field
- Card type detection icon (Visa / Mastercard placeholder)

**Net Banking tab:**
- `Spinner` / dropdown listing: SBI · HDFC · ICICI · Axis · Kotak · Others

**On "Pay" tap (any method):**
1. Button shows `ProgressBar` for 1.5s (simulated network call via `Handler.postDelayed`)
2. Transitions to Success state

### 4.5 Success State

- Full-screen green gradient background
- Animated checkmark (scale-in + fade-in, 400ms)
- "Payment Successful! 🎉"
- Plan name + amount + transaction ID (mock UUID)
- Date + "Valid until [date + 1 year]"
- "Start Using Agroषधि Pro →" button → `finish()` back to home
- Sets `SharedPreferences "is_subscribed" = true`

### 4.6 Manifest

```xml
<activity
    android:name=".SubscriptionActivity"
    android:exported="false" />
```

---

## 5. Files Created / Modified

### New Files
| File | Purpose |
|---|---|
| `SubscriptionActivity.java` | Payment gateway logic |
| `activity_subscription.xml` | Plans + payment + success layout |
| `item_upi_app.xml` | Quick-tap UPI app row item |

### Modified Files
| File | Change |
|---|---|
| `colors.xml` | Add green token set |
| `themes.xml` | Update `colorPrimary`, `colorPrimaryDark` |
| `activity_home_dummy.xml` | Full layout rewrite — Hero + Grid |
| `HomeDummy.java` | Hero card data binding; click targets on outer tile containers |
| `TransactionsActivity.java` | Replace stub with redirect to `SubscriptionActivity` |
| `activity_transactions.xml` | Replace empty state with "Pro subscriber" screen |
| `AndroidManifest.xml` | Register `SubscriptionActivity` |
| `dimens.xml` | Add `corner_card=16dp`, `corner_tile=12dp`, `corner_button=12dp` tokens. File already exists (created in the Tier 4 audit); do not recreate it. |
| `strings.xml` | Add subscription copy strings |

---

## 6. Out of Scope

- Real payment processing (no Razorpay SDK, no server calls)
- Backend subscription validation
- Push notifications for renewal
- Paywall enforcement across activities (future work — SharedPreferences flag set but not checked in other activities yet)

---

## 7. Success Criteria

- Home screen renders the Hero + Grid layout with live pending-drug count
- All 6 Quick Service tiles respond to tap on the full tile area (not just icon)
- `SubscriptionActivity` cycles cleanly through Plans → Payment → Success states
- Annual plan pre-selected; toggle correctly updates price display
- All three payment tabs (UPI / Card / Net Banking) render correct input fields
- Success screen sets `is_subscribed = true` in SharedPreferences
- Build passes `assembleDebug` with no errors or resource warnings
