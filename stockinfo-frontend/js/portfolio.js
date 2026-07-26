Auth.requireLogin();
renderNavbar("portfolio");

let holdings = [];

async function loadPortfolio() {
    try {
        holdings = await Api.get("/portfolio");
        renderSummary();
        renderTable();
        document.getElementById("loadingWrap").classList.add("d-none");
        document.getElementById("portfolioContent").classList.remove("d-none");
    } catch (err) {
        showToast(err.message || "Failed to load portfolio", true);
    }
}

function renderSummary() {
    const totalInvested = holdings.reduce((sum, h) => sum + Number(h.investedValue), 0);
    const currentValue = holdings.reduce((sum, h) => sum + Number(h.currentValue), 0);
    const totalPL = currentValue - totalInvested;
    const totalPLPercent = totalInvested > 0 ? (totalPL / totalInvested) * 100 : 0;

    document.getElementById("totalInvested").textContent = formatCurrency(totalInvested);
    document.getElementById("currentValue").textContent = formatCurrency(currentValue);

    const plEl = document.getElementById("totalPL");
    plEl.textContent = `${formatCurrency(totalPL)} (${formatPercent(totalPLPercent)})`;
    plEl.classList.add(totalPL >= 0 ? "text-success" : "text-danger");
}

function renderTable() {
    const body = document.getElementById("portfolioBody");

    if (holdings.length === 0) {
        body.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No holdings yet - visit the Market to buy your first stock.</td></tr>`;
        return;
    }

    body.innerHTML = holdings.map(h => `
        <tr>
            <td><strong>${h.symbol}</strong><div class="text-muted small">${h.companyName}</div></td>
            <td>${h.quantity}</td>
            <td>${formatCurrency(h.averageBuyPrice)}</td>
            <td>${formatCurrency(h.currentPrice)}</td>
            <td>${formatCurrency(h.investedValue)}</td>
            <td>${formatCurrency(h.currentValue)}</td>
            <td class="${Number(h.profitLoss) >= 0 ? 'text-success' : 'text-danger'}">
                ${formatCurrency(h.profitLoss)} (${formatPercent(h.profitLossPercent)})
            </td>
            <td class="text-center">
                <button class="btn btn-sm btn-outline-danger sell-btn"
                    data-symbol="${h.symbol}" data-price="${h.currentPrice}" data-qty="${h.quantity}">
                    Sell
                </button>
            </td>
        </tr>
    `).join("");

    document.querySelectorAll(".sell-btn").forEach(btn => {
        btn.addEventListener("click", () => openSellModal(
            btn.dataset.symbol, Number(btn.dataset.price), Number(btn.dataset.qty)
        ));
    });
}

let activeSellSymbol = null;
let activeSellPrice = 0;
let activeSellOwned = 0;

function openSellModal(symbol, price, owned) {
    activeSellSymbol = symbol;
    activeSellPrice = price;
    activeSellOwned = owned;

    document.getElementById("sellSymbol").textContent = symbol;
    document.getElementById("sellPrice").textContent = formatCurrency(price);
    document.getElementById("sellOwned").textContent = owned;
    document.getElementById("sellQty").value = 1;
    document.getElementById("sellQty").max = owned;
    document.getElementById("sellTotal").textContent = formatCurrency(price);
    document.getElementById("sellError").classList.add("d-none");

    document.getElementById("sellQty").oninput = () => {
        const qty = Number(document.getElementById("sellQty").value) || 0;
        document.getElementById("sellTotal").textContent = formatCurrency(qty * price);
    };

    new bootstrap.Modal(document.getElementById("sellModal")).show();
}

document.getElementById("confirmSellBtn").addEventListener("click", async () => {
    const qty = Number(document.getElementById("sellQty").value);
    const errorBox = document.getElementById("sellError");
    errorBox.classList.add("d-none");

    if (!qty || qty <= 0) {
        errorBox.textContent = "Enter a valid quantity.";
        errorBox.classList.remove("d-none");
        return;
    }
    if (qty > activeSellOwned) {
        errorBox.textContent = `You only own ${activeSellOwned} shares.`;
        errorBox.classList.remove("d-none");
        return;
    }

    try {
        await Api.post("/portfolio/sell", { symbol: activeSellSymbol, quantity: qty });
        bootstrap.Modal.getInstance(document.getElementById("sellModal")).hide();
        showToast(`Sold ${qty} share(s) of ${activeSellSymbol}`);
        await loadPortfolio();
    } catch (err) {
        errorBox.textContent = err.message || "Sell failed.";
        errorBox.classList.remove("d-none");
    }
});

loadPortfolio();// portfolio.js - logic added in the Frontend Integration phase
