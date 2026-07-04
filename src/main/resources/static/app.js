const productList = document.querySelector("#product-list");
const productCount = document.querySelector("#product-count");
const catalogStatus = document.querySelector("#catalog-status");
const cartList = document.querySelector("#cart-list");
const cartEmpty = document.querySelector("#cart-empty");
const cartTotal = document.querySelector("#cart-total");
const checkoutButton = document.querySelector("#checkout");
const clearCartButton = document.querySelector("#clear-cart");
const refreshButton = document.querySelector("#refresh-products");
const orderStatus = document.querySelector("#order-status");
const orderId = document.querySelector("#order-id");
const orderSummary = document.querySelector("#order-summary");
const lookupForm = document.querySelector("#order-lookup-form");
const lookupInput = document.querySelector("#order-lookup-id");
const lookupStatus = document.querySelector("#lookup-status");
const loadOrdersButton = document.querySelector("#load-orders");
const adminStatus = document.querySelector("#admin-status");
const ordersList = document.querySelector("#orders-list");

let products = [];
const cart = new Map();

function formatMoney(value) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD"
    }).format(Number(value || 0));
}

function setStatus(element, message, type = "") {
    element.textContent = message;
    element.className = type ? `status ${type}` : "status";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function formatDate(value) {
    if (!value) {
        return "unknown date";
    }

    return new Intl.DateTimeFormat("en-US", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function formatApiError(data, fallback) {
    const fieldErrors = data?.fieldErrors
        ? Object.entries(data.fieldErrors).map(([field, message]) => `${field}: ${message}`)
        : [];

    if (fieldErrors.length > 0) {
        return `${data?.message || "Validation failed"} (${fieldErrors.join(", ")})`;
    }

    return data?.message || data?.error || fallback;
}

async function fetchJson(url, options) {
    const response = await fetch(url, options);
    const text = await response.text();
    let data = null;

    try {
        data = text ? JSON.parse(text) : null;
    } catch (error) {
        data = {message: text || "Unexpected response from server"};
    }

    if (!response.ok) {
        const message = formatApiError(data, `Request failed with ${response.status}`);
        throw new Error(message);
    }

    return data;
}

async function loadProducts() {
    setStatus(catalogStatus, "Loading catalog...");
    productList.innerHTML = "";
    productCount.textContent = "Loading...";

    try {
        products = await fetchJson("/api/products");
        renderProducts();
        setStatus(catalogStatus, "");
    } catch (error) {
        productCount.textContent = "Unavailable";
        setStatus(catalogStatus, error.message, "error");
    }
}

function renderProducts() {
    productCount.textContent = `${products.length} item${products.length === 1 ? "" : "s"}`;
    productList.innerHTML = "";

    products.forEach((product) => {
        const selected = cart.get(product.id) || 1;
        const card = document.createElement("article");
        card.className = "product-card";
        card.innerHTML = `
            <div>
                <div class="product-title">
                    <h3>${escapeHtml(product.name)}</h3>
                    <span class="price">${formatMoney(product.price)}</span>
                </div>
                <p class="product-description">${escapeHtml(product.description)}</p>
                <div class="meta">
                    <span class="pill">${escapeHtml(product.type)}</span>
                    <span class="pill">${product.stock} in stock</span>
                    <span class="pill">${product.available ? "Available" : "Unavailable"}</span>
                </div>
            </div>
            <div class="product-actions">
                <div class="quantity-control">
                    <input aria-label="Quantity for ${product.name}" min="1" max="${product.stock}" type="number" value="${selected}">
                    <button class="small-button" type="button" ${!product.available || product.stock < 1 ? "disabled" : ""}>Add</button>
                </div>
            </div>
        `;

        const input = card.querySelector("input");
        const button = card.querySelector("button");
        button.addEventListener("click", () => {
            const quantity = Math.max(1, Math.min(Number(input.value || 1), product.stock));
            cart.set(product.id, quantity);
            renderCart();
            setStatus(orderStatus, `${product.name} added to current order.`, "success");
        });

        productList.appendChild(card);
    });
}

function renderCart() {
    cartList.innerHTML = "";
    cartEmpty.hidden = cart.size > 0;

    let total = 0;
    cart.forEach((quantity, productId) => {
        const product = products.find((item) => item.id === productId);
        if (!product) {
            return;
        }

        total += Number(product.price) * quantity;
        const item = document.createElement("div");
        item.className = "cart-item";
        item.innerHTML = `
            <div>
                <strong>${escapeHtml(product.name)}</strong>
                <span class="meta">${quantity} x ${formatMoney(product.price)}</span>
            </div>
            <button class="text-button" type="button">Remove</button>
        `;
        item.querySelector("button").addEventListener("click", () => {
            cart.delete(productId);
            renderCart();
        });
        cartList.appendChild(item);
    });

    cartTotal.textContent = formatMoney(total);
    checkoutButton.disabled = cart.size === 0;
}

async function createOrder() {
    if (cart.size === 0) {
        return;
    }

    checkoutButton.disabled = true;
    setStatus(orderStatus, "Placing order...");

    const items = [...cart.entries()].map(([productId, quantity]) => ({productId, quantity}));

    try {
        const order = await fetchJson("/api/orders", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({items})
        });

        cart.clear();
        renderCart();
        renderOrder(order);
        await loadProducts();
        setStatus(orderStatus, "Order placed.", "success");
        lookupInput.value = order.id;
    } catch (error) {
        setStatus(orderStatus, error.message, "error");
        checkoutButton.disabled = false;
    }
}

function renderOrder(order) {
    orderId.textContent = `#${order.id}`;
    orderSummary.hidden = false;
    orderSummary.innerHTML = `
        <p class="meta">Status: ${escapeHtml(order.status)} | Created: ${formatDate(order.createdAt)} | Total: ${formatMoney(order.totalPrice)}</p>
        <div class="product-list">
            ${order.items.map((item) => `
                <div class="order-item">
                    <strong>${escapeHtml(item.productName)}</strong>
                    <span class="meta">${item.quantity} x ${formatMoney(item.price)} | ${escapeHtml(item.productType)}</span>
                </div>
            `).join("")}
        </div>
    `;
}

async function loadOrderById(orderIdToLoad) {
    if (!orderIdToLoad || Number(orderIdToLoad) < 1) {
        setStatus(lookupStatus, "Enter a valid order ID.", "error");
        return;
    }

    setStatus(lookupStatus, "Loading order...");

    try {
        const order = await fetchJson(`/api/orders/${orderIdToLoad}`);
        renderOrder(order);
        setStatus(lookupStatus, "Order loaded.", "success");
    } catch (error) {
        orderSummary.hidden = true;
        orderId.textContent = "";
        setStatus(lookupStatus, error.message, "error");
    }
}

async function loadAllOrders() {
    setStatus(adminStatus, "Loading orders...");
    ordersList.innerHTML = "";

    try {
        const orders = await fetchJson("/api/admin/orders");
        renderOrdersList(orders);
        setStatus(adminStatus, orders.length === 0 ? "No orders yet." : `${orders.length} order${orders.length === 1 ? "" : "s"} loaded.`, "success");
    } catch (error) {
        setStatus(adminStatus, error.message, "error");
    }
}

function renderOrdersList(orders) {
    ordersList.innerHTML = "";

    orders.forEach((order) => {
        const item = document.createElement("article");
        item.className = "order-row";
        item.innerHTML = `
            <div>
                <strong>Order #${order.id}</strong>
                <span class="meta">${escapeHtml(order.status)} | ${formatDate(order.createdAt)} | ${formatMoney(order.totalPrice)}</span>
            </div>
            <button class="small-button" type="button">View</button>
        `;
        item.querySelector("button").addEventListener("click", () => {
            lookupInput.value = order.id;
            renderOrder(order);
            setStatus(lookupStatus, "Order loaded from all orders.", "success");
            document.querySelector("#orders").scrollIntoView({behavior: "smooth", block: "start"});
        });
        ordersList.appendChild(item);
    });
}

checkoutButton.addEventListener("click", createOrder);
clearCartButton.addEventListener("click", () => {
    cart.clear();
    renderCart();
    setStatus(orderStatus, "");
});
refreshButton.addEventListener("click", loadProducts);
lookupForm.addEventListener("submit", (event) => {
    event.preventDefault();
    loadOrderById(lookupInput.value);
});
loadOrdersButton.addEventListener("click", loadAllOrders);

loadProducts();
