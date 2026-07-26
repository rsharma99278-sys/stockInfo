Auth.requireLogin();
renderNavbar("stocks");

const PAGE_SIZE = 20;
let allStocks = [];
let filteredStocks = [];
let shownCount = 0;
let watchlistSymbols = new Set();
let scrollObserver = null;

async function init() {
    try {
        const [stocks, watchlist] = await Promise.all([
            Api.get("/stocks"),
            Api.get("/watchlist")
        ]);

        watchlistSymbols = new Set(watchlist.map(w => w.symbol));
        allStocks = stocks;
        filteredStocks = stocks;

        populateSectorFilter(stocks);
        setupScrollObserver();
        resetAndRender();

        document.getElementById("loadingWrap").classList.add("d-none");
        document.getElementById("stocksContent").classList.remove("d-none");

        setInterval(refreshPrices, 10000);
    } catch (err) {
        document.getElementById("loadingWrap").innerHTML =
            `<p class="text-danger text-center">Failed to load market data: ${err.message || "Unknown error"}</p>`;
        showToast(err.message || "Failed to load market data", true);
    }
}

function populateSectorFilter(stocks) {
    const sectors = [...new Set(stocks.map(s => s.sector))].sort();
    const select = document.getElementById("sectorFilter");
    sectors.forEach(sector => {
        const opt = document.createElement("option");
        opt.value = sector;
        opt.textContent = sector;
        select.appendChild(opt);
    });
}

function resetAndRender() {
    shownCount = 0;
    document.getElementById("stocksBody").innerHTML = "";
    document.getElementById("endOfListMsg").classList.add("d-none");
    loadMoreRows();
}

function loadMoreRows() {
    const nextBatch = filteredStocks.slice(shownCount, shownCount + PAGE_SIZE);
    if (nextBatch.length === 0) {
        document.getElementById("endOfListMsg").classList.remove("d-none");
        return;
    }

    const body = document.getElementById("stocksBody");
    body.insertAdjacentHTML("beforeend", nextBatch.map(s => rowHtml(s)).join(""));
    attachRowHandlers(nextBatch);

    shownCount += nextBatch.length;
    document.getElementById("pageInfo").textContent =
        `Showing ${shownCount} of ${filteredStocks.length} stocks`;

    if (shownCount >= filteredStocks.length) {
        document.getElementById("endOfListMsg").classList.remove("d-none");
    } else {
        document.getElementById("endOfListMsg").classList.add("d-none");
    }
}

function setupScrollObserver() {
    const sentinel = document.getElementById("scrollSentinel");
    scrollObserver = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && shownCount < filteredStocks.length) {
            document.getElementById("loadMoreSpinner").classList.remove("d-none");
            setTimeout(() => {
                loadMoreRows();
                document.getElementById("loadMoreSpinner").classList.add("d-none");
            }, 200);
        }
    }, { rootMargin: "200px" });
    scrollObserver.observe(sentinel);
}

function rowHtml(s) {
    const isUp = Number(s.changePercent) >= 0;
    const isWatched = watchlistSymbols.has(s.symbol);

    return `
        <tr data-symbol="${s.symbol}">
            <td><a href="#" class="stock-detail-link" data-symbol="${s.symbol}"><strong>${s.symbol}</strong></a><div class="text-muted small">${s.companyName}</div></td>
            <td><span class="badge bg-secondary-subtle text-dark">${s.sector}</span></td>
            <td class="text-end price-cell" data-price="${s.currentPrice}">${formatCurrency(s.currentPrice)}</td>
            <td class="text-end ${isUp ? 'text-success' : 'text-danger'}">
                <i class="bi ${isUp ? 'bi-caret-up-fill' : 'bi-caret-down-fill'}"></i> ${formatPercent(s.changePercent)}
            </td>
            <td class="text-center">
                <button class="btn btn-sm watch-btn ${isWatched ? 'btn-warning' : 'btn-outline-secondary'}" data-symbol="${s.symbol}">
                    <i class="bi ${isWatched ? 'bi-star-fill' : 'bi-star'}"></i>
                </button>
            </td>
            <td class="text-center">
                <button class="btn btn-sm btn-success buy-btn" data-symbol="${s.symbol}" data-price="${s.currentPrice}">Buy</button>
            </td>
        </tr>
    `;
}

