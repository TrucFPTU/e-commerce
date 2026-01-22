(function () {
    let stompClient = null;
    let activeConversationId = null;
    let oldestMessageId = null;
    let isLoadingOlder = false;
    let pendingFiles = [];

    const me = Number(window.__STAFF_CURRENT_USER_ID || 0);

    const el = {
        list: document.getElementById("staffConvList"),
        with: document.getElementById("staffChatWith"),
        box: document.getElementById("staffChatMessages"),
        text: document.getElementById("staffChatText"),
        send: document.getElementById("staffChatSend"),
    };


    function showSessionBanner(msg) {
        const id = "staffSessionBanner";
        let bar = document.getElementById(id);
        if (!bar) {
            bar = document.createElement("div");
            bar.id = id;
            bar.style.position = "fixed";
            bar.style.left = "12px";
            bar.style.right = "12px";
            bar.style.bottom = "12px";
            bar.style.zIndex = "99999";
            bar.style.padding = "10px 12px";
            bar.style.borderRadius = "12px";
            bar.style.background = "#111827";
            bar.style.color = "#fff";
            bar.style.fontSize = "13px";
            bar.style.boxShadow = "0 10px 25px rgba(0,0,0,.25)";
            bar.style.display = "flex";
            bar.style.gap = "10px";
            bar.style.alignItems = "center";
            bar.style.justifyContent = "space-between";

            const left = document.createElement("div");
            left.id = id + "_text";

            const btn = document.createElement("button");
            btn.type = "button";
            btn.textContent = "Reload";
            btn.style.border = "none";
            btn.style.padding = "8px 10px";
            btn.style.borderRadius = "10px";
            btn.style.cursor = "pointer";
            btn.onclick = () => window.location.reload();

            bar.appendChild(left);
            bar.appendChild(btn);
            document.body.appendChild(bar);
        }
        const text = document.getElementById(id + "_text");
        if (text) text.textContent = msg || "Phiên đăng nhập có thể đã hết hạn. Hãy reload trang.";
    }


    const unreadMap = new Map();
    const originalOrderKey = "__origIndex";

    function ensureDot(item) {
        let dot = item.querySelector(".staff-unread-dot");
        if (dot) return dot;

        dot = document.createElement("span");
        dot.className = "staff-unread-dot";
        dot.style.display = "inline-block";
        dot.style.width = "8px";
        dot.style.height = "8px";
        dot.style.borderRadius = "999px";
        dot.style.background = "#e11d48";
        dot.style.marginLeft = "8px";
        dot.style.verticalAlign = "middle";

        item.appendChild(dot);
        return dot;
    }

    function setUnread(convId, isUnread, msgTs) {
        if (!el.list) return;
        const item = el.list.querySelector(`.conv-item[data-conv-id="${convId}"]`);
        if (!item) return;

        if (item.getAttribute(originalOrderKey) == null) {
            const idx = Array.from(el.list.querySelectorAll(".conv-item")).indexOf(item);
            item.setAttribute(originalOrderKey, String(idx));
        }

        if (isUnread) {
            unreadMap.set(Number(convId), Number(msgTs || Date.now()));
            ensureDot(item).style.display = "inline-block";
        } else {
            unreadMap.delete(Number(convId));
            const dot = item.querySelector(".staff-unread-dot");
            if (dot) dot.style.display = "none";
        }

        reorderConversationList();
    }

    function reorderConversationList() {
        if (!el.list) return;

        const items = Array.from(el.list.querySelectorAll(".conv-item"));
        items.sort((a, b) => {
            const aId = Number(a.getAttribute("data-conv-id"));
            const bId = Number(b.getAttribute("data-conv-id"));

            const aUnread = unreadMap.has(aId);
            const bUnread = unreadMap.has(bId);

            if (aUnread && !bUnread) return -1;
            if (!aUnread && bUnread) return 1;

            if (aUnread && bUnread) {
                const at = unreadMap.get(aId) || 0;
                const bt = unreadMap.get(bId) || 0;
                if (bt !== at) return bt - at;
            }

            const ai = Number(a.getAttribute(originalOrderKey) || 0);
            const bi = Number(b.getAttribute(originalOrderKey) || 0);
            return ai - bi;
        });

        const frag = document.createDocumentFragment();
        items.forEach((it) => frag.appendChild(it));
        el.list.appendChild(frag);
    }


    function getCsrf() {
        const metaToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
        const metaHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
        if (metaToken && metaHeader) return { header: metaHeader, token: metaToken };

        const input = document.querySelector('input[name="_csrf"]');
        if (input && input.value) return { header: "X-CSRF-TOKEN", token: input.value };

        const m = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        if (m) return { header: "X-XSRF-TOKEN", token: decodeURIComponent(m[1]) };

        return null;
    }


    async function fetchJsonSafe(url, opts) {
        let res;
        try {
            res = await fetch(url, Object.assign({ credentials: "same-origin" }, (opts || {})));
        } catch (e) {
            return { ok: false, status: 0, reason: "network_error" };
        }

        if (res.status === 401) return { ok: false, status: 401, reason: "unauthorized" };
        if (!res.ok) return { ok: false, status: res.status, reason: "http_error" };

        const ct = (res.headers.get("content-type") || "").toLowerCase();
        if (!ct.includes("application/json")) {
            return { ok: false, status: res.status, reason: "non_json" };
        }

        try {
            const data = await res.json();
            return { ok: true, data };
        } catch (e) {
            return { ok: false, status: res.status, reason: "bad_json" };
        }
    }

    async function loadLatest(conversationId) {
        const r = await fetchJsonSafe(`/staff/chat/messages/latest?conversationId=${conversationId}&limit=30`);
        if (!r.ok) {
            if (r.status === 401) showSessionBanner("Staff: phiên đăng nhập hết hạn (401). Hãy reload/login lại.");
            return null;
        }
        return Array.isArray(r.data) ? r.data : null;
    }

    async function loadOlder(conversationId, beforeMessageId) {
        const r = await fetchJsonSafe(`/staff/chat/messages/older?conversationId=${conversationId}&beforeMessageId=${beforeMessageId}&limit=30`);
        if (!r.ok) {
            if (r.status === 401) showSessionBanner("Staff: phiên đăng nhập hết hạn (401). Hãy reload/login lại.");
            return null;
        }
        return Array.isArray(r.data) ? r.data : null;
    }

    async function fetchConversationSummary(conversationId) {
        const r = await fetchJsonSafe(`/staff/chat/conversations/summary?conversationId=${conversationId}`);
        return r.ok ? r.data : null;
    }

    function neutralizeLinks(item) {
        const links = item.querySelectorAll("a");
        links.forEach(a => {
            a.setAttribute("href", "javascript:void(0)");
            a.removeAttribute("target");
            a.removeAttribute("rel");
        });
    }

    function updateCustomerNameInItem(item, name) {
        let nameEl = item.querySelector(".staff-conv-name");
        if (!nameEl) {
            nameEl = document.createElement("span");
            nameEl.className = "staff-conv-name";
            nameEl.style.flex = "1";
            nameEl.style.minWidth = "0";
            nameEl.style.whiteSpace = "nowrap";
            nameEl.style.overflow = "hidden";
            nameEl.style.textOverflow = "ellipsis";

            item.style.display = "flex";
            item.style.alignItems = "center";
            item.style.gap = "8px";
            item.appendChild(nameEl);
        }
        nameEl.textContent = name || "Khách hàng";
        item.title = name || "Khách hàng";
    }


    async function ensureConversationItemExists(convId) {
        if (!el.list) return;

        let item = el.list.querySelector(`.conv-item[data-conv-id="${convId}"]`);
        if (item) return item;

        item = document.createElement("div");
        item.className = "conv-item";
        item.setAttribute("data-conv-id", String(convId));

        item.style.padding = "10px 12px";
        item.style.borderRadius = "10px";
        item.style.cursor = "pointer";
        item.style.userSelect = "none";


        const placeholderName = "Khách hàng";
        item.setAttribute("data-customer-name", placeholderName);
        updateCustomerNameInItem(item, placeholderName);

        const items = Array.from(el.list.querySelectorAll(".conv-item"));
        const maxIdx = items.reduce((mx, it) => Math.max(mx, Number(it.getAttribute(originalOrderKey) || 0)), -1);
        item.setAttribute(originalOrderKey, String(maxIdx + 1));

        const dot = ensureDot(item);
        dot.style.display = "none";


        el.list.insertBefore(item, el.list.firstChild);

        const summary = await fetchConversationSummary(convId);
        const customerName = summary?.customerName;
        if (customerName) {
            item.setAttribute("data-customer-name", customerName);
            updateCustomerNameInItem(item, customerName);
        }

        return item;
    }


    function pad2(n) { return n < 10 ? "0" + n : "" + n; }

    function toLocalParts(iso) {
        const d = new Date(iso);
        const fmtDate = new Intl.DateTimeFormat("vi-VN", { timeZone: "Asia/Ho_Chi_Minh", year: "numeric", month: "2-digit", day: "2-digit" });
        const fmtTime = new Intl.DateTimeFormat("vi-VN", { timeZone: "Asia/Ho_Chi_Minh", hour: "2-digit", minute: "2-digit" });

        const parts = fmtDate.formatToParts(d);
        const dd = parts.find(p => p.type === "day")?.value || "01";
        const mm = parts.find(p => p.type === "month")?.value || "01";
        const yy = parts.find(p => p.type === "year")?.value || "1970";
        const dayKey = `${yy}-${mm}-${dd}`;

        return { dayKey, timeText: fmtTime.format(d) };
    }

    function dayLabel(dayKey) {
        const [y, m, d] = dayKey.split("-").map(Number);
        const nowKey = toLocalParts(new Date().toISOString()).dayKey;
        const [ny, nm, nd] = nowKey.split("-").map(Number);

        const a = new Date(Date.UTC(y, m - 1, d));
        const b = new Date(Date.UTC(ny, nm - 1, nd));
        const diffDays = Math.round((b - a) / (24 * 3600 * 1000));

        if (diffDays === 0) return "Hôm nay";
        if (diffDays === 1) return "Hôm qua";
        if (diffDays === 2) return "Hôm kia";
        return `${pad2(d)}/${pad2(m)}/${y}`;
    }

    function insertDaySeparator(box, dayKey, where) {
        const sep = document.createElement("div");
        sep.setAttribute("data-day-key", dayKey);
        sep.textContent = dayLabel(dayKey);
        sep.style.textAlign = "center";
        sep.style.color = "#888";
        sep.style.fontSize = "12px";
        sep.style.margin = "10px 0";
        sep.style.userSelect = "none";

        if (where === "prepend") box.insertBefore(sep, box.firstChild);
        else box.appendChild(sep);
    }

    function lastRenderedDayKey(box) {
        for (let i = box.childNodes.length - 1; i >= 0; i--) {
            const n = box.childNodes[i];
            if (n?.getAttribute && n.getAttribute("data-day-key")) return n.getAttribute("data-day-key");
            if (n?.getAttribute && n.getAttribute("data-day-key-msg")) return n.getAttribute("data-day-key-msg");
        }
        return null;
    }

    function hasMessageId(box, id) {
        if (!id) return false;
        return !!box.querySelector(`[data-msg-id="${id}"]`);
    }

    function openImageViewer(src) {
        if (!src) return;

        const old = document.getElementById("staffChatImgViewer");
        if (old) old.remove();

        const overlay = document.createElement("div");
        overlay.id = "staffChatImgViewer";
        overlay.style.position = "fixed";
        overlay.style.left = "0";
        overlay.style.top = "0";
        overlay.style.right = "0";
        overlay.style.bottom = "0";
        overlay.style.background = "rgba(0,0,0,0.85)";
        overlay.style.zIndex = "99999";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";
        overlay.style.padding = "20px";
        overlay.style.cursor = "zoom-out";

        const img = document.createElement("img");
        img.src = src;
        img.style.maxWidth = "95vw";
        img.style.maxHeight = "95vh";
        img.style.objectFit = "contain";
        img.style.borderRadius = "10px";

        const close = () => overlay.remove();
        overlay.addEventListener("click", close);
        img.addEventListener("click", (e) => e.stopPropagation());

        document.addEventListener("keydown", function esc(e) {
            if (e.key === "Escape") {
                close();
                document.removeEventListener("keydown", esc);
            }
        });

        overlay.appendChild(img);
        document.body.appendChild(overlay);
    }

    function appendMessage(m, scrollToBottom, where) {
        const box = el.box;
        if (!box) return;

        if (m.messageId && hasMessageId(box, m.messageId)) return;

        const isMe = me && Number(m.senderId) === me;
        const { dayKey, timeText } = toLocalParts(m.createdAt);

        if (where !== "prepend") {
            const lastDay = lastRenderedDayKey(box);
            if (!lastDay || lastDay !== dayKey) insertDaySeparator(box, dayKey, "append");
        }

        const row = document.createElement("div");
        row.className = "msg-row " + (isMe ? "msg-me" : "msg-other");
        row.setAttribute("data-day-key-msg", dayKey);
        row.setAttribute("data-msg-id", m.messageId || "");

        const bubble = document.createElement("div");
        bubble.className = "bubble " + (isMe ? "me" : "");
        bubble.innerHTML = "";

        const type = m.type || "TEXT";

        if (type === "TEXT") {
            const t = document.createElement("div");
            t.textContent = m.content || "";
            bubble.appendChild(t);
        } else if (type === "IMAGE" && m.attachmentUrl) {
            const img = document.createElement("img");
            img.src = m.attachmentUrl;
            img.style.maxWidth = "220px";
            img.style.borderRadius = "10px";
            img.style.display = "block";
            img.style.cursor = "zoom-in";
            img.addEventListener("click", () => openImageViewer(m.attachmentUrl));
            bubble.appendChild(img);

            const cap = document.createElement("div");
            cap.textContent = m.originalName || m.content || "";
            cap.style.fontSize = "12px";
            cap.style.color = "#555";
            cap.style.marginTop = "6px";
            bubble.appendChild(cap);
        } else if (type === "FILE" && m.attachmentUrl) {
            const a = document.createElement("a");
            a.href = m.attachmentUrl;
            a.target = "_blank";
            a.rel = "noopener";
            a.textContent = m.originalName || m.content || "Tải file";
            a.style.textDecoration = "underline";
            bubble.appendChild(a);
        } else {
            const t = document.createElement("div");
            t.textContent = m.content || "";
            bubble.appendChild(t);
        }

        const meta = document.createElement("div");
        meta.textContent = timeText;
        meta.style.fontSize = "11px";
        meta.style.color = "#888";
        meta.style.marginTop = "3px";
        bubble.appendChild(meta);

        row.appendChild(bubble);

        if (where === "prepend") box.insertBefore(row, box.firstChild);
        else box.appendChild(row);

        if (scrollToBottom) box.scrollTop = box.scrollHeight;
    }

    function renderMessages(messages) {
        const box = el.box;
        if (!box) return;

        box.innerHTML = "";

        let prevDay = null;
        (messages || []).forEach((m) => {
            const { dayKey } = toLocalParts(m.createdAt);
            if (dayKey !== prevDay) {
                insertDaySeparator(box, dayKey, "append");
                prevDay = dayKey;
            }
            appendMessage(m, false, "append");
        });

        box.scrollTop = box.scrollHeight;
        oldestMessageId = (messages && messages.length) ? Number(messages[0].messageId) : null;
    }

    function prependMessages(messages) {
        if (!messages || !messages.length) return;

        const box = el.box;
        const prevHeight = box.scrollHeight;

        let prevDay = null;
        for (let i = 0; i < messages.length; i++) {
            const m = messages[i];
            const { dayKey } = toLocalParts(m.createdAt);

            if (dayKey !== prevDay) {
                insertDaySeparator(box, dayKey, "prepend");
                prevDay = dayKey;
            }
            appendMessage(m, false, "prepend");
        }

        oldestMessageId = Number(messages[0].messageId);

        const newHeight = box.scrollHeight;
        box.scrollTop = newHeight - prevHeight;
    }

    function connectWsOnce() {
        if (stompClient) return;

        const sock = new SockJS("/ws-chat");
        stompClient = Stomp.over(sock);
        stompClient.debug = null;

        stompClient.connect(
            {},
            () => {
                stompClient.subscribe("/user/queue/messages", (frame) => {
                    (async () => {
                        const msg = JSON.parse(frame.body);
                        const msgConvId = Number(msg.conversationId);

                        await ensureConversationItemExists(msgConvId);

                        if (activeConversationId && msgConvId === activeConversationId) {
                            appendMessage(msg, true, "append");
                        } else {
                            const ts = msg.createdAt ? new Date(msg.createdAt).getTime() : Date.now();
                            setUnread(msgConvId, true, ts);
                        }
                    })().catch(err => console.error(err));
                });

            },
            (err) => {
                console.error("WS connect error:", err);
                showSessionBanner("Không kết nối được realtime (WS). Bạn có thể reload trang.");
            }
        );
    }

    function ensureStaffSearchUi() {
        if (!el.list) return;
        if (document.getElementById("staffChatSearch")) return;

        const wrap = document.createElement("div");
        wrap.style.marginBottom = "10px";

        const input = document.createElement("input");
        input.id = "staffChatSearch";
        input.type = "text";
        input.placeholder = "Tìm khách hàng...";
        input.autocomplete = "off";

        input.style.width = "100%";
        input.style.padding = "10px 12px";
        input.style.border = "1px solid #ddd";
        input.style.borderRadius = "10px";
        input.style.outline = "none";
        input.style.boxSizing = "border-box";

        wrap.appendChild(input);

        const parent = el.list.parentElement;
        if (parent) parent.insertBefore(wrap, el.list);
        else el.list.insertAdjacentElement("beforebegin", wrap);

        function normalize(s) {
            return (s || "")
                .toLowerCase()
                .normalize("NFD")
                .replace(/[\u0300-\u036f]/g, "")
                .trim();
        }

        function applyFilter() {
            const q = normalize(input.value);
            const items = Array.from(el.list.querySelectorAll(".conv-item"));
            items.forEach((item) => {
                const name = item.getAttribute("data-customer-name") || item.textContent || "";
                const ok = normalize(name).includes(q);
                item.style.display = ok ? "" : "none";
            });
        }

        input.addEventListener("input", applyFilter);
        input.addEventListener("keydown", (e) => {
            if (e.key === "Escape") {
                input.value = "";
                applyFilter();
            }
        });
    }

    function ensureAttachmentUi() {
        if (!el.text) return;

        const inputWrap = el.text.parentElement;
        if (!inputWrap) return;

        if (document.getElementById("staffAttachBtn")) return;

        const attachBtn = document.createElement("button");
        attachBtn.id = "staffAttachBtn";
        attachBtn.type = "button";
        attachBtn.textContent = "📎";
        attachBtn.style.padding = "9px 12px";
        attachBtn.style.border = "none";
        attachBtn.style.borderRadius = "10px";
        attachBtn.style.cursor = "pointer";

        const fileInput = document.createElement("input");
        fileInput.id = "staffAttachInput";
        fileInput.type = "file";
        fileInput.multiple = true;
        fileInput.style.display = "none";

        const preview = document.createElement("div");
        preview.id = "staffAttachPreview";
        preview.style.display = "flex";
        preview.style.flexWrap = "wrap";
        preview.style.gap = "6px";
        preview.style.marginTop = "8px";

        inputWrap.insertBefore(attachBtn, el.text);
        inputWrap.appendChild(fileInput);

        if (inputWrap.parentElement) {
            inputWrap.parentElement.insertBefore(preview, inputWrap);
        }

        attachBtn.addEventListener("click", () => fileInput.click());

        fileInput.addEventListener("change", () => {
            const files = Array.from(fileInput.files || []);
            if (!files.length) return;

            pendingFiles.push(...files);
            fileInput.value = "";
            renderPreview();
        });

        function renderPreview() {
            preview.innerHTML = "";

            pendingFiles.forEach((f, idx) => {
                const chip = document.createElement("div");
                chip.style.border = "1px solid #ddd";
                chip.style.borderRadius = "10px";
                chip.style.padding = "6px 8px";
                chip.style.background = "#fff";
                chip.style.display = "flex";
                chip.style.alignItems = "center";
                chip.style.gap = "8px";
                chip.style.maxWidth = "320px";

                if (f.type && f.type.startsWith("image/")) {
                    const img = document.createElement("img");
                    img.style.width = "32px";
                    img.style.height = "32px";
                    img.style.objectFit = "cover";
                    img.style.borderRadius = "6px";
                    img.src = URL.createObjectURL(f);
                    chip.appendChild(img);
                }

                const name = document.createElement("div");
                name.textContent = f.name;
                name.style.fontSize = "12px";
                name.style.whiteSpace = "nowrap";
                name.style.overflow = "hidden";
                name.style.textOverflow = "ellipsis";
                name.style.maxWidth = "200px";
                chip.appendChild(name);

                const x = document.createElement("button");
                x.type = "button";
                x.textContent = "✕";
                x.style.border = "none";
                x.style.background = "transparent";
                x.style.cursor = "pointer";
                x.addEventListener("click", () => {
                    pendingFiles.splice(idx, 1);
                    renderPreview();
                });
                chip.appendChild(x);

                preview.appendChild(chip);
            });
        }

        window.__staffRenderPreview = renderPreview;
    }

    async function sendMessage() {
        if (!activeConversationId) return;

        const text = (el.text?.value || "").trim();

        if (text && stompClient) {
            try {
                stompClient.send("/app/chat.send", {}, JSON.stringify({ conversationId: activeConversationId, text }));
                el.text.value = "";
            } catch (e) {
                console.error("send stomp error:", e);
                showSessionBanner("Không gửi được tin (WS). Hãy reload trang.");
                return;
            }
        }

        if (!pendingFiles.length) return;

        const fd = new FormData();
        fd.append("conversationId", String(activeConversationId));
        pendingFiles.forEach((f) => fd.append("files", f));

        const csrf = getCsrf();
        const headers = {};
        if (csrf) headers[csrf.header] = csrf.token;

        const res = await fetch("/chat/attachments", {
            method: "POST",
            body: fd,
            credentials: "same-origin",
            headers,
        });

        if (res.status === 401) {
            showSessionBanner("Staff: phiên đăng nhập hết hạn (401) khi upload. Hãy reload/login lại.");
            return;
        }

        const ct = (res.headers.get("content-type") || "").toLowerCase();
        if (!ct.includes("application/json")) return;
        if (!res.ok) return;

        const uploadedMessages = await res.json();
        if (Array.isArray(uploadedMessages)) {
            uploadedMessages.forEach((msg) => {
                if (Number(msg.conversationId) === Number(activeConversationId)) {
                    appendMessage(msg, true, "append");
                }
            });
        }

        pendingFiles = [];
        if (window.__staffRenderPreview) window.__staffRenderPreview();
    }


    async function onSelectConversation(item) {
        const convId = Number(item.getAttribute("data-conv-id"));
        const customerName = item.getAttribute("data-customer-name") || "Khách hàng";

        activeConversationId = convId;
        setUnread(convId, false);

        if (el.with) el.with.textContent = customerName;
        if (el.text) el.text.disabled = false;
        if (el.send) el.send.disabled = false;

        if (el.list) {
            [...el.list.querySelectorAll(".conv-item")].forEach((x) => (x.style.background = ""));
            item.style.background = "#f3f4f6";
        }

        const messages = await loadLatest(convId);
        if (messages) renderMessages(messages);
    }

    async function onScrollLoadOlder() {
        if (!activeConversationId || !oldestMessageId || isLoadingOlder) return;
        if (!el.box) return;
        if (el.box.scrollTop > 40) return;

        isLoadingOlder = true;
        try {
            const older = await loadOlder(activeConversationId, oldestMessageId);
            if (older && older.length) prependMessages(older);
            else if (older && older.length === 0) oldestMessageId = null;
        } finally {
            isLoadingOlder = false;
        }
    }

    function showEmptyState() {
        activeConversationId = null;
        oldestMessageId = null;
        isLoadingOlder = false;

        if (el.with) el.with.textContent = "Chọn khách hàng để bắt đầu tư vấn";
        if (el.box) el.box.innerHTML = `<div style="padding:14px;color:#666;">Chọn khách hàng bên trái để bắt đầu tư vấn.</div>`;

        if (el.text) {
            el.text.value = "";
            el.text.disabled = true;
            el.text.placeholder = "Chọn khách hàng để bắt đầu...";
        }
        if (el.send) el.send.disabled = true;
        if (el.list) {
            [...el.list.querySelectorAll(".conv-item")].forEach(x => x.style.background = "");
        }
    }

    function bind() {
        if (!el.list || !el.box || !el.text || !el.send) return;

        ensureStaffSearchUi();
        ensureAttachmentUi();
        connectWsOnce();


        el.list.addEventListener("click", (e) => {
            const link = e.target.closest("a");
            if (link) e.preventDefault();

            const item = e.target.closest(".conv-item");
            if (item) onSelectConversation(item);
        });

        el.send.addEventListener("click", (e) => {
            e.preventDefault();
            sendMessage();
        });

        el.text.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                sendMessage();
            }
        });

        el.box.addEventListener("scroll", onScrollLoadOlder);

        Array.from(el.list.querySelectorAll(".conv-item")).forEach((item, idx) => {
            item.setAttribute(originalOrderKey, String(idx));
            neutralizeLinks(item);
            const dot = ensureDot(item);
            dot.style.display = "none";
        });

        showEmptyState();

    }

    bind();
})();
