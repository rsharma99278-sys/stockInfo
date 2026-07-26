Auth.requireLogin();
renderNavbar("dashboard");

async function loadDashboard() {
    try {
        const data = await Api.get("/dashboard");

        document.getElementById("walletBalance").textContent = formatCurrency(data.walletBalance);
        document.getElementById("portfolioValue").textContent = formatCurrency(data.currentPortfolioValue);
        document.getElementById("holdingsCount").textContent = data.totalHoldings ?? 0;
        document.getElementById("watchlistCount").textContent = data.watchlistCount ?? 0;

        const plEl = document.getElementById("totalPL");
        const plValue = Number(data.totalProfitLoss) || 0;
        plEl.textContent = `${formatCurrency(plValue)} (${formatPercent(data.totalProfitLossPercent)})`;
        plEl.classList.add(plValue >= 0 ? "text-success" : "text-danger");

        renderTopHoldings(data.topHoldings || []);
        renderRecentTransactions(data.recentTransactions || []);

        document.getElementById("loadingWrap").classList.add("d-none");
        document.getElementById("dashboardContent").classList.remove("d-none");
    } catch (err) {
        showToast(err.message || "Failed to load dashboard", true);
    }
}

function renderTopHoldings(holdings) {
    const body = document.getElementById("topHoldingsBody");
    if (holdings.length === 0) {
        body.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No holdings yet</td></tr>`;
        return;
    }

    body.innerHTML = holdings.map(h => `
        <tr>
            <td><strong>${h.symbol}</strong><div class="text-muted small">${h.companyName}</div></td>
            <td>${h.quantity}</td>
            <td>${formatCurrency(h.averageBuyPrice)}</td>
            <td>${formatCurrency(h.currentPrice)}</td>
            <td class="${Number(h.profitLoss) >= 0 ? 'text-success' : 'text-danger'}">
                ${formatCurrency(h.profitLoss)} (${formatPercent(h.profitLossPercent)})
            </td>
        </tr>
    `).join("");
}

function renderRecentTransactions(transactions) {
    const list = document.getElementById("recentTxnList");
    if (transactions.length === 0) {
        list.innerHTML = `<li class="list-group-item text-center text-muted">No transactions yet</li>`;
        return;
    }

    list.innerHTML = transactions.map(t => `
        <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>
                <span class="badge ${t.type === 'BUY' ? 'bg-success' : 'bg-danger'} me-2">${t.type}</span>
                <strong>${t.symbol}</strong>
                <div class="text-muted small">${t.quantity} shares @ ${formatCurrency(t.pricePerUnit)}</div>
            </div>
            <span class="fw-semibold">${formatCurrency(t.totalAmount)}</span>
        </li>
    `).join("");
}

loadDashboard();// dashboard.js - logic added in the Frontend Integration phase
