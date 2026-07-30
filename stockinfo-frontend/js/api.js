// Centralized API wrapper for the StockInfo frontend.

// Apply saved theme immediately (before paint) so pages never flash the wrong theme.
(function () {
    const saved = localStorage.getItem("stockinfo_theme") || "light";
    document.documentElement.setAttribute("data-theme", saved);
})();

const API_BASE_URL = "http://stockinfo-production.up.railway.app";

function getToken() {
    return sessionStorage.getItem("stockinfo_token");
}

async function apiRequest(endpoint, method = "GET", body = null) {
    const headers = { "Content-Type": "application/json" };
    const token = getToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    if (response.status === 401) {
        Auth.logout(true);
        throw new Error("Session expired. Please log in again.");
    }

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Request failed: ${response.status}`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

const Api = {
    get: (endpoint) => apiRequest(endpoint, "GET"),
    post: (endpoint, body) => apiRequest(endpoint, "POST", body),
    put: (endpoint, body) => apiRequest(endpoint, "PUT", body),
    del: (endpoint) => apiRequest(endpoint, "DELETE"),
};

const Auth = {
    async login(username, password) {
        const data = await apiRequest("/auth/login", "POST", { username, password });
        sessionStorage.setItem("stockinfo_token", data.token);
        sessionStorage.setItem("stockinfo_user", JSON.stringify({
            userId: data.userId,
            username: data.username,
            fullName: data.fullName,
            role: data.role
        }));
        return data;
    },

    async register(payload) {
        return apiRequest("/auth/register", "POST", payload);
    },

    logout(silent = false) {
        sessionStorage.removeItem("stockinfo_token");
        sessionStorage.removeItem("stockinfo_user");
        if (!silent) {
            window.location.href = "index.html";
        }
    },

    isLoggedIn() {
        return !!getToken();
    },

    getUser() {
        const raw = sessionStorage.getItem("stockinfo_user");
        return raw ? JSON.parse(raw) : null;
    },

    isAdmin() {
        const user = Auth.getUser();
        return !!user && user.role === "ADMIN";
    },

    requireLogin() {
        if (!Auth.isLoggedIn()) {
            window.location.href = "index.html";
        }
    },

    requireAdmin() {
        Auth.requireLogin();
        if (!Auth.isAdmin()) {
            window.location.href = "dashboard.html";
        }
    }
};

const Theme = {
    get() {
        return document.documentElement.getAttribute("data-theme") || "light";
    },
    set(theme) {
        document.documentElement.setAttribute("data-theme", theme);
        localStorage.setItem("stockinfo_theme", theme);
        Theme.updateIcons();
    },
    toggle() {
        Theme.set(Theme.get() === "dark" ? "light" : "dark");
    },
    updateIcons() {
        const isDark = Theme.get() === "dark";
        document.querySelectorAll(".theme-toggle-icon").forEach(icon => {
            icon.className = "bi theme-toggle-icon " + (isDark ? "bi-sun" : "bi-moon-stars");
        });
    }
};

function formatCurrency(n) {
    const value = Number(n) || 0;
    return "₹" + value.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatPercent(n) {
    const value = Number(n) || 0;
    const sign = value > 0 ? "+" : "";
    return `${sign}${value.toFixed(2)}%`;
}

function showToast(message, isError = false) {
    const toastEl = document.getElementById("appToast");
    if (!toastEl) {
        alert(message);
        return;
    }
    toastEl.classList.remove("text-bg-success", "text-bg-danger");
    toastEl.classList.add(isError ? "text-bg-danger" : "text-bg-success");
    toastEl.querySelector(".toast-body").textContent = message;
    new bootstrap.Toast(toastEl).show();
}

function renderNavbar(activePage) {
    const container = document.getElementById("navbarContainer");
    if (!container) return;

    const user = Auth.getUser();
    const isAdmin = Auth.isAdmin();

    const links = [
        { key: "dashboard", href: "dashboard.html", icon: "bi-grid-1x2", label: "Dashboard" },
        { key: "stocks", href: "stocks.html", icon: "bi-graph-up", label: "Market" },
        { key: "portfolio", href: "portfolio.html", icon: "bi-briefcase", label: "Portfolio" },
        { key: "watchlist", href: "watchlist.html", icon: "bi-star", label: "Watchlist" },
        { key: "transactions", href: "transactions.html", icon: "bi-clock-history", label: "Transactions" },
        { key: "reports", href: "reports.html", icon: "bi-file-earmark-bar-graph", label: "Reports" },
    ];
    if (isAdmin) {
        links.push({ key: "admin", href: "admin.html", icon: "bi-shield-lock", label: "Admin" });
    }

    const navLinks = links.map(l => `
        <li class="nav-item">
            <a class="nav-link ${activePage === l.key ? "active fw-semibold" : ""}" href="${l.href}">
                <i class="bi ${l.icon} me-1"></i>${l.label}
            </a>
        </li>
    `).join("");

    container.innerHTML = `
        <nav class="navbar navbar-expand-lg navbar-dark" style="background:var(--bg-navbar);">
            <div class="container-fluid">
                <a class="navbar-brand" href="dashboard.html">Stock<span>Info</span></a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="mainNav">
                    <ul class="navbar-nav me-auto">${navLinks}</ul>
                    <button class="theme-toggle-btn me-3" onclick="Theme.toggle()" title="Toggle dark/light mode">
                        <i class="bi bi-moon-stars theme-toggle-icon"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-light me-2" id="profileTrigger" data-bs-toggle="modal" data-bs-target="#profileModal">
                        <i class="bi bi-person-circle me-1"></i>${user ? user.fullName : ""}
                    </button>
                    <button class="btn btn-outline-light btn-sm" onclick="Auth.logout()">
                        <i class="bi bi-box-arrow-right me-1"></i>Logout
                    </button>
                </div>
            </div>
        </nav>
    `;

    injectProfileModal();
    Theme.updateIcons();
}

function injectProfileModal() {
    if (document.getElementById("profileModal")) return;

    const modalHtml = `
        <div class="modal fade" id="profileModal" tabindex="-1">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-person-circle me-1"></i>My Profile</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
              </div>
              <div class="modal-body" id="profileModalBody">
                <div class="text-center py-3"><div class="spinner-border text-primary"></div></div>
              </div>
            </div>
          </div>
        </div>
    `;
    document.body.insertAdjacentHTML("beforeend", modalHtml);

    document.getElementById("profileModal").addEventListener("show.bs.modal", async () => {
        const body = document.getElementById("profileModalBody");
        body.innerHTML = `<div class="text-center py-3"><div class="spinner-border text-primary"></div></div>`;
        try {
            const profile = await Api.get("/user/profile");
            const memberSince = profile.memberSince
                ? new Date(profile.memberSince).toLocaleDateString("en-IN", { day: "numeric", month: "long", year: "numeric" })
                : "-";

            body.innerHTML = `
                <table class="table table-borderless mb-0">
                    <tr><td class="text-muted">Full Name</td><td class="fw-semibold">${profile.fullName}</td></tr>
                    <tr><td class="text-muted">Username</td><td class="fw-semibold">${profile.username}</td></tr>
                    <tr><td class="text-muted">Email</td><td class="fw-semibold">${profile.email}</td></tr>
                    <tr><td class="text-muted">Mobile</td><td class="fw-semibold">${profile.phoneNumber || "Not provided"}</td></tr>
                    <tr><td class="text-muted">Role</td><td><span class="badge bg-primary">${profile.role}</span></td></tr>
                    <tr><td class="text-muted">Wallet Balance</td><td class="fw-semibold">${formatCurrency(profile.walletBalance)}</td></tr>
                    <tr><td class="text-muted">Member Since</td><td class="fw-semibold">${memberSince}</td></tr>
                </table>
            `;
        } catch (err) {
            body.innerHTML = `<p class="text-danger text-center mb-0">Failed to load profile: ${err.message}</p>`;
        }
    });
}