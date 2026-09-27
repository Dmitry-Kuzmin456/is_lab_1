const id = new URLSearchParams(location.search).get('id');

function fact(title, value) {
    return `<span>${title}</span><div>${value}</div>`;
}

function render(dragon) {
    const wing = Math.max(28, Math.min(Number(dragon.wingspan) * 10, 180));
    document.getElementById('content').innerHTML = `
        <div class="visual">
            <div class="creature">
                <span class="wing left" style="width:${wing}px"></span>
                <span class="body"></span>
                <span class="head"></span>
                <span class="wing right" style="width:${wing}px"></span>
            </div>
            <div>Размах ${num(dragon.wingspan)} · возраст ${num(dragon.age)} · вес ${num(dragon.weight)}</div>
            <a class="button" href="edit.html?id=${dragon.id}" target="dragon-edit">Изменить</a>
        </div>
        <h1>${esc(dragon.name)}</h1>
        <div class="facts">
            ${fact('Номер', dragon.id)}
            ${fact('Дата создания', esc(dragon.creationDate || '—'))}
            ${fact('Характер', esc(label(dragon.character)))}
            ${fact('Координаты', num(dragon.coordinatesX) + ', ' + num(dragon.coordinatesY))}
            ${fact('Пещера', 'глубина ' + num(dragon.caveDepth) + ', сокровища ' + num(dragon.caveTreasures))}
            ${fact('Голова', dragon.headSize == null ? '—' : num(dragon.headSize))}
            ${fact('Убийца', esc(dragon.killerName || '—'))}
            ${fact('Глаза', esc(label(dragon.eyeColor)))}
            ${fact('Волосы', esc(label(dragon.hairColor)))}
            ${fact('Страна', esc(label(dragon.nationality)))}
            ${fact('День рождения', esc(when(dragon.birthday)))}
            ${fact('Место', dragon.killerId == null ? '—' : num(dragon.locationX) + ', ' + num(dragon.locationY) + ', ' + num(dragon.locationZ) + (dragon.locationName ? ', ' + esc(dragon.locationName) : ''))}
        </div>
        <div class="actions">
            <button type="button" class="danger" id="delete">Удалить</button>
            <a class="button quiet" href="index.html">К списку</a>
        </div>`;
    document.getElementById('delete').addEventListener('click', remove);
}

async function load() {
    if (!id) {
        showError(new Error('Не указан дракон'));
        return;
    }
    try {
        render(await api('api/dragons/' + id));
        showError(null);
    } catch (error) {
        document.getElementById('content').innerHTML = '';
        showError(error);
    }
}

async function remove() {
    if (!confirm('Удалить дракона и связанные с ним объекты, если они больше никому не нужны?')) {
        return;
    }
    try {
        await api('api/dragons/' + id, { method: 'DELETE' });
        location.href = 'index.html';
    } catch (error) {
        showError(error);
    }
}

window.reloadDragons = load;
load();
connectUpdates(load);
