let options = { persons: [], caves: [], characters: [] };
let teams = [];
let refreshResult = () => {};

function dragonCard(dragon) {
    return `<article class="hit">
        <a href="view.html?id=${dragon.id}">${esc(dragon.name)}</a>
        <p>№ ${dragon.id} · ${esc(dragon.creationDate || '—')} · ${esc(label(dragon.character))}</p>
        <p>пещера ${num(dragon.caveDepth)} · ${esc(dragon.killerName || 'без убийцы')}</p>
    </article>`;
}

function showDragons(list, title) {
    document.getElementById('result-title').textContent = title;
    const node = document.getElementById('result');
    if (!list.length) {
        node.innerHTML = '<p class="hint">Ничего не найдено.</p>';
        return;
    }
    node.innerHTML = list.map(dragonCard).join('');
}

function showTeam(team) {
    const members = team.members && team.members.length ? team.members.map(esc).join(', ') : 'без участников';
    document.getElementById('team-result').innerHTML = `<article class="hit">
        <strong>${esc(team.name)}</strong>
        <p>№ ${team.id} · ${esc(team.caveLabel || 'пещера не назначена')}</p>
        <p>${members}</p>
    </article>`;
}

function renderMembers() {
    const box = document.getElementById('members');
    const checked = new Set([...box.querySelectorAll('input:checked')].map((input) => input.value));
    if (!options.persons.length) {
        box.innerHTML = '<p class="hint">Убийц пока нет. Сначала укажите убийцу у дракона.</p>';
        return;
    }
    box.innerHTML = options.persons.map((person) => `<label class="check">
        <input type="checkbox" value="${person.id}" ${checked.has(String(person.id)) ? 'checked' : ''}>
        <span>${esc(person.label)}</span>
    </label>`).join('');
}

function renderTeams() {
    const send = document.getElementById('team-id');
    const previous = send.value;
    send.innerHTML = teams.map((team) => `<option value="${team.id}">${esc(team.name)}</option>`).join('');
    if (previous) send.value = previous;
    document.getElementById('teams').innerHTML = teams.length
        ? teams.map((team) => `<article class="mini"><span>${esc(team.name)}</span><span>${esc(team.caveLabel || 'пещера не назначена')}</span><span>${team.members && team.members.length ? team.members.map(esc).join(', ') : 'без участников'}</span></article>`).join('')
        : '<p>Команд пока нет.</p>';
}

async function loadLists() {
    options = await api('api/dragons/options');
    teams = await api('api/special/teams');
    document.getElementById('character').innerHTML = enumOptions(options.characters);
    renderMembers();
    document.getElementById('cave-id').innerHTML = options.caves.map((cave) => `<option value="${cave.id}">${esc(cave.label)}</option>`).join('');
    renderTeams();
}

document.getElementById('max-date').addEventListener('click', async () => {
    refreshResult = () => document.getElementById('max-date').click();
    try {
        showDragons([await api('api/special/max-date')], 'Самая поздняя дата');
        showError(null);
    } catch (error) {
        document.getElementById('result').innerHTML = '';
        showError(error);
    }
});

document.getElementById('by-name').addEventListener('submit', async (event) => {
    event.preventDefault();
    const part = document.getElementById('part').value.trim();
    refreshResult = async () => {
        showDragons(await api('api/special/by-name?part=' + encodeURIComponent(part)), 'Имя содержит «' + part + '»');
    };
    try {
        await refreshResult();
        showError(null);
    } catch (error) {
        showError(error);
    }
});

document.getElementById('by-character').addEventListener('submit', async (event) => {
    event.preventDefault();
    const character = document.getElementById('character').value;
    refreshResult = async () => {
        showDragons(await api('api/special/by-character?character=' + encodeURIComponent(character)), 'Характер меньше: ' + label(character));
    };
    try {
        await refreshResult();
        showError(null);
    } catch (error) {
        showError(error);
    }
});

document.getElementById('team-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const memberIds = [...document.querySelectorAll('#members input:checked')].map((input) => input.value);
    try {
        const team = await api('api/special/teams', {
            method: 'POST',
            body: JSON.stringify({ name: document.getElementById('team-name').value.trim(), memberIds })
        });
        showTeam(team);
        showError(null);
        await loadLists();
    } catch (error) {
        showError(error);
    }
});

document.getElementById('send-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        const team = await api('api/special/teams/send', {
            method: 'POST',
            body: JSON.stringify({
                teamId: document.getElementById('team-id').value,
                caveId: document.getElementById('cave-id').value
            })
        });
        showTeam(team);
        showError(null);
        await loadLists();
    } catch (error) {
        showError(error);
    }
});

async function boot() {
    try {
        await loadLists();
    } catch (error) {
        showError(error);
    }
}

boot();
connectUpdates(async () => {
    try {
        await loadLists();
        await refreshResult();
    } catch (error) {
        showError(error);
    }
});
