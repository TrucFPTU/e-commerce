(function () {
    let stompClient = null;
    let conversationId = null;
    let oldestMessageId = null;
    let isBootstrapped = false;
    let isLoadingOlder = false;
    let isBound = false;
    let pendingFiles = []; 
    let hasUnread = false;
    let lastSeenMessageId = null;



    const $ = (id) => document.getElementById(id);

    function log(...args) { /* console.log(...args); */ }

    async function fetchJsonOrRedirect(url) {
        const res = await fetch(url, { credentials: "same-origin" });

        if (res.status === 401) {
            window.location.href = "/login";
            return null;
        }

        const ct = (res.headers.get("content-type") || "").toLowerCase();
        if (!ct.includes("application/json")) {
            window.location.href = "/login";
            return null;
        }

        if (!res.ok) return null;

        try {
            return await res.json();
        } catch (e) {
            return null;
        }
    }


    function openBox() {
        $("chatBox").classList.remove("hidden");
        setUnread(false);
        const last = getLastRenderedMessageId();
        if (last) lastSeenMessageId = last;

        if (!isBootstrapped) bootstrap();
    }

    function closeBox() {
        $("chatBox").classList.add("hidden");
    }
    function getLastRenderedMessageId() {
        const box = $("chatMessages");
        const nodes = box ? box.querySelectorAll("[data-msg-id]") : [];
        if (!nodes.length) return null;
        const id = nodes[nodes.length - 1].getAttribute("data-msg-id");
        return id ? Number(id) : null;
    }


    async function bootstrap() {
        const data = await fetchJsonOrRedirect(`/chat/bootstrap?limit=30`);
        if (!data) {
            log("bootstrap failed or not logged in");
            return;
        }

        const staffNameEl = document.getElementById("chatStaffName");
        if (staffNameEl) {
            staffNameEl.textContent = data["staffName"] ? data["staffName"] : "Đang kết nối...";
        }

        conversationId = Number(data["conversationId"]);
        oldestMessageId = data["oldestMessageId"] ? Number(data["oldestMessageId"]) : null;

        if (data.currentUserId) {
            window.__CHAT_CURRENT_USER_ID = Number(data.currentUserId);
        }

        renderMessages(data.messages || []);
        isBootstrapped = true;

        connectWs();
    }


    function connectWs() {
        if (stompClient) return;

        const sock = new SockJS("/ws-chat");
        stompClient = Stomp.over(sock);
        stompClient.debug = null;

        stompClient.connect({}, () => {
            log("WS connected");
            stompClient.subscribe("/user/queue/messages", (frame) => {
                const msg = JSON.parse(frame.body);
                const msgConvId = Number(msg.conversationId);

                const isMine = isMeMessage(msg);

                if (conversationId && msgConvId === conversationId) {
                    appendMessage(msg, true);

                    const isClosed = $("chatBox").classList.contains("hidden");
                    if (isClosed && !isMine) setUnread(true);

                    if (!isClosed) {
                        lastSeenMessageId = msg.messageId || lastSeenMessageId;
                        setUnread(false);
                    }
                } else {
                    const isClosed = $("chatBox").classList.contains("hidden");
                    if (isClosed && !isMine) setUnread(true);
                }
            });

        }, (err) => log("WS connect error", err));
    }

    async function sendText() {
        const text = $("chatText").value.trim();

        if (text && conversationId && stompClient) {
            stompClient.send("/app/chat.send", {}, JSON.stringify({ conversationId, text }));
            $("chatText").value = "";
        }

        if (pendingFiles.length && conversationId) {
            const fd = new FormData();
            fd.append("conversationId", String(conversationId));
            pendingFiles.forEach(f => fd.append("files", f));

            const res = await fetch("/chat/attachments", {
                method: "POST",
                body: fd,
                credentials: "same-origin",
            });

            if (res.status === 401) {
                window.location.href = "/login";
                return;
            }

            if (!res.ok) {
                return;
            }

            const ct = (res.headers.get("content-type") || "").toLowerCase();
            if (!ct.includes("application/json")) {
                window.location.href = "/login";
                return;
            }

            const uploadedMessages = await res.json();


            if (Array.isArray(uploadedMessages)) {
                uploadedMessages.forEach(msg => {

                    if (Number(msg.conversationId) === Number(conversationId)) {
                        appendMessage(msg, true);
                    }
                });
            }

            pendingFiles = [];
            if (window.__chatWidgetRenderPreview) window.__chatWidgetRenderPreview();
        }

    }

    function hasMessageId(box, id) {
        if (!id) return false;
        return !!box.querySelector(`[data-msg-id="${id}"]`);
    }

    function renderMessages(messages) {
        const box = $("chatMessages");
        box.innerHTML = "";

        let prevDay = null;
        (messages || []).forEach(m => {
            const { dayKey } = toLocalParts(m.createdAt);
            if (dayKey !== prevDay) {
                insertDaySeparator(box, dayKey, "append");
                prevDay = dayKey;
            }
            appendMessage(m, false);
        });

        box.scrollTop = box.scrollHeight;
    }

    function pad2(n) { return n < 10 ? "0" + n : "" + n; }

    function toLocalParts(iso) {
        const d = new Date(iso);
        const fmtDate = new Intl.DateTimeFormat("vi-VN", {
            timeZone: "Asia/Ho_Chi_Minh",
            year: "numeric", month: "2-digit", day: "2-digit"
        });
        const fmtTime = new Intl.DateTimeFormat("vi-VN", {
            timeZone: "Asia/Ho_Chi_Minh",
            hour: "2-digit", minute: "2-digit"
        });

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


    function isMeMessage(m) {
        const me = window.__CHAT_CURRENT_USER_ID;
        return me && Number(m.senderId) === Number(me);
    }

    function appendMessage(m, scrollToBottom) {
        const box = $("chatMessages");
        if (m.messageId && hasMessageId(box, m.messageId)) return;

        const isMe = isMeMessage(m);

        const { dayKey, timeText } = toLocalParts(m.createdAt);

        const lastDay = lastRenderedDayKey(box);
        if (!lastDay || lastDay !== dayKey) {
            insertDaySeparator(box, dayKey, "append");
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
            bubble.appendChild(img);
            img.style.cursor = "zoom-in";
            img.addEventListener("click", () => openImageViewer(m.attachmentUrl));


            if (m.originalName || m.content) {
                const cap = document.createElement("div");
                cap.textContent = m.originalName || m.content || "";
                cap.style.fontSize = "12px";
                cap.style.color = "#555";
                cap.style.marginTop = "6px";
                bubble.appendChild(cap);
            }
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
        box.appendChild(row);

        if (scrollToBottom) box.scrollTop = box.scrollHeight;
    }


    async function loadOlderIfNeeded() {
        if (!conversationId || !oldestMessageId || isLoadingOlder) return;

        const box = $("chatMessages");
        if (box.scrollTop > 40) return;

        isLoadingOlder = true;
        const prevHeight = box.scrollHeight;

        try {
            const older = await fetchJsonOrRedirect(
                `/chat/older?conversationId=${conversationId}&beforeMessageId=${oldestMessageId}&limit=30`
            );

            if (!older) return;

            if (older.length > 0) {
                const frag = document.createDocumentFragment();

                let prevDay = null;

                older.forEach(m => {
                    const { dayKey, timeText } = toLocalParts(m.createdAt);

                    if (dayKey !== prevDay) {
                        const sep = document.createElement("div");
                        sep.setAttribute("data-day-key", dayKey);
                        sep.textContent = dayLabel(dayKey);
                        sep.style.textAlign = "center";
                        sep.style.color = "#888";
                        sep.style.fontSize = "12px";
                        sep.style.margin = "10px 0";
                        sep.style.userSelect = "none";
                        frag.appendChild(sep);
                        prevDay = dayKey;
                    }

                    const isMe = isMeMessage(m);

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
                        bubble.appendChild(img);
                        img.style.cursor = "zoom-in";
                        img.addEventListener("click", () => openImageViewer(m.attachmentUrl));


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
                    frag.appendChild(row);
                });

                box.insertBefore(frag, box.firstChild);
                oldestMessageId = Number(older[0].messageId);

                const newHeight = box.scrollHeight;
                box.scrollTop = newHeight - prevHeight;
            } else {
                oldestMessageId = null;
            }

        } finally {
            isLoadingOlder = false;
        }
    }
    function ensureAttachmentUi() {
        const inputWrap = document.querySelector("#chatBox .chat-input");
        if (!inputWrap) return;

        if (document.getElementById("chatAttachBtn")) return;

        const attachBtn = document.createElement("button");
        attachBtn.id = "chatAttachBtn";
        attachBtn.type = "button";
        attachBtn.textContent = "📎";
        attachBtn.style.padding = "9px 12px";
        attachBtn.style.border = "none";
        attachBtn.style.borderRadius = "10px";
        attachBtn.style.cursor = "pointer";

        const fileInput = document.createElement("input");
        fileInput.id = "chatAttachInput";
        fileInput.type = "file";
        fileInput.multiple = true;
        fileInput.style.display = "none";

        const preview = document.createElement("div");
        preview.id = "chatAttachPreview";
        preview.style.display = "flex";
        preview.style.flexWrap = "wrap";
        preview.style.gap = "6px";
        preview.style.marginTop = "8px";

        const textInput = document.getElementById("chatText");
        inputWrap.insertBefore(attachBtn, textInput);
        inputWrap.appendChild(fileInput);

        inputWrap.parentElement.insertBefore(preview, inputWrap);


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

        window.__chatWidgetRenderPreview = renderPreview;
    }

    function ensureUnreadDot() {
        const toggle = $("chatToggle");
        if (!toggle) return;
        if ($("chatUnreadDot")) return;

        const style = window.getComputedStyle(toggle);
        if (style.position === "static") toggle.style.position = "relative";

        const dot = document.createElement("span");
        dot.id = "chatUnreadDot";
        dot.style.position = "absolute";
        dot.style.top = "6px";
        dot.style.right = "6px";
        dot.style.width = "10px";
        dot.style.height = "10px";
        dot.style.borderRadius = "999px";
        dot.style.background = "#ff3b30";
        dot.style.display = "none";
        dot.style.boxShadow = "0 0 0 2px white";

        toggle.appendChild(dot);
    }

    function setUnread(on) {
        hasUnread = on;
        const dot = $("chatUnreadDot");
        if (dot) dot.style.display = on ? "block" : "none";
    }

    function openImageViewer(src) {
        if (!src) return;

        const old = document.getElementById("chatImgViewer");
        if (old) old.remove();

        const overlay = document.createElement("div");
        overlay.id = "chatImgViewer";
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
        img.style.cursor = "auto";

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





    function bind() {
        if (isBound) return;
        isBound = true;
        ensureAttachmentUi();
        ensureUnreadDot();

        $("chatToggle").addEventListener("click", openBox);
        $("chatClose").addEventListener("click", closeBox);
        $("chatSend").addEventListener("click", sendText);
        $("chatText").addEventListener("keydown", (e) => {
            if (e.key === "Enter") sendText();
        });
        $("chatMessages").addEventListener("scroll", loadOlderIfNeeded);
    }

    window.ChatWidgetInit = bind;
})();
