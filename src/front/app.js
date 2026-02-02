// Utilidades
const money = (cents) => (cents / 100).toLocaleString("en-US", { style: "currency", currency: "USD" });

function toast(msg){
  const el = document.getElementById("toast");
  el.textContent = msg;
  el.classList.remove("hidden");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => el.classList.add("hidden"), 1800);
}

// Productos
const PRODUCTS = [
  { id:"agua",    name:"Agua 500 ml",       price:150, icon:"🧴", stock:7 },
  { id:"gaseosa", name:"Gaseosa 350 ml",    price:250, icon:"🥤", stock:5 },
  { id:"snack",   name:"Snack salado",      price:300, icon:"🟥", stock:7 },
  { id:"choco",   name:"Chocolate",         price:275, icon:"🍫", stock:8 },
  { id:"galleta", name:"Galletas",          price:125, icon:"🍪", stock:0 },
  { id:"jugo",    name:"Jugo",              price:200, icon:"🧃", stock:4 },
  { id:"cafe",    name:"Café",              price:180, icon:"☕", stock:10 },
  { id:"chips",   name:"Chips",             price:230, icon:"🟩", stock:6 },
  { id:"barra",   name:"Barra energética",  price:190, icon:"🟪", stock:9 },
];

// carrito
const cart = new Map();

//  Denominaciones
const DENOMS = [
  { key:"50d", label:"$50",  value:5000, type:"billete" },
  { key:"20d", label:"$20",  value:2000, type:"billete" },
  { key:"10d", label:"$10",  value:1000, type:"billete" },
  { key:"5d",  label:"$5",   value:500,  type:"billete" },
  { key:"1d",  label:"$1",   value:100,  type:"billete" },
  { key:"25c", label:"25¢",  value:25,   type:"moneda"  },
  { key:"10c", label:"10¢",  value:10,   type:"moneda"  },
  { key:"5c",  label:"5¢",   value:5,    type:"moneda"  },
  { key:"1c",  label:"1¢",   value:1,    type:"moneda"  },
];

// Inventario inicial 
const inventory = {
  "50d": 0,
  "20d": 0,
  "10d": 1,
  "5d":  3,
  "1d":  5,
  "25c": 13,
  "10c": 3,
  "5c":  13,
  "1c":  7,
};

// pago insertado en esta compra
const inserted = Object.fromEntries(DENOMS.map(d => [d.key, 0]));

// Elementos
const els = {
  productsGrid: document.getElementById("productsGrid"),
  denomsGrid: document.getElementById("denomsGrid"),
  trays: document.getElementById("trays"),

  total: document.getElementById("total"),
  paid: document.getElementById("paid"),
  paidPieces: document.getElementById("paidPieces"),
  change: document.getElementById("change"),
  changeToReturn: document.getElementById("changeToReturn"),
  changePieces: document.getElementById("changePieces"),
  changeBreakdown: document.getElementById("changeBreakdown"),

  btnClear: document.getElementById("btnClear"),
  btnPay: document.getElementById("btnPay"),
};

// logica del carrito de compras
function getQty(id){ return cart.get(id) ?? 0; }

function setQty(id, qty){
  const p = PRODUCTS.find(x => x.id === id);
  if (!p) return;

  qty = Math.max(0, qty);
  qty = Math.min(qty, p.stock); // control del stock

  if (qty === 0) cart.delete(id);
  else cart.set(id, qty);

  renderAll();
}

function inc(id){ setQty(id, getQty(id) + 1); }
function dec(id){ setQty(id, getQty(id) - 1); }

function totalDueCents(){
  let total = 0;
  for (const [id, qty] of cart.entries()){
    const p = PRODUCTS.find(x => x.id === id);
    total += (p?.price ?? 0) * qty;
  }
  return total;
}

// Proceso de pago
function paidCents(){
  let sum = 0;
  for (const d of DENOMS) sum += inserted[d.key] * d.value;
  return sum;
}
function paidPieces(){
  let n = 0;
  for (const d of DENOMS) n += inserted[d.key];
  return n;
}

function addMoney(denKey){
  inserted[denKey] += 1;
  renderAll();
}

function resetPurchase(){
  cart.clear();
  for (const d of DENOMS) inserted[d.key] = 0;
  renderAll();
}

function computeChangePlan(changeCents, invSnapshot){
  // Greedy de mayor a menor con limitación por inventario
  const sorted = [...DENOMS].sort((a,b) => b.value - a.value);

  const plan = {};
  let remaining = changeCents;

  for (const d of sorted){
    const avail = invSnapshot[d.key] ?? 0;
    if (avail <= 0) continue;
    if (remaining <= 0) break;

    const need = Math.floor(remaining / d.value);
    const take = Math.min(need, avail);
    if (take > 0){
      plan[d.key] = take;
      remaining -= take * d.value;
    }
  }

  return { plan, remaining };
}

function planToLines(plan){
  const lines = [];
  for (const d of [...DENOMS].sort((a,b)=> b.value - a.value)){
    if (plan[d.key]){
      lines.push({ label: d.label, qty: plan[d.key], value: d.value });
    }
  }
  return lines;
}

