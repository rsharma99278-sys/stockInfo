Auth.requireLogin();
renderNavbar("reports");

let allSuggestions = [];

async function loadReports() {
    try {
        const [holdings, stocks, suggestions] = await Promise.all([
            Api.get("/portfolio"),
            Api.get("/stocks"),
            Api.get("/suggestions")
        ]);

        const sectorMap = new Map(stocks.map(s => [s.symbol, s.sector]));

        renderAllocationChart(holdings, sectorMap);
        renderValueChart(holdings);

        allSuggestions = suggestions;
        renderSuggestions("ALL");

        document.getElementById("loadingWrap").classList.add("d-none");
        document.getElementById("reportsContent").classList.remove("d-none");
    } catch (err) {
        document.getElementById("loadingWrap").innerHTML =
            `<p class="text-danger text-center">Failed to load reports: ${err.message || "Unknown error"}</p>`;
        showToast(err.message || "Failed to load reports", true);
    }
}

function renderAllocationChart(holdings, sectorMap) {
    const canvas = document.getElementById("allocationChart");

    if (holdings.length === 0) {
        canvas.classList.add("d-none");
        document.getElementById("noHoldingsMsg").classList.remove("d-none");
        return;
    }

    const bySector = {};
    holdings.forEach(h => {
        const sector = sectorMap.get(h.symbol) || "Other";
        bySector[sector] = (bySector[sector] || 0) + Number(h.currentValue);
    });

    new Chart(canvas, {
        type: "doughnut",
        data: {
            labels: Object.keys(bySector),
            datasets: [{
                data: Object.values(bySector),
                backgroundColor: ["#2563eb", "#16a34a", "#dc2626", "#f59e0b", "#7c3aed", "#0891b2", "#db2777", "#65a30d", "#ea580c", "#4f46e5"]
            }]
        },
        options: {
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom", labels: { boxWidth: 12, font: { size: 11 } } },
                tooltip: {
                    callbacks: { label: (ctx) => `${ctx.label}: ${formatCurrency(ctx.raw)}` }
                }
            }
        }
    });
}

function renderValueChart(holdings) {
    const canvas = document.getElementById("valueChart");

    new Chart(canvas, {
        type: "bar",
        data: {
            labels: holdings.map(h => h.symbol),
            datasets: [
                { label: "Invested", data: holdings.map(h => Number(h.investedValue)), backgroundColor: "#94a3b8" },
                { label: "Current Value", data: holdings.map(h => Number(h.currentValue)), backgroundColor: "#2563eb" }
            ]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            plugins: { legend: { position: "bottom" } },
            scales: { y: { beginAtZero: true } }
        }
    });
}

function renderSuggestions(filter) {
    const body = document.getElementById("suggestionsBody");
    const filtered = filter === "ALL" ? allSuggestions : allSuggestions.filter(s => s.suggestion === filter);

    if (filtered.length === 0) {
        body.innerHTML = `<tr><td colspan="3" class="text-center text-muted">No suggestions in this category</td></tr>`;
        return;
    }

    const badgeClass = { BUY: "bg-success", HOLD: "bg-warning text-dark", SELL: "bg-danger" };

    body.innerHTML = filtered.map(s => `
        <tr>
            <td><strong>${s.symbol}</strong><div class="text-muted small">${s.companyName}</div></td>
            <td><span class="badge ${badgeClass[s.suggestion] || 'bg-secondary'}">${s.suggestion}</span></td>
            <td class="text-muted small">${s.reason || ""}</td>
        </tr>
    `).join("");
}

document.querySelectorAll("[data-filter]").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll("[data-filter]").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        renderSuggestions(btn.dataset.filter);
    });
});

loadReports();