function attachRowHandlers(newRows) {
    newRows.forEach(s => {
        const row = document.querySelector(`tr[data-symbol="${CSS.escape(s.symbol)}"]`);
        if (!row) return;

        row.querySelector(".watch-btn").addEventListener("click", (e) => toggleWatchlist(s.symbol, e.currentTarget));
        row.querySelector(".buy-btn").addEventListener("click", (e) =>
            openBuyModal(e.currentTarget.dataset.symbol, e.currentTarget.dataset.price));
        row.querySelector(".stock-detail-link").addEventListener("click", (e) => {
            e.preventDefault();
            openDetailModal(s.symbol);
        });
    });
}

async function toggleWatchlist(symbol, btn) {
    btn.disabled = true;
    try {
        if (watchlistSymbols.has(symbol)) {
            await Api.del(`/watchlist/${symbol}`);
            watchlistSymbols.delete(symbol);
            showToast(`${symbol} removed from watchlist`);
            btn.classList.remove("btn-warning");
            btn.classList.add("btn-outline-secondary");
            btn.querySelector("i").className = "bi bi-star";
        } else {
            await Api.post(`/watchlist/${symbol}`);
            watchlistSymbols.add(symbol);
            showToast(`${symbol} added to watchlist`);
            btn.classList.remove("btn-outline-secondary");
            btn.classList.add("btn-warning");
            btn.querySelector("i").className = "bi bi-star-fill";
        }
    } catch (err) {
        showToast(err.message || "Watchlist update failed", true);
    } finally {
        btn.disabled = false;
    }
}

let activeBuySymbol = null;

function openBuyModal(symbol, price) {
    activeBuySymbol = symbol;
    document.getElementById("buySymbol").textContent = symbol;
    document.getElementById("buyPrice").textContent = formatCurrency(price);
    document.getElementById("buyQty").value = 1;
    document.getElementById("buyTotal").textContent = formatCurrency(price);
    document.getElementById("buyError").classList.add("d-none");

    document.getElementById("buyQty").oninput = () => {
        const qty = Number(document.getElementById("buyQty").value) || 0;
        document.getElementById("buyTotal").textContent = formatCurrency(qty * price);
    };

    new bootstrap.Modal(document.getElementById("buyModal")).show();
}

document.getElementById("confirmBuyBtn").addEventListener("click", async () => {
    const qty = Number(document.getElementById("buyQty").value);
    const errorBox = document.getElementById("buyError");
    errorBox.classList.add("d-none");

    if (!qty || qty <= 0) {
        errorBox.textContent = "Enter a valid quantity.";
        errorBox.classList.remove("d-none");
        return;
    }

    try {
        await Api.post("/portfolio/buy", { symbol: activeBuySymbol, quantity: qty });
        bootstrap.Modal.getInstance(document.getElementById("buyModal")).hide();
        showToast(`Bought ${qty} share(s) of ${activeBuySymbol}`);
    } catch (err) {
        errorBox.textContent = err.message || "Purchase failed. Check your wallet balance.";
        errorBox.classList.remove("d-none");
    }
});

document.getElementById("searchInput").addEventListener("input", debounce(async (e) => {
    const query = e.target.value.trim();
    if (!query) {
        filteredStocks = allStocks;
    } else {
        try {
            filteredStocks = await Api.get(`/stocks/search?query=${encodeURIComponent(query)}`);
        } catch (err) {
            showToast(err.message || "Search failed", true);
            return;
        }
    }
    resetAndRender();
}, 350));

document.getElementById("sectorFilter").addEventListener("change", async (e) => {
    const sector = e.target.value;
    filteredStocks = sector ? await Api.get(`/stocks/sector/${encodeURIComponent(sector)}`) : allStocks;
    resetAndRender();
});

