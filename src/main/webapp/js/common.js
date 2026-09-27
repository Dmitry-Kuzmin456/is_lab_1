const LABELS = {
    WISE: 'Мудрый',
    GOOD: 'Добрый',
    CHAOTIC: 'Хаотичный',
    FICKLE: 'Непостоянный',
    GREEN: 'Зелёный',
    BLACK: 'Чёрный',
    BLUE: 'Синий',
    YELLOW: 'Жёлтый',
    ORANGE: 'Оранжевый',
    RUSSIA: 'Россия',
    USA: 'США',
    GERMANY: 'Германия',
    SPAIN: 'Испания',
    INDIA: 'Индия'
};

function esc(value) {
    return String(value ?? '').replace(/[&<>"']/g, (ch) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[ch]));
}

function label(value) {
    if (value == null || value === '') {
        return '—';
    }
    return LABELS[value] || String(value);
}

function num(value) {
    if (value == null || value === '') {
        return '—';
    }
    const number = Number(value);
    if (Number.isNaN(number)) {
        return '—';
    }
    return String(Math.round(number * 1000) / 1000);
}

function when(value) {
    if (!value) {
        return '—';
    }
    return String(value).replace('T', ' ').slice(0, 16);
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    if (response.status === 204) {
        return null;
    }
    const text = await response.text();
    let data = null;
    if (text) {
        try {
            data = JSON.parse(text);
        } catch (error) {
            data = null;
        }
    }
    if (!response.ok) {
        throw new Error((data && data.message) || 'Запрос не выполнен');
    }
    return data;
}

function showError(error) {
    const node = document.getElementById('error');
    if (!node) {
        return;
    }
    node.textContent = error ? error.message || String(error) : '';
}

function wsUrl() {
    const proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
    const base = location.pathname.replace(/[^/]*$/, '');
    return proto + '//' + location.host + base + 'ws/dragons';
}

function connectUpdates(onChange) {
    const open = () => {
        const socket = new WebSocket(wsUrl());
        socket.onmessage = () => onChange();
        socket.onclose = () => setTimeout(open, 2000);
    };
    open();
}

function optionsHtml(items, placeholder) {
    const head = placeholder ? `<option value="${placeholder.value}">${esc(placeholder.text)}</option>` : '';
    const body = (items || []).map((item) => `<option value="${item.id}">${esc(item.label)}</option>`).join('');
    return head + body;
}

function enumOptions(values, emptyText) {
    const head = emptyText ? `<option value="">${esc(emptyText)}</option>` : '';
    return head + values.map((value) => `<option value="${value}">${esc(label(value))}</option>`).join('');
}