function pay(){
  const total = totalDueCents();
  if (total <= 0){
    toast("Selecciona al menos un producto.");
    return;
  }

  const paid = paidCents();
  if (paid < total){
    toast("Pago insuficiente.");
    return;
  }

  // Sumar lo insertado al inventario 
  for (const d of DENOMS){
    inventory[d.key] = (inventory[d.key] ?? 0) + inserted[d.key];
  }

  // Calculo de cambio usando inventario ya actualizado
  const change = paid - total;
  const invSnapshot = { ...inventory };
  const { plan, remaining } = computeChangePlan(change, invSnapshot);

  if (remaining !== 0){
    // No se puede dar cambio exacto: revertimos el paso 1
    for (const d of DENOMS){
      inventory[d.key] -= inserted[d.key];
    }
    toast("No hay cambio suficiente en bandejas (exacto).");
    return;
  }

  // Restamos del inventario lo devuelto
  for (const [k, qty] of Object.entries(plan)){
    inventory[k] -= qty;
  }

  //  Despachamo reducir stock según carrito
  for (const [id, qty] of cart.entries()){
    const p = PRODUCTS.find(x => x.id === id);
    if (p) p.stock -= qty;
  }

  // Limpiar compra 
  cart.clear();
  for (const d of DENOMS) inserted[d.key] = 0;

  toast(`Compra completada. Cambio: ${money(change)}`);
  renderAll();
}

// render
function renderProducts(){
  els.productsGrid.innerHTML = PRODUCTS.map(p => {
    const qty = getQty(p.id);
    const soldOut = p.stock <= 0;

    return `
      <article class="product ${soldOut ? "disabled" : ""}" data-id="${p.id}">
        <div class="p-img">${p.icon}</div>
        <div class="p-name">${p.name}</div>
        <div class="p-price">${money(p.price)}</div>
        <div class="p-stock">Disponible: ${p.stock}</div>

        ${soldOut ? `<div class="soldout">AGOTADO</div>` : ""}

        <div class="p-qty">
          <button class="dec" ${qty === 0 ? "disabled" : ""}>-</button>
          <div class="count">${qty}</div>
          <button class="inc" ${qty >= p.stock ? "disabled" : ""}>+</button>
        </div>
      </article>
    `;
  }).join("");

  els.productsGrid.querySelectorAll(".product").forEach(card => {
    const id = card.getAttribute("data-id");
    const p = PRODUCTS.find(x => x.id === id);
    if (!p || p.stock <= 0) return;

    card.querySelector(".inc").addEventListener("click", () => inc(id));
    card.querySelector(".dec").addEventListener("click", () => dec(id));
  });
}

function renderDenoms(){
  const ordered = [
    "1c","5c","10c","25c",
    "1d","5d","10d","20d",
    "50d"
  ].map(k => DENOMS.find(d => d.key === k)).filter(Boolean);

  els.denomsGrid.innerHTML = ordered.map(d => `
    <button class="dbtn" data-key="${d.key}">
      ${d.label}
      <small>${d.type}</small>
    </button>
  `).join("");

  els.denomsGrid.querySelectorAll(".dbtn").forEach(btn => {
    const k = btn.getAttribute("data-key");
    btn.addEventListener("click", () => addMoney(k));
  });
}

function renderTrays(){
  // Barra inferior de monedas
  const ordered = [...DENOMS].sort((a,b)=> b.value - a.value);
  els.trays.innerHTML = ordered.map(d => `
    <div class="tray">
      <div class="tlabel">${d.label}</div>
      <div class="tcount">${inventory[d.key] ?? 0}</div>
      <div class="ttype">${d.type}</div>
    </div>
  `).join("");
}

function renderTotalsAndChangePreview(){
  const total = totalDueCents();
  const paid = paidCents();
  const paidP = paidPieces();
  const change = Math.max(0, paid - total);

  els.total.textContent = money(total);
  els.paid.textContent = money(paid);
  els.paidPieces.textContent = String(paidP);
  els.change.textContent = money(change);

  // Preview del cambio
  els.changeToReturn.textContent = money(change);

  if (paid < total || total === 0){
    els.changePieces.textContent = "0";
    els.changeBreakdown.innerHTML = `<div class="muted">—</div>`;
    return;
  }

  // Cambio calculado contra inventario + insertado
  const invSnapshot = { ...inventory };
  for (const d of DENOMS){
    invSnapshot[d.key] = (invSnapshot[d.key] ?? 0) + inserted[d.key];
  }

  const { plan, remaining } = computeChangePlan(change, invSnapshot);
  const lines = planToLines(plan);

  const pieces = lines.reduce((acc, x) => acc + x.qty, 0);
  els.changePieces.textContent = String(remaining === 0 ? pieces : 0);

  if (remaining !== 0){
    els.changeBreakdown.innerHTML = `<div class="muted">No hay cambio exacto disponible.</div>`;
    return;
  }

  els.changeBreakdown.innerHTML = lines.length
    ? lines.map(x => `
        <div class="breakline">
          <span>${x.qty} × ${x.label}</span>
          <span>${money(x.qty * x.value)}</span>
        </div>
      `).join("")
    : `<div class="muted">Sin cambio.</div>`;
}

function renderAll(){
  renderProducts();
  renderDenoms();
  renderTotalsAndChangePreview();
  renderTrays();
}

els.btnClear.addEventListener("click", () => {
  resetPurchase();
  toast("Compra reiniciada.");
});

els.btnPay.addEventListener("click", pay);

renderAll();
