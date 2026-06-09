#!/usr/bin/env node
/* Backfill species-correct images for seed_* records missing one.
   Keyed on the binomial so images stay species-correct.
   Order: 1) en.wikipedia lead image (pageimages, follows redirects)
          2) Wikimedia Commons photo search (jpg/png)
   Throttled with backoff so Wikipedia/Commons don't 429 us. */
const admin = require("firebase-admin");
const https = require("https");
admin.initializeApp({
  credential: admin.credential.cert(require("./serviceAccountKey.json")),
  databaseURL: "https://vibhu-project-688eb-default-rtdb.firebaseio.com"
});
const sleep = ms => new Promise(r => setTimeout(r, ms));
function _get(url){return new Promise(r=>{https.get(url,{headers:{"User-Agent":"ArogyaMitra-seed/1.0 (contact: arogya)"}},s=>{if(s.statusCode!==200){s.resume();return r({_status:s.statusCode});}let d="";s.on("data",c=>d+=c);s.on("end",()=>{try{r(JSON.parse(d))}catch{r(null)}})}).on("error",()=>r(null))})}
async function getJSON(url){for(let i=0;i<4;i++){const j=await _get(url);if(j&&!j._status)return j;await sleep(500*(i+1));}return null;}
const clean = sci => sci.replace(/\s*\(.*?\)\s*/g," ").replace(/\s+(var|subsp|f|ssp)\.?\s+\S+/i," ").trim();
async function wiki(sci){
  const t=encodeURIComponent(clean(sci));
  const j=await getJSON(`https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&piprop=original|thumbnail&pithumbsize=800&redirects=1&titles=${t}`);
  const pages=j&&j.query&&j.query.pages; if(!pages)return null;
  for(const p of Object.values(pages)){
    if(p.original&&p.original.source) return p.original.source;
    if(p.thumbnail&&p.thumbnail.source) return p.thumbnail.source;
  }
  return null;
}
async function commons(sci){
  const q=encodeURIComponent(clean(sci));
  const j=await getJSON(`https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrnamespace=6&gsrsearch=${q}&gsrlimit=3&prop=imageinfo&iiprop=url&iiurlwidth=800`);
  const pages=j&&j.query&&j.query.pages; if(!pages)return null;
  for(const p of Object.values(pages).sort((a,b)=>(a.index||0)-(b.index||0))){
    const ii=p.imageinfo&&p.imageinfo[0]; if(!ii)continue;
    const u=ii.thumburl||ii.url;
    if(u && /\.(jpe?g|png)$/i.test(u.split("?")[0])) return u;
  }
  return null;
}
(async()=>{
  const ref=admin.database().ref("drug_to_be_validated");
  const all=(await ref.get()).val()||{};
  const todo=Object.keys(all).filter(k=>k.startsWith("seed_")&&!(all[k].imageUrls&&Object.keys(all[k].imageUrls).length));
  console.log(`${todo.length} seed records missing an image — backfilling (throttled)...`);
  let w=0,c=0,none=0,done=0;
  for(const k of todo){
    const sci=all[k].scientificName;
    let img=await wiki(sci), src="wiki";
    if(!img){img=await commons(sci); src="commons";}
    if(img){await ref.child(k).child("imageUrls").set({[src]:img}); (src==="wiki"?w++:c++);}
    else {none++; console.log(`  no image: ${all[k]["Drug Name"]}`);}
    done++;
    if(done%25===0) console.log(`  ...${done}/${todo.length}  (wiki:${w} commons:${c} none:${none})`);
    await sleep(120);
  }
  const after=(await ref.get()).val()||{};
  const seeds=Object.keys(after).filter(k=>k.startsWith("seed_"));
  const withImg=seeds.filter(k=>after[k].imageUrls&&Object.keys(after[k].imageUrls).length);
  console.log(`\nBackfill pass done. this pass: wiki:${w} commons:${c} none:${none}`);
  console.log(`Coverage now: ${withImg.length}/${seeds.length} seed records have an image.`);
  process.exit(0);
})().catch(e=>{console.error("ERR",e.message);process.exit(1)});
