Auth.requireLogin();
renderNavbar("watchlist");

async function loadWatchlist() {
    try {
        const items = await Api.get("/watchlist");
        renderTable(items);
        document.getElementById("loadingWrap").classList.add("d-none");
        document.getElementById("watchlistContent").classList.remove("d-none");
        setInterval(async () => {
            try {
                const fresh = await Api.get("/watchlist");
                renderTable(fresh);
            } catch (e) { /* silent background refresh */ }
        }, 10000);
    } catch (err) {
        showToast(err.message || "Failed to load watchlist", true);
    }
}

function renderTable(items) {
    const body = document.getElementById("watchlistBody");

    if (items.length === 0) {
        body.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Your watchlist is empty - add stocks from the Market page.</td></tr>`;
        return;
    }

    body.innerHTML = items.map(w => {
        const isUp = Number(w.changePercent) >= 0;
        return `
            <tr>
                <td><strong>${w.symbol}</strong><div class="text-muted small">${w.companyName}</div></td>
                <td class="text-end">${formatCurrency(w.currentPrice)}</td>
                <td class="text-end ${isUp ? 'text-success' : 'text-danger'}">
                    <i class="bi ${isUp ? 'bi-caret-up-fill' : 'bi-caret-down-fill'}"></i> ${formatPercent(w.changePercent)}
                </td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger remove-btn" data-symbol="${w.symbol}">
                        <i class="bi bi-trash"></i> Remove
                    </button>
                </td>
            </tr>
        `;
    }).join("");

    document.querySelectorAll(".remove-btn").forEach(btn => {
        btn.addEventListener("click", () => removeFromWatchlist(btn.dataset.symbol));
    });
}

async function removeFromWatchlist(symbol) {
    try {
        await Api.del(`/watchlist/${symbol}`);
        showToast(`${symbol} removed from watchlist`);
        const items = await Api.get("/watchlist");
        renderTable(items);
    } catch (err) {
        showToast(err.message || "Failed to remove", true);
    }
}

loadWatchlist();// watchlist.js - logic added in the Frontend Integration phase
