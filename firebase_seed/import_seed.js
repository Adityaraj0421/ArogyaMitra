#!/usr/bin/env node
/*
 * Arogya Mitra — medicinal plant seed importer
 * ============================================
 * Pushes 200+ curated medicinal-plant records into the `drug_to_be_validated`
 * node of the Firebase Realtime Database, using the EXACT field names the app
 * already reads (see DrugDetails.addDrugToDatabase / DrugDetailsActivity).
 *
 * KEY FIX FOR WRONG IMAGES:
 *   Each plant's photo is resolved from its *scientific name's* Wikipedia page
 *   (REST summary API -> thumbnail.originalimage). Keying on the binomial means
 *   "Ashoka" resolves to Saraca asoca (the tree), never "Ashoka the emperor".
 *
 * IDEMPOTENT:
 *   Records are written at drug_to_be_validated/<_seedId> (a stable key like
 *   "seed_001"), so re-running updates in place instead of creating duplicates.
 *   User-submitted records (random push-ids) are never touched.
 *
 * USAGE:
 *   1) Firebase Console -> Project Settings -> Service accounts
 *        -> Generate new private key  ->  save as ./serviceAccountKey.json
 *   2) npm install
 *   3) node import_seed.js --dry-run     # preview, no writes
 *      node import_seed.js               # actually seed
 */
const fs = require('fs');
const path = require('path');
const https = require('https');

const DB_URL = 'https://vibhu-project-688eb-default-rtdb.firebaseio.com';
const DRY_RUN = process.argv.includes('--dry-run');
const KEY_PATH = path.join(__dirname, 'serviceAccountKey.json');

