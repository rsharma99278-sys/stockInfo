Auth.requireAdmin();
renderNavbar("admin");

async function loadStats() {
  try {
    const stats = await Api.get("/admin/stats");
    document.getElementById("statsRow").innerHTML = `
      <div class="col-md-4">
        <div class="stat-card">
          <div class="stat-label">Total Users</div>
          <div class="stat-value">${stats.totalUsers}</div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card dark">
          <div class="stat-label">Total Stocks Listed</div>
          <div class="stat-value">${stats.totalStocks}</div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="stat-card green">
          <div class="stat-label">Total Transactions</div>
          <div class="stat-value">${stats.totalTransactions}</div>
        </div>
      </div>`;
  } catch (err) {
    showToast(err.message, true);
  }
}

async function loadAdminStocks() {
  try {
    const stocks = await Api.get("/stocks");
    const body = document.getElementById("adminStocksBody");
    body.innerHTML = stocks.map(s => `
      <tr>
        <td class="stock-symbol">${s.symbol}</td>
        <td>${s.companyName}</td>
        <td><span class="badge bg-secondary">${s.sector}</span></td>
        <td>${formatCurrency(s.currentPrice)}</td>
        <td class="${changeClass(s.changePercent)}">${formatPercent(s.changePercent)}</td>
        <td style="max-width:160px;">
          <div class="input-group input-group-sm">
            <input type="number" step="0.01" class="form-control" id="price-${s.symbol}" placeholder="New price">
            <button class="btn btn-outline-primary" onclick="updatePrice('${s.symbol}')">Set</button>
          </div>
        </td>
        <td><button class="btn btn-sm btn-outline-danger" onclick="deleteStock('${s.symbol}')"><i class="bi bi-trash"></i></button></td>
      </tr>
    `).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function loadUsers() {
  try {
    const users = await Api.get("/admin/users");
    const body = document.getElementById("usersBody");
    body.innerHTML = users.map(u => `
      <tr>
        <td>${u.fullName}</td>
        <td>${u.username}</td>
        <td>${u.email}</td>
        <td><span class="badge ${u.role === 'ADMIN' ? 'bg-primary' : 'bg-secondary'}">${u.role}</span></td>
        <td>${formatCurrency(u.walletBalance)}</td>
      </tr>
    `).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}

document.getElementById("addStockForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await Api.post("/admin/stocks", {
      symbol: document.getElementById("newSymbol").value.trim().toUpperCase(),
      companyName: document.getElementById("newCompanyName").value.trim(),
      sector: document.getElementById("newSector").value.trim(),
      currentPrice: Number(document.getElementById("newPrice").value)
    });
    showToast("Stock added successfully");
    e.target.reset();
    loadAdminStocks();
    loadStats();
  } catch (err) {
    showToast(err.message, true);
  }
});

async function updatePrice(symbol) {
  const input = document.getElementById(`price-${symbol}`);
  const price = Number(input.value);
  if (!price || price <= 0) {
    showToast("Enter a valid price", true);
    return;
  }
  try {
    await Api.put(`/admin/stocks/${symbol}/price`, { price });
    showToast(`${symbol} price updated`);
    loadAdminStocks();
  } catch (err) {
    showToast(err.message, true);
  }
}

async function deleteStock(symbol) {
  if (!confirm(`Delete ${symbol}? This cannot be undone.`)) return;
  try {
    await Api.del(`/admin/stocks/${symbol}`);
    showToast(`${symbol} deleted`);
    loadAdminStocks();
    loadStats();
  } catch (err) {
    showToast(err.message, true);
  }
}

loadStats();
loadAdminStocks();
loadUsers();