document.getElementById("gainersBtn").addEventListener("click", async () => {
    filteredStocks = await Api.get("/stocks/top-gainers");
    resetAndRender();
});

document.getElementById("losersBtn").addEventListener("click", async () => {
    filteredStocks = await Api.get("/stocks/top-losers");
    resetAndRender();
});

document.getElementById("resetBtn").addEventListener("click", () => {
    document.getElementById("searchInput").value = "";
    document.getElementById("sectorFilter").value = "";
    filteredStocks = allStocks;
    resetAndRender();
});

function debounce(fn, delay) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), delay);
    };
}

async function refreshPrices() {
    try {
        const fresh = await Api.get("/stocks");
        const freshMap = new Map(fresh.map(s => [s.symbol, s]));
        allStocks = fresh;
        filteredStocks = filteredStocks.map(s => freshMap.get(s.symbol) || s);

        document.querySelectorAll("#stocksBody tr[data-symbol]").forEach(row => {
            const symbol = row.dataset.symbol;
            const updated = freshMap.get(symbol);
            if (!updated) return;

            const priceCell = row.querySelector(".price-cell");
            const oldPrice = Number(priceCell.dataset.price);
            const newPrice = Number(updated.currentPrice);

            if (newPrice !== oldPrice) {
                priceCell.textContent = formatCurrency(newPrice);
                priceCell.dataset.price = newPrice;
                priceCell.classList.remove("flash-green", "flash-red");
                void priceCell.offsetWidth;
                priceCell.classList.add(newPrice > oldPrice ? "flash-green" : "flash-red");
            }
        });
    } catch (err) {
        console.warn("Price refresh failed:", err.message);
    }
}

init();

let detailChart = null;
let detailInterval = null;
let detailHistory = {};

function openDetailModal(symbol) {
    const stock = allStocks.find(s => s.symbol === symbol);
    if (!stock) return;

    document.getElementById("detailSymbol").textContent = `${stock.symbol} - ${stock.companyName}`;
    document.getElementById("detailSector").textContent = stock.sector;
    document.getElementById("detailPrice").textContent = formatCurrency(stock.currentPrice);
    document.getElementById("detailChange").textContent = formatPercent(stock.changePercent);
    document.getElementById("detailHigh").textContent = formatCurrency(stock.dayHigh);
    document.getElementById("detailLow").textContent = formatCurrency(stock.dayLow);
    document.getElementById("detailVolume").textContent = Number(stock.volume).toLocaleString("en-IN");

    detailHistory[symbol] = [{ time: new Date().toLocaleTimeString(), price: Number(stock.currentPrice) }];
    renderDetailChart(symbol);

    const modalEl = document.getElementById("detailModal");
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    detailInterval = setInterval(async () => {
        try {
            const fresh = await Api.get(`/stocks/${symbol}`);
            detailHistory[symbol].push({ time: new Date().toLocaleTimeString(), price: Number(fresh.currentPrice) });
            if (detailHistory[symbol].length > 30) detailHistory[symbol].shift();
            renderDetailChart(symbol);
        } catch (e) { /* silent */ }
    }, 10000);

    modalEl.addEventListener("hidden.bs.modal", () => {
        clearInterval(detailInterval);
        if (detailChart) { detailChart.destroy(); detailChart = null; }
    }, { once: true });
}

function renderDetailChart(symbol) {
    const points = detailHistory[symbol];
    const ctx = document.getElementById("detailChartCanvas");

    if (detailChart) {
        detailChart.data.labels = points.map(p => p.time);
        detailChart.data.datasets[0].data = points.map(p => p.price);
        detailChart.update();
        return;
    }

    detailChart = new Chart(ctx, {
        type: "line",
        data: {
            labels: points.map(p => p.time),
            datasets: [{
                label: `${symbol} Price`,
                data: points.map(p => p.price),
                borderColor: "#2563eb",
                backgroundColor: "rgba(37, 99, 235, 0.1)",
                tension: 0.3,
                fill: true,
                pointRadius: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { ticks: { callback: (v) => "₹" + v } } }
        }
    });
}