// [common, scientific, family, medicinal use, mode of preparation]
const PLANTS = [
  ["Rasna","Alpinia galanga","Zingiberaceae","Rheumatism, respiratory ailments and digestion.","Rhizome decoction or powder with warm water."],
  ["Arive-Dantu","Amaranthus viridis","Amaranthaceae","Inflammation, scurvy and mild laxative.","Leaves cooked or juiced."],
  ["Jackfruit","Artocarpus heterophyllus","Moraceae","Wounds, abscesses; fruit nutritive.","Leaf paste applied; decoction taken."],
  ["Neem","Azadirachta indica","Meliaceae","Skin diseases, blood purifier, fever; antibacterial.","Leaf paste/decoction or neem oil."],
  ["Basale","Basella alba","Basellaceae","Ulcers, constipation; cooling.","Leaves cooked or as paste."],
  ["Indian Mustard","Brassica juncea","Brassicaceae","Muscle pain and congestion.","Seed paste poultice; oil massaged."],
  ["Karanda","Carissa carandas","Apocynaceae","Fever, anaemia, digestion.","Fruit eaten; root decoction."],
  ["Lemon","Citrus limon","Rutaceae","Scurvy, indigestion, nausea; antiseptic.","Juice with water or honey."],
  ["Roxburgh Fig","Ficus auriculata","Moraceae","Diarrhoea and dysentery.","Bark decoction taken."],
  ["Peepal","Ficus religiosa","Moraceae","Skin diseases, asthma, diabetes.","Bark decoction; leaf paste."],
  ["Hibiscus","Hibiscus rosa-sinensis","Malvaceae","Hair tonic, menstrual disorders.","Flower/leaf paste applied."],
  ["Jasmine","Jasminum officinale","Oleaceae","Mouth ulcers and skin.","Leaf chewed or paste applied."],
  ["Mango","Mangifera indica","Anacardiaceae","Diarrhoea, diabetes, gum health.","Leaf/bark decoction."],
  ["Mint","Mentha arvensis","Lamiaceae","Indigestion, nausea, cold, headache.","Leaves as tea or juiced."],
  ["Drumstick","Moringa oleifera","Moringaceae","Anaemia, inflammation, lactation, BP.","Leaves cooked or powdered."],
  ["Jamaica Cherry","Muntingia calabura","Muntingiaceae","Headache, gastric pain; antioxidant.","Leaf infusion as tea."],
  ["Curry Leaf","Murraya koenigii","Rutaceae","Digestion, diabetes, hair, nausea.","Fresh leaves eaten or juiced."],
  ["Oleander","Nerium oleander","Apocynaceae","Skin (external); CAUTION highly toxic.","Leaf paste, external only, under guidance."],
  ["Parijata","Nyctanthes arbor-tristis","Oleaceae","Fever, sciatica, arthritis, cough.","Leaf decoction taken."],
  ["Tulsi","Ocimum tenuiflorum","Lamiaceae","Immunity, cough, cold, fever, stress.","Leaves chewed, as tea, or with honey."],
  ["Betel","Piper betle","Piperaceae","Cough, bad breath, wounds; antiseptic.","Leaf chewed or warmed and applied."],
  ["Mexican Mint","Plectranthus amboinicus","Lamiaceae","Cough, cold, sore throat, indigestion.","Leaf juice with honey."],
  ["Indian Beech","Pongamia pinnata","Fabaceae","Skin diseases, rheumatism, wounds.","Karanja oil; leaf poultice."],
  ["Guava","Psidium guajava","Myrtaceae","Diarrhoea, oral health, diabetes.","Tender leaves chewed or decocted."],
  ["Pomegranate","Punica granatum","Lythraceae","Diarrhoea; anaemia.","Rind decoction; fruit/juice."],
  ["Sandalwood","Santalum album","Santalaceae","Skin, acne, burning urination.","Bark paste applied."],
  ["Jamun","Syzygium cumini","Myrtaceae","Diabetes and diarrhoea.","Seed powder; bark decoction."],
  ["Rose Apple","Syzygium jambos","Myrtaceae","Diabetes, diarrhoea, fever.","Bark/seed decoction."],
  ["Crape Jasmine","Tabernaemontana divaricata","Apocynaceae","Eye, skin, toothache.","Root paste/latex external."],
  ["Fenugreek","Trigonella foenum-graecum","Fabaceae","Diabetes, lactation, digestion.","Soaked seeds eaten; powder."],
  ["Ashoka","Saraca asoca","Fabaceae","Menstrual disorders and uterine health.","Bark decoction taken."],
  ["Ashwagandha","Withania somnifera","Solanaceae","Stress, vigour, sleep, immunity.","Root powder with warm milk."],
  ["Brahmi","Bacopa monnieri","Plantaginaceae","Memory, cognition, anxiety.","Whole-plant juice or powder."],
  ["Shatavari","Asparagus racemosus","Asparagaceae","Female reproductive tonic, lactation.","Root powder with milk."],
  ["Amla","Phyllanthus emblica","Phyllanthaceae","Immunity, hair, digestion; vit-C.","Fruit raw, juiced or powdered."],
  ["Haritaki","Terminalia chebula","Combretaceae","Digestive, laxative, rejuvenative.","Fruit powder with warm water."],
  ["Bibhitaki","Terminalia bellirica","Combretaceae","Respiratory and digestive tonic.","Fruit powder with warm water."],
  ["Arjuna","Terminalia arjuna","Combretaceae","Heart health and BP.","Bark decoction or powder with milk."],
  ["Giloy","Tinospora cordifolia","Menispermaceae","Fever, diabetes, immunity.","Stem decoction or juice."],
  ["Kalmegh","Andrographis paniculata","Acanthaceae","Liver, fever, immunity.","Leaf decoction or powder."],
  ["Bhringraj","Eclipta prostrata","Asteraceae","Hair growth, liver, skin.","Leaf juice applied or taken."],
  ["Gokshura","Tribulus terrestris","Zygophyllaceae","Urinary tract, kidney stones, vitality.","Fruit powder with water."],
  ["Punarnava","Boerhavia diffusa","Nyctaginaceae","Oedema, kidney, liver; diuretic.","Whole-plant decoction."],
  ["Vasaka","Justicia adhatoda","Acanthaceae","Cough, asthma, bronchitis.","Leaf juice/decoction with honey."],
  ["Manjistha","Rubia cordifolia","Rubiaceae","Skin diseases, complexion; blood purifier.","Root powder; paste applied."],
  ["Kutki","Picrorhiza kurroa","Plantaginaceae","Liver and fever; bitter.","Rhizome powder taken."],
  ["Bael","Aegle marmelos","Rutaceae","Diarrhoea and dysentery.","Unripe-fruit pulp decoction."],
  ["Shankhpushpi","Convolvulus pluricaulis","Convolvulaceae","Memory and anxiety; brain tonic.","Whole-plant syrup or powder."],
  ["Bhumi Amla","Phyllanthus niruri","Phyllanthaceae","Jaundice, liver, kidney stones.","Whole-plant decoction."],
  ["Kantakari","Solanum virginianum","Solanaceae","Cough, asthma, congestion.","Whole-plant decoction."],
  ["Pippali","Piper longum","Piperaceae","Respiratory and digestive stimulant.","Fruit powder with honey."],
  ["Black Pepper","Piper nigrum","Piperaceae","Digestion; cough and cold.","Powder with honey or in food."],
  ["Ginger","Zingiber officinale","Zingiberaceae","Digestion, nausea, inflammation.","Rhizome decoction or powder."],
  ["Turmeric","Curcuma longa","Zingiberaceae","Anti-inflammatory, antiseptic, wounds.","Powder with milk; paste applied."],
  ["Garlic","Allium sativum","Amaryllidaceae","Cholesterol, BP, antimicrobial.","Cloves raw or in food."],
  ["Kalonji","Nigella sativa","Ranunculaceae","Immunity, digestion, respiratory.","Seeds eaten or oil taken."],
  ["Fennel","Foeniculum vulgare","Apiaceae","Bloating and indigestion.","Seeds chewed or infused."],
  ["Cumin","Cuminum cyminum","Apiaceae","Digestive and carminative.","Seeds infused or in food."],
  ["Coriander","Coriandrum sativum","Apiaceae","Acidity and urinary heat; cooling.","Seed/leaf infusion."],
  ["Ajwain","Trachyspermum ammi","Apiaceae","Colic, gas, indigestion.","Seeds with warm water/salt."],
  ["Nut Grass","Cyperus rotundus","Cyperaceae","Digestion, fever, lactation.","Tuber decoction taken."],
  ["Vidanga","Embelia ribes","Primulaceae","Intestinal worms; anthelmintic.","Fruit powder with honey."],
  ["Chitrak","Plumbago zeylanica","Plumbaginaceae","Digestive stimulant; CAUTION irritant.","Trace root under guidance."],
  ["Daruharidra","Berberis aristata","Berberidaceae","Eyes, skin, diabetes; antimicrobial.","Stem decoction; paste applied."],
  ["Anantmool","Hemidesmus indicus","Apocynaceae","Cooling blood purifier.","Root decoction taken."],
  ["Makoy","Solanum nigrum","Solanaceae","Liver disorders, oedema, skin.","Whole-plant decoction."],
  ["Apamarga","Achyranthes aspera","Amaranthaceae","Piles, asthma, skin; diuretic.","Whole-plant decoction; ash."],
  ["Aloe Vera","Aloe vera","Asphodelaceae","Skin, burns, digestion, laxative.","Gel applied; pulp ingested."],
  ["Lemongrass","Cymbopogon citratus","Poaceae","Fever, digestion; calming tea.","Leaves infused as tea."],
  ["Ram Tulsi","Ocimum gratissimum","Lamiaceae","Cough, cold, fever; antiseptic.","Leaves infused or chewed."],
  ["Stevia","Stevia rebaudiana","Asteraceae","Natural sweetener; diabetes-friendly.","Leaves dried and powdered."],
  ["Insulin Plant","Costus igneus","Costaceae","Traditionally lowers blood sugar.","One leaf chewed daily."],
  ["Bermuda Grass","Cynodon dactylon","Poaceae","Bleeding, skin, acidity; cooling.","Whole-plant juice."],
  ["Castor","Ricinus communis","Euphorbiaceae","Laxative; joint pain.","Oil taken; warm leaf applied."],
  ["Calotropis","Calotropis gigantea","Apocynaceae","Skin and joints; CAUTION toxic latex.","External only under guidance."],
  ["Ber","Ziziphus mauritiana","Rhamnaceae","Digestion and skin.","Leaf paste; fruit eaten."],
  ["Sweet Flag","Acorus calamus","Acoraceae","Speech, memory, digestion; CAUTION dose.","Trace rhizome powder."],
  ["Liquorice","Glycyrrhiza glabra","Fabaceae","Throat, cough, ulcers; soothing.","Root powder with honey."],
  ["Country Mallow","Sida cordifolia","Malvaceae","Joints and nerves; strengthening.","Root decoction taken."],
  ["Indian Pennywort","Centella asiatica","Apiaceae","Brain tonic, skin, wounds.","Leaf juice; paste applied."],
  ["Henna","Lawsonia inermis","Lythraceae","Skin, hair, headache; cooling.","Leaf paste applied."],
  ["Marigold","Tagetes erecta","Asteraceae","Wounds and skin; antiseptic.","Flower/leaf paste applied."],
  ["Periwinkle","Catharanthus roseus","Apocynaceae","Traditionally for diabetes.","Leaf decoction under guidance."],
  ["Sacred Lotus","Nelumbo nucifera","Nelumbonaceae","Diarrhoea, bleeding, heart; cooling.","Seeds, rhizome and petals."],
  ["Indian Borage","Coleus aromaticus","Lamiaceae","Cough, cold, indigestion.","Leaf juice with honey."],
  ["Velvet Bean","Mucuna pruriens","Fabaceae","Parkinsonism, vigour; nervine.","Seed powder with milk."],
  ["Vijaysar","Pterocarpus marsupium","Fabaceae","Anti-diabetic heartwood.","Water stored in wood, drunk."],
  ["Nirgundi","Vitex negundo","Lamiaceae","Joint pain, fever, inflammation.","Leaf decoction; warm leaves."],
  ["Indian Snakeroot","Rauvolfia serpentina","Apocynaceae","Hypertension, insomnia; CAUTION potent.","Root powder under guidance."],
  ["Cluster Fig","Ficus racemosa","Moraceae","Diabetes, diarrhoea, skin.","Bark/fruit decoction."],
  ["Banyan","Ficus benghalensis","Moraceae","Diabetes and skin; astringent.","Bark decoction; latex applied."],
  ["Indian Laburnum","Cassia fistula","Fabaceae","Mild laxative; skin diseases.","Fruit-pulp decoction."],
  ["Senna","Senna alexandrina","Fabaceae","Constipation; strong laxative.","Leaf/pod infusion."],
  ["Wood Apple","Limonia acidissima","Rutaceae","Diarrhoea and dysentery.","Fruit pulp eaten/decocted."],
  ["Bitter Gourd","Momordica charantia","Cucurbitaceae","Blood sugar control; skin.","Fruit juice taken."],
  ["Ivy Gourd","Coccinia grandis","Cucurbitaceae","Anti-diabetic leaves and fruit.","Leaf juice or cooked fruit."],
  ["Snake Gourd","Trichosanthes cucumerina","Cucurbitaceae","Laxative; fever.","Fruit/leaf decoction."],
  ["Pointed Gourd","Trichosanthes dioica","Cucurbitaceae","Digestion; blood purifier.","Fruit cooked; leaf juice."],
  ["Ridge Gourd","Luffa acutangula","Cucurbitaceae","Jaundice; cooling laxative.","Fruit cooked; juice."],
  ["Ash Gourd","Benincasa hispida","Cucurbitaceae","Brain tonic; cooling.","Fruit juice or pulp."],
  ["Indian Valerian","Valeriana wallichii","Caprifoliaceae","Insomnia and anxiety; sedative.","Rhizome powder at night."],
  ["Shallaki","Boswellia serrata","Burseraceae","Arthritis; anti-inflammatory.","Gum resin as tablet/powder."],
  ["Guggul","Commiphora wightii","Burseraceae","Cholesterol, arthritis, obesity.","Purified gum resin."],
  ["Myrrh","Commiphora myrrha","Burseraceae","Gums, wounds, menses; antiseptic.","Resin gargle or applied."],
  ["Heart-leaved Moonseed","Tinospora crispa","Menispermaceae","Fever, diabetes, immunity.","Stem decoction taken."],
  ["Asthma Plant","Euphorbia hirta","Euphorbiaceae","Asthma, cough, dysentery.","Whole-plant decoction."],
  ["Indian Acalypha","Acalypha indica","Euphorbiaceae","Cough, skin; emetic.","Leaf juice/paste."],
  ["Goat Weed","Ageratum conyzoides","Asteraceae","Wounds, cuts, fever.","Leaf juice applied/taken."],
  ["Tridax Daisy","Tridax procumbens","Asteraceae","Wounds and hair; styptic.","Leaf juice applied."],
  ["Indian Heliotrope","Heliotropium indicum","Boraginaceae","Wounds, ulcers, stings.","Leaf paste applied."],
  ["Climbing Staff Tree","Celastrus paniculatus","Celastraceae","Memory; brain tonic.","Seed oil taken."],
  ["Noni","Morinda citrifolia","Rubiaceae","Immunity, joints, digestion.","Fruit juice taken."],
  ["False Daisy","Wedelia chinensis","Asteraceae","Hair, liver, skin.","Leaf juice applied/taken."],
  ["Balloon Vine","Cardiospermum halicacabum","Sapindaceae","Joint pain, rheumatism, skin.","Leaf paste; oil massaged."],
  ["Wild Turmeric","Curcuma aromatica","Zingiberaceae","Skin, complexion, bruises.","Rhizome paste applied."],
  ["Mango Ginger","Curcuma amada","Zingiberaceae","Digestion; anti-inflammatory.","Rhizome paste/pickle."],
  ["Cardamom","Elettaria cardamomum","Zingiberaceae","Digestion, nausea, breath.","Seeds chewed or infused."],
  ["Black Cardamom","Amomum subulatum","Zingiberaceae","Cough, cold, digestion.","Seeds in decoction."],
  ["Indian Sorrel","Oxalis corniculata","Oxalidaceae","Acidity, scurvy, skin; cooling.","Leaf juice taken/applied."],
  ["Indian Trumpet Tree","Oroxylum indicum","Bignoniaceae","Respiratory, digestive, joints.","Bark decoction taken."],
  ["Gambhari","Gmelina arborea","Lamiaceae","Fever, digestion; tonic.","Bark/root decoction."],
  ["Shalparni","Desmodium gangeticum","Fabaceae","Fever; nervine (Dashamula).","Root decoction taken."],
  ["Bitter Indrajao","Holarrhena pubescens","Apocynaceae","Dysentery and diarrhoea.","Bark/seed decoction."],
  ["Sweet Indrajao","Wrightia tinctoria","Apocynaceae","Psoriasis and skin diseases.","Leaf/seed oil applied."],
  ["Kamala","Mallotus philippensis","Euphorbiaceae","Intestinal worms; anthelmintic.","Fruit-gland powder."],
  ["Soapnut","Sapindus mukorossi","Sapindaceae","Hair cleanser; skin.","Fruit decoction as wash."],
  ["Shikakai","Senegalia rugata","Fabaceae","Hair wash and dandruff.","Pod powder as wash."],
  ["Indian Almond","Terminalia catappa","Combretaceae","Skin and dysentery.","Leaf decoction taken/applied."],
  ["Crepe Ginger","Cheilocostus speciosus","Costaceae","Diabetes, fever, skin.","Rhizome decoction."],
  ["Elephant Foot Yam","Amorphophallus paeoniifolius","Araceae","Piles and digestion.","Tuber cooked and eaten."],
  ["Country Mallow Flower","Abutilon indicum","Malvaceae","Cough, piles, joints; demulcent.","Leaf/seed decoction."],
  ["Bala","Sida rhombifolia","Malvaceae","Joints; strengthening.","Root powder taken."],
  ["Spikenard","Nardostachys jatamansi","Caprifoliaceae","Insomnia and stress; nervine.","Rhizome powder/oil."],
  ["Chirata","Swertia chirayita","Gentianaceae","Fever, liver, diabetes; bitter.","Whole-plant decoction."],
  ["Mugwort","Artemisia vulgaris","Asteraceae","Menstrual and digestive tonic.","Leaf infusion taken."],
  ["Akarkara","Anacyclus pyrethrum","Asteraceae","Toothache; nerve tonic.","Root powder applied/taken."],
  ["Safflower","Carthamus tinctorius","Asteraceae","Skin, heart, menses.","Seed oil and flower."],
  ["Indigo","Indigofera tinctoria","Fabaceae","Hair, liver; antidote.","Leaf paste/decoction."],
  ["Country Cotton","Gossypium herbaceum","Malvaceae","Lactation and menses.","Seed/root decoction."],
  ["Yellow-berried Nightshade","Solanum xanthocarpum","Solanaceae","Cough, asthma, fever.","Whole-plant decoction."],
  ["Turkey Berry","Solanum torvum","Solanaceae","Digestion and liver.","Fruit cooked or decocted."],
  ["Climbing Brinjal","Solanum trilobatum","Solanaceae","Cough and asthma; respiratory.","Leaf/fruit decoction."],
  ["Hairy Fig","Ficus hispida","Moraceae","Diabetes, ulcers, lactation.","Fruit/leaf decoction."],
  ["Persian Lilac","Melia azedarach","Meliaceae","Skin, worms, fever; CAUTION fruit toxic.","Leaf decoction; external."],
  ["Marking Nut","Semecarpus anacardium","Anacardiaceae","Joint pain; CAUTION caustic.","Purified form under guidance."],
  ["Indian Cherry","Cordia dichotoma","Boraginaceae","Cough, urinary, skin.","Fruit/bark decoction."],
  ["Portia Tree","Thespesia populnea","Malvaceae","Skin diseases; astringent.","Bark/fruit paste applied."],
  ["Screw Tree","Helicteres isora","Malvaceae","Diabetes, diarrhoea, colic.","Fruit decoction taken."],
  ["Country Fig Bark","Ficus virens","Moraceae","Ulcers and skin; astringent.","Bark decoction taken."],
  ["Indian Ash Tree","Lannea coromandelica","Anacardiaceae","Wounds, ulcers, sprains.","Bark paste applied."],
  ["Holy Fruit Tree","Crataeva nurvala","Capparaceae","Kidney/bladder stones; urinary.","Bark decoction taken."],
  ["Malabar Nut White","Justicia gendarussa","Acanthaceae","Rheumatism and fever.","Leaf decoction; warm applied."],
  ["Country Borage Root","Plectranthus barbatus","Lamiaceae","Heart and asthma; forskolin.","Root decoction under guidance."],
  ["Indian Madder Climber","Rubia manjith","Rubiaceae","Complexion; blood purifier.","Root powder/paste."],
  ["Asparagus Wild","Asparagus gonoclados","Asparagaceae","Reproductive tonic.","Root powder with milk."],
  ["Indian Birch","Betula utilis","Betulaceae","Skin and ear; antiseptic.","Bark decoction; applied."],
  ["Indian Gooseberry Leaf","Phyllanthus acidus","Phyllanthaceae","Liver tonic; cough.","Leaf/fruit decoction."],
  ["Five-leaved Chaste","Vitex trifolia","Lamiaceae","Headache, fever, joint pain.","Leaf decoction; warm applied."],
  ["Indian Coral Tree","Erythrina variegata","Fabaceae","Earache, joints, worms.","Leaf juice; bark decoction."],
  ["Caltrops Big","Pedalium murex","Pedaliaceae","Urinary and reproductive tonic.","Fruit decoction taken."],
  ["Sicklepod","Senna tora","Fabaceae","Ringworm and skin.","Seed paste applied."],
  ["Coffee Senna","Senna occidentalis","Fabaceae","Liver, skin; purgative.","Seed/leaf decoction."],
  ["Indian Squill","Drimia indica","Asparagaceae","Expectorant; CAUTION glycosides.","Under guidance only."],
  ["Sweet Basil","Ocimum basilicum","Lamiaceae","Cold, cough, digestion.","Leaves infused or chewed."],
  ["Camphor Basil","Ocimum kilimandscharicum","Lamiaceae","Cold, cough; insect repellent.","Leaf infusion or oil."],
  ["Wood Sorrel","Oxalis debilis","Oxalidaceae","Indigestion; cooling.","Leaf juice taken."],
  ["Indian Stinging Nettle","Tragia involucrata","Euphorbiaceae","Joint pain and skin.","Root decoction under guidance."],
  ["Wild Asparagus Root","Asparagus adscendens","Asparagaceae","Strengthening tonic (safed musli).","Root powder with milk."],
  ["Safed Musli","Chlorophytum borivilianum","Asparagaceae","Vitality and strength; tonic.","Tuber powder with milk."],
  ["Kapikacchu Leaf","Mucuna pruriens (leaf)","Fabaceae","Nervine and tonic.","Leaf decoction taken."],
  ["Indian Pellitory Root","Anacyclus pyrethrum (root)","Asteraceae","Nerve tonic; toothache.","Root powder used."],
  ["Country Indigo Leaf","Indigofera aspalathoides","Fabaceae","Skin diseases and ulcers.","Leaf decoction/paste."],
  ["Devil's Cotton","Abroma augusta","Malvaceae","Menstrual disorders; uterine.","Root-bark decoction."],
  ["Indian Tulip Leaf","Thespesia populnea (leaf)","Malvaceae","Wounds and skin.","Leaf paste applied."],
  ["Holy Thorn","Balanites aegyptiaca","Zygophyllaceae","Skin, worms; purgative.","Fruit/bark decoction."],
  ["Indian Privet","Clerodendrum inerme","Lamiaceae","Skin, fever, rheumatism.","Leaf decoction; applied."],
  ["Glory Bower","Clerodendrum infortunatum","Lamiaceae","Worms, skin, fever.","Leaf juice taken."],
  ["Bharangi","Clerodendrum serratum","Lamiaceae","Asthma and bronchitis; respiratory.","Root decoction taken."],
  ["Indian Beech Flower","Pongamia pinnata (flower)","Fabaceae","Diabetes; cooling.","Flower decoction."],
  ["White Murdah","Terminalia arjuna (leaf)","Combretaceae","Heart tonic adjunct.","Leaf decoction."],
  ["Beleric Leaf","Terminalia bellirica (leaf)","Combretaceae","Respiratory and digestive.","Leaf decoction."],
  ["Patala","Stereospermum suaveolens","Bignoniaceae","Fever and inflammation.","Bark decoction taken."],
  ["Agnimantha","Premna serratifolia","Lamiaceae","Oedema, inflammation, digestion.","Root decoction."],
  ["Prishnaparni","Uraria picta","Fabaceae","Fever and bleeding (Dashamula).","Root decoction."],
  ["Brihati","Solanum anguivi","Solanaceae","Cough, asthma, digestion.","Root decoction."],
  ["Cork Tree","Millingtonia hortensis","Bignoniaceae","Sinus and cough; tonic.","Flower/leaf used."],
  ["Country Mallow Tonic","Sida acuta","Malvaceae","Fever and rheumatism; tonic.","Root decoction."],
  ["Arrowroot","Curcuma angustifolia","Zingiberaceae","Convalescence; demulcent.","Starch with milk."],
  ["Zedoary","Curcuma zedoaria","Zingiberaceae","Digestion and cold.","Rhizome powder."],
  ["Galangal Lesser","Alpinia officinarum","Zingiberaceae","Digestion; respiratory.","Rhizome decoction."],
  ["Indian Long Pepper Root","Piper longum (root)","Piperaceae","Respiratory and digestive.","Root powder with honey."],
  ["Sweet Root Wild","Hemidesmus indicus (root)","Apocynaceae","Cooling blood purifier; appetiser.","Root infusion."],
  ["Indian Sarsaparilla Climber","Ichnocarpus frutescens","Apocynaceae","Cooling diuretic; blood purifier.","Root decoction."],
  ["Wild Yam","Dioscorea bulbifera","Dioscoreaceae","Piles and dysentery; CAUTION raw toxic.","Cooked tuber under guidance."],
  ["Indian Kudzu","Pueraria tuberosa","Fabaceae","Rejuvenative and tonic (vidari).","Tuber powder with milk."],
  ["White Dammar","Vateria indica","Dipterocarpaceae","Skin, wounds; resin antiseptic.","Resin applied."],
  ["Soapberry South","Sapindus trifoliatus","Sapindaceae","Migraine; cleansing.","Fruit decoction used."],
  ["Country Almond Leaf","Terminalia catappa (leaf)","Combretaceae","Skin and dysentery.","Leaf decoction."],
  ["Indian Olibanum Leaf","Boswellia ovalifoliolata","Burseraceae","Ulcers; anti-inflammatory.","Bark decoction."],
  ["Pterocarpus Leaf","Pterocarpus marsupium (leaf)","Fabaceae","Diabetes adjunct.","Leaf decoction."],
  ["Indian Frankincense Leaf","Boswellia serrata (leaf)","Burseraceae","Anti-inflammatory.","Leaf decoction."],
  ["Country Fig Latex","Ficus religiosa (latex)","Moraceae","Warts and cracked skin.","Latex applied externally."],
  ["Indian Birthwort","Aristolochia indica","Aristolochiaceae","Traditionally snakebite; CAUTION toxic.","Under strict guidance only."],
  ["Bay Leaf","Cinnamomum tamala","Lauraceae","Digestion, cold, diabetes.","Leaves in decoction/food."],
  ["Cinnamon","Cinnamomum verum","Lauraceae","Digestion, cold, blood sugar.","Bark powder or decoction."],
  ["Camphor Tree","Cinnamomum camphora","Lauraceae","Cold, pain; CAUTION dose.","Trace camphor; oil applied."],
  ["Clove","Syzygium aromaticum","Myrtaceae","Toothache, nausea; antiseptic.","Bud chewed; oil applied."],
  ["Nutmeg","Myristica fragrans","Myristicaceae","Insomnia, digestion; CAUTION dose.","Powder pinch with milk."],
  ["Star Anise","Illicium verum","Schisandraceae","Cough, cold, digestion.","Seeds in decoction."],
  ["Tamarind","Tamarindus indica","Fabaceae","Digestion and cooling.","Pulp in water taken."],
  ["Indian Jujube Leaf","Ziziphus jujuba","Rhamnaceae","Digestion; calming sleep.","Leaf/seed decoction."],
  ["Soap Pod","Senegalia rugata (leaf)","Fabaceae","Skin and hair.","Leaf paste used."],
  ["Drumstick Root Bark","Moringa oleifera (root)","Moringaceae","Stimulant; CAUTION strong.","Trace root bark."],
  ["Indian Aloe Flower","Aloe vera (flower)","Asphodelaceae","Cooling; minor tonic.","Flower infusion."],
  ["Holy Basil Krishna","Ocimum tenuiflorum var. krishna","Lamiaceae","Immunity and respiratory.","Leaves as tea."],
  ["Sandpaper Tree","Ehretia laevis","Boraginaceae","Wounds, fractures, ulcers.","Bark paste applied."],
  ["Indian Coral Bead Leaf","Abrus precatorius (leaf)","Fabaceae","Sweet leaf for throat/cough.","Leaf chewed or decocted."],
  ["Velvet Leaf","Cissampelos pareira","Menispermaceae","Diarrhoea, urinary, fever.","Root decoction taken."],
  ["Country Leadwort","Plumbago indica","Plumbaginaceae","Digestion; CAUTION irritant.","Trace root under guidance."],
  ["Indian Squash Leaf","Benincasa hispida (leaf)","Cucurbitaceae","Cooling; minor wounds.","Leaf juice applied."],
  ["Climbing Nettle Root","Tragia plukenetii","Euphorbiaceae","Joint pain; counter-irritant.","Root decoction under guidance."],
  ["Spreading Hogweed White","Trianthema portulacastrum","Aizoaceae","Liver disorders; diuretic.","Whole-plant cooked/decocted."],
  ["Indian Madar Root","Calotropis procera","Apocynaceae","Joints and skin; CAUTION toxic.","External only under guidance."],
  ["Country Senna Leaf","Cassia angustifolia (leaf)","Fabaceae","Constipation; purgative.","Leaf infusion."],
  ["Indian Tinospora Leaf","Tinospora cordifolia (leaf)","Menispermaceae","Immunity and skin.","Leaf paste/juice."],
  ["Drumstick Flower","Moringa oleifera (flower)","Moringaceae","Tonic; nutritive.","Flowers cooked."],
  ["Holy Fig Aerial Root","Ficus benghalensis (root)","Moraceae","Diabetes and tonic.","Aerial-root decoction."],
  ["Indian Kino Leaf","Pterocarpus santalinus","Fabaceae","Skin, complexion; red sandalwood.","Heartwood paste applied."],
  ["White Teak","Gmelina arborea (root)","Lamiaceae","Fever and tonic.","Root decoction taken."]
];

