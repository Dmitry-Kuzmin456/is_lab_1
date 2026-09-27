const size = 5;

function readState() {
    const params = new URLSearchParams(location.search);
    return {
        page: Math.max(0, Number(params.get('page') || 0)),
        field: params.get('field') || '',
        q: params.get('q') || '',
        sort: params.get('sort') || '',
        dir: params.get('dir') || 'asc'
    };
}

function writeState(state) {
    const params = new URLSearchParams();
    if (state.page) params.set('page', String(state.page));
    if (state.field) params.set('field', state.field);
    if (state.q) params.set('q', state.q);
    if (state.sort) params.set('sort', state.sort);
    if (state.dir && state.dir !== 'asc') params.set('dir', state.dir);
    history.replaceState(null, '', params.toString() ? '?' + params.toString() : location.pathname);
}

function formState(page) {
    return {
        page,
        field: document.getElementById('field').value,
        q: document.getElementById('q').value.trim(),
        sort: document.getElementById('sort').value,
        dir: document.getElementById('dir').value
    };
}

function applyState(state) {
    document.getElementById('field').value = state.field;
    document.getElementById('q').value = state.q;
    document.getElementById('sort').value = state.sort;
    document.getElementById('dir').value = state.dir;
}

async function load() {
    const state = readState();
    applyState(state);
    if (state.q && !state.field) {
        showError(new Error('Выберите строковую колонку для фильтра'));
        return;
    }
    const params = new URLSearchParams({
        page: String(state.page),
        size: String(size),
        dir: state.dir || 'asc'
    });
    if (state.field && state.q) {
        params.set('field', state.field);
        params.set('q', state.q);
    }
    if (state.sort) {
        params.set('sort', state.sort);
    }
    try {
        const data = await api('api/dragons?' + params.toString());
        if (data.items.length === 0 && data.total > 0 && state.page > 0) {
            const next = formState(state.page - 1);
            writeState(next);
            await load();
            return;
        }
        render(data);
        showError(null);
    } catch (error) {
        showError(error);
    }
}

function render(data) {
    const table = document.getElementById('table');
    if (!data.items.length) {
        table.innerHTML = '<p>Драконов пока нет.</p>';
        document.getElementById('pager').innerHTML = '';
        return;
    }
    const rows = data.items.map((dragon) => `<tr>
        <td>${dragon.id}</td>
        <td>${esc(dragon.name)}</td>
        <td>${num(dragon.coordinatesX)}</td>
        <td>${num(dragon.coordinatesY)}</td>
        <td>${esc(dragon.creationDate || '—')}</td>
        <td>${num(dragon.caveDepth)}</td>
        <td>${num(dragon.caveTreasures)}</td>
        <td>${esc(dragon.killerName || '—')}</td>
        <td>${num(dragon.age)}</td>
        <td>${num(dragon.wingspan)}</td>
        <td>${num(dragon.weight)}</td>
        <td>${esc(label(dragon.character))}</td>
        <td>${dragon.headSize == null ? '—' : num(dragon.headSize)}</td>
        <td>${esc(label(dragon.eyeColor))}</td>
        <td>${esc(label(dragon.hairColor))}</td>
        <td>${esc(label(dragon.nationality))}</td>
        <td>${esc(when(dragon.birthday))}</td>
        <td>${num(dragon.locationX)}</td>
        <td>${num(dragon.locationY)}</td>
        <td>${num(dragon.locationZ)}</td>
        <td>${esc(dragon.locationName || '—')}</td>
        <td>
            <a href="view.html?id=${dragon.id}">Открыть</a>
            <a href="edit.html?id=${dragon.id}" target="dragon-edit">Изменить</a>
            <button type="button" class="quiet" data-id="${dragon.id}">Удалить</button>
        </td>
    </tr>`).join('');
    table.innerHTML = `<div class="table-wrap"><table>
        <thead><tr>
            <th>Номер</th><th>Имя</th><th>X</th><th>Y</th><th>Дата</th><th>Глубина</th><th>Сокровища</th>
            <th>Убийца</th><th>Возраст</th><th>Размах</th><th>Вес</th><th>Характер</th><th>Голова</th>
            <th>Глаза</th><th>Волосы</th><th>Страна</th><th>День рождения</th>
            <th>Место X</th><th>Место Y</th><th>Место Z</th><th>Название места</th><th></th>
        </tr></thead>
        <tbody>${rows}</tbody>
    </table></div>`;
    table.querySelectorAll('button[data-id]').forEach((button) => {
        button.addEventListener('click', () => remove(button.dataset.id));
    });
    const pages = Math.max(1, Math.ceil(data.total / data.size));
    document.getElementById('pager').innerHTML = `
        <button type="button" class="quiet" id="prev" ${data.page <= 0 ? 'disabled' : ''}>Назад</button>
        <span>${data.page + 1} из ${pages}</span>
        <button type="button" class="quiet" id="next" ${data.page + 1 >= pages ? 'disabled' : ''}>Дальше</button>`;
    document.getElementById('prev').addEventListener('click', () => go(data.page - 1));
    document.getElementById('next').addEventListener('click', () => go(data.page + 1));
}

function go(page) {
    writeState(formState(page));
    load();
}

async function remove(id) {
    if (!confirm('Удалить дракона и связанные с ним объекты, если они больше никому не нужны?')) {
        return;
    }
    try {
        await api('api/dragons/' + id, { method: 'DELETE' });
        await load();
    } catch (error) {
        showError(error);
    }
}

document.getElementById('filters').addEventListener('submit', (event) => {
    event.preventDefault();
    writeState(formState(0));
    load();
});

window.reloadDragons = load;
applyState(readState());
load();
connectUpdates(load);
