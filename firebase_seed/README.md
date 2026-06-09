# Arogya Mitra — Plant Database Seed

Adds **~230 medicinal-plant records** to the Firebase Realtime Database node
`drug_to_be_validated`, using the exact field names the app already reads — so
no app code changes are needed for them to appear in search and drug details.

## Why this fixes the "wrong image" problem
Each plant's photo is fetched from its **scientific name's** Wikipedia page
(`…/page/summary/<Binomial>` → `originalimage`). Because we key on the binomial
(e.g. *Saraca asoca*), "Ashoka" always resolves to the **tree**, never
"Ashoka the emperor". Verified: `Saraca asoca → Sita-Ashok flowers`.

## Field mapping (matches DrugDetails.addDrugToDatabase)
| Firebase field      | Value                                   |
|---------------------|-----------------------------------------|
| `Drug Name`         | `Common (Scientific name)`              |
| `scientificName`    | binomial                                |
| `medicinalPlants`   | `Common / Scientific (Family)`          |
| `howToApply`        | medicinal use                           |
| `modeOfPreparation` | preparation method                      |
| `isViable`          | `true`                                  |
| `imageUrls/wiki`    | species-correct Wikipedia image URL     |
| `Aarogya Mitra`     | `Seed Catalogue` (so it's filterable)   |

Records are written at `drug_to_be_validated/seed_001 … seed_229` — stable keys,
so **re-running updates in place** and never duplicates. User-submitted records
(random push-ids) are left untouched.

## Run it
```bash
# 1. Firebase Console → Project Settings → Service accounts
#    → "Generate new private key" → save the file here as:
#        serviceAccountKey.json
# 2. Install + run
npm install
node import_seed.js --dry-run   # preview + resolve sample images, no writes
node import_seed.js             # seed for real (~2-3 min, network-bound)
```

## Fixing existing bad images already in your DB
This importer only writes the `seed_*` records. To correct a wrong image on an
**existing** user record (e.g. the current Ashoka entry), point me at the record
key once you've shared the service-account key, or set its `imageUrls` to the
resolved URL shown by `--dry-run`.

## Files
- `import_seed.js`  — the importer (plant data embedded; resolves images live)
- `plants_seed.json` — generated preview of the records (no images)
- `package.json`     — `firebase-admin` dependency