function record(p, idx) {
  const [common, sci, family, use, prep] = p;
  return {
    "_seedId": "seed_" + String(idx + 1).padStart(3, "0"),
    "Aarogya Mitra": "Seed Catalogue",
    "Client": "seed-import",
    "Drug Name": common + " (" + sci + ")",
    "scientificName": sci,
    "medicinalPlants": common + " / " + sci + " (" + family + ")",
    "howToApply": use,
    "modeOfPreparation": prep,
    "isViable": true,
    "yearsUsedSince": 0,
    "livingAreaSince": 0
  };
}

function getJSON(url) {
  return new Promise((resolve) => {
    https.get(url, { headers: { "User-Agent": "ArogyaMitra-seed/1.0" } }, (res) => {
      if (res.statusCode !== 200) { res.resume(); return resolve(null); }
      let d = ""; res.on("data", c => d += c);
      res.on("end", () => { try { resolve(JSON.parse(d)); } catch { resolve(null); } });
    }).on("error", () => resolve(null));
  });
}

// Resolve the canonical image for a plant from its SCIENTIFIC NAME's Wikipedia
// page. This is what guarantees species-correct photos (no Ashoka emperor).
async function resolveImage(sci) {
  const title = encodeURIComponent(sci.replace(/ \(.*\)$/, "").replace(/ /g, "_"));
  const j = await getJSON("https://en.wikipedia.org/api/rest_v1/page/summary/" + title);
  if (j && j.type === "standard") {
    if (j.originalimage && j.originalimage.source) return j.originalimage.source;
    if (j.thumbnail && j.thumbnail.source) return j.thumbnail.source;
  }
  return null;
}

