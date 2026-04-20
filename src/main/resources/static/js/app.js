// ===== FARM-TO-CLOUD: Core JavaScript Utilities =====

const API_BASE = '';

// ===== AUTH HELPERS =====
function getToken() { return localStorage.getItem('ftc_token'); }
function getUser() { const u = localStorage.getItem('ftc_user'); return u ? JSON.parse(u) : null; }
function setAuth(token, user) {
    localStorage.setItem('ftc_token', token);
    localStorage.setItem('ftc_user', JSON.stringify(user));
}
function clearAuth() { localStorage.removeItem('ftc_token'); localStorage.removeItem('ftc_user'); }
function isLoggedIn() { return !!getToken(); }
function getUserRole() { const u = getUser(); return u ? u.role : null; }

function requireAuth() {
    if (!isLoggedIn()) { window.location.href = '/login.html'; return false; }
    return true;
}

function requireRole(role) {
    if (!requireAuth()) return false;
    if (getUserRole() !== role) {
        showToast('Access denied. Wrong role.', 'error');
        window.location.href = '/';
        return false;
    }
    return true;
}

// ===== API CALLS =====
async function apiCall(endpoint, options = {}) {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    try {
        const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
        const data = await res.json();
        if (!res.ok || !data.success) {
            throw new Error(data.message || 'Request failed');
        }
        return data;
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
}

async function apiGet(endpoint) { return apiCall(endpoint); }
async function apiPost(endpoint, body) { return apiCall(endpoint, { method: 'POST', body: JSON.stringify(body) }); }
async function apiPut(endpoint, body) { return apiCall(endpoint, { method: 'PUT', body: JSON.stringify(body) }); }
async function apiDelete(endpoint) { return apiCall(endpoint, { method: 'DELETE' }); }

// ===== TOAST NOTIFICATIONS =====
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// ===== NAVBAR RENDERER =====
function renderNavbar() {
    const user = getUser();
    const nav = document.getElementById('navbar');
    if (!nav) return;

    let links = '';
    if (!user) {
        links = `
            <a href="/login.html">Login</a>
            <a href="/signup.html" class="btn-primary-sm">Get Started</a>
        `;
    } else if (user.role === 'FARMER') {
        links = `
            <a href="/farmer-dashboard.html">Dashboard</a>
            <a href="/crop-listing.html">My Crops</a>
            <a href="/payment.html">Payments</a>
            <button onclick="logout()">Logout</button>
        `;
    } else {
        links = `
            <a href="/kitchen-dashboard.html">Dashboard</a>
            <a href="/kitchen-dashboard.html#browse">Browse</a>
            <a href="/payment.html">Payments</a>
            <button onclick="logout()">Logout</button>
        `;
    }

    nav.innerHTML = `
        <a href="/" class="logo">🌾 FarmToCloud</a>
        <div class="nav-links">${links}</div>
    `;
}

function logout() {
    clearAuth();
    showToast('Logged out successfully');
    window.location.href = '/';
}

// ===== UTILITY FUNCTIONS =====
function formatDate(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}
function formatDateTime(dateStr) {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('en-IN', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
}
function formatCurrency(amt) {
    if (amt == null) return '—';
    return '₹' + Number(amt).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function getStatusBadge(status) {
    const map = {
        'PENDING': 'badge-warning', 'PICKUP_ASSIGNED': 'badge-info', 'VERIFIED': 'badge-success',
        'FARMER_CONFIRMED': 'badge-success', 'IN_TRANSIT': 'badge-info', 'DELIVERED': 'badge-success',
        'CANCELLED': 'badge-danger', 'OPEN': 'badge-warning', 'RESOLVED': 'badge-success',
        'PAID': 'badge-success', 'PROCESSING': 'badge-info', 'AVAILABLE': 'badge-success',
        'SOLD_OUT': 'badge-danger', 'HIGH': 'badge-danger', 'MEDIUM': 'badge-warning', 'LOW': 'badge-success'
    };
    return `<span class="badge ${map[status] || 'badge-neutral'}">${status}</span>`;
}

function getDemandEmoji(level) {
    if (level === 'HIGH') return '🔥';
    if (level === 'MEDIUM') return '⚖️';
    return '📉';
}

// ===== STATUS TIMELINE RENDERER =====
function renderStatusTimeline(currentStatus) {
    const steps = ['PENDING', 'PICKUP_ASSIGNED', 'VERIFIED', 'FARMER_CONFIRMED', 'IN_TRANSIT', 'DELIVERED'];
    const labels = ['Pending', 'Pickup', 'Verified', 'Confirmed', 'Transit', 'Delivered'];
    const icons = ['📋', '🚛', '✅', '👨‍🌾', '🚚', '📦'];
    const currentIdx = steps.indexOf(currentStatus);

    let html = '<div class="status-timeline">';
    steps.forEach((step, i) => {
        const isCompleted = i < currentIdx;
        const isActive = i === currentIdx;
        html += `
            <div class="status-step">
                ${i > 0 ? `<div class="status-line ${isCompleted || isActive ? 'completed' : ''}"></div>` : ''}
                <div class="status-dot ${isCompleted ? 'completed' : ''} ${isActive ? 'active' : ''}">${icons[i]}</div>
                <div class="status-label">${labels[i]}</div>
            </div>
        `;
    });
    html += '</div>';
    return html;
}

// Init navbar on page load
document.addEventListener('DOMContentLoaded', () => {
    renderNavbar();
    if (isLoggedIn()) {
        startNotificationPolling();
    }
});

// ===== NOTIFICATION POLLING =====
let pollingInterval = null;
function startNotificationPolling() {
    if (pollingInterval) clearInterval(pollingInterval);
    pollingInterval = setInterval(async () => {
        try {
            const res = await apiGet('/api/notifications/unread');
            if (res.success && res.data && res.data.length > 0) {
                for (const notif of res.data) {
                    showToast('🔔 ' + notif.message, 'info');
                    await apiPut(`/api/notifications/${notif.id}/read`, {});
                }
            }
        } catch (e) {
            console.error('Notification polling failed', e);
        }
    }, 5000); // Poll every 5 seconds
}

