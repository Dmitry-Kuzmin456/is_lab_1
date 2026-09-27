let options = { persons: [], caves: [], characters: [] };
let teams = [];
let refreshResult = () => {};

function dragonCard(dragon) {
    return `<article class="mini">
        <a href="view.html?id=${dragon.id}">${esc(dragon.name)}</a>
        <span>№ ${dragon.id}</span>
        <span>${esc(dragon.creationDate || '—')}</span>
        <span>${esc(label(dragon.character))}</span>
        <span>пещера ${num(dragon.caveDepth)}</span>
        <span>${esc(dragon.killerName || 'без убийцы')}</span>
    </article>`;
}

function showDragons(list) {
    const node = document.getElementById('result');
    if (!list.length) {
        node.innerHTML = '<p>Ничего не найдено.</p>';
        return;
    }
    node.innerHTML = list.map(dragonCard).join('');
}

function showTeam(team) {
    const members = team.members && team.members.length ? team.members.map(esc).join(', ') : 'без участников';
    document.getElementById('result').innerHTML = `<article class="mini">
        <strong>${esc(team.name)}</strong>
        <span>№ ${team.id}</span>
        <span>${esc(team.caveLabel || 'пещера не назначена')}</span>
        <span>${members}</span>
    </article>`;
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
    document.getElementById('members').innerHTML = options.persons.map((person) => `<option value="${person.id}">${esc(person.label)}</option>`).join('');
    document.getElementById('cave-id').innerHTML = options.caves.map((cave) => `<option value="${cave.id}">${esc(cave.label)}</option>`).join('');
    renderTeams();
}

document.getElementById('max-date').addEventListener('click', async () => {
    refreshResult = () => document.getElementById('max-date').click();
    try {
        showDragons([await api('api/special/max-date')]);
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
        showDragons(await api('api/special/by-name?part=' + encodeURIComponent(part)));
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
        showDragons(await api('api/special/by-character?character=' + encodeURIComponent(character)));
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
    const memberIds = [...document.getElementById('members').selectedOptions].map((option) => option.value);
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