(async () => {
  const records = PLANTS.map(record);
  console.log(`Prepared ${records.length} plant records (${DRY_RUN ? "DRY RUN" : "LIVE"}).`);

  if (DRY_RUN) {
    // Resolve a few images to prove the pipeline, write a preview file.
    for (let i = 0; i < 5; i++) {
      const img = await resolveImage(records[i].scientificName);
      console.log(`  ${records[i]["Drug Name"]}\n    -> ${img || "(no Wikipedia image)"}`);
    }
    fs.writeFileSync(path.join(__dirname, "plants_seed.json"),
      JSON.stringify(records, null, 2));
    console.log("Wrote plants_seed.json (preview, no images embedded).");
    return;
  }

  if (!fs.existsSync(KEY_PATH)) {
    console.error("\nMissing serviceAccountKey.json next to this script.");
    console.error("Firebase Console -> Project Settings -> Service accounts -> Generate new private key.");
    process.exit(1);
  }
  const admin = require("firebase-admin");
  admin.initializeApp({
    credential: admin.credential.cert(require(KEY_PATH)),
    databaseURL: DB_URL
  });
  const db = admin.database();

  let ok = 0, noImg = 0;
  for (const r of records) {
    const img = await resolveImage(r.scientificName);
    const { _seedId, ...row } = r;
    if (img) { row.imageUrls = { wiki: img }; } else { noImg++; }
    await db.ref("drug_to_be_validated/" + _seedId).set(row);
    ok++;
    if (ok % 25 === 0) console.log(`  ...${ok}/${records.length} written`);
  }
  console.log(`\nDone. Wrote ${ok} records (${noImg} had no Wikipedia image and were seeded without one).`);
  console.log("Re-running is safe — records key off stable seed ids and update in place.");
  process.exit(0);
})();
