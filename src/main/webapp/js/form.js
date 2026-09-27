const editing = location.pathname.endsWith('edit.html');
const dragonId = new URLSearchParams(location.search).get('id');
let options = null;

function field(name, title, type, extra) {
    return `<label>${title}<input name="${name}" type="${type || 'text'}" ${extra || ''}></label>`;
}

function renderForm() {
    document.getElementById('form').innerHTML = `
        <div class="grid">
            ${field('name', 'Имя', 'text', 'maxlength="255" required')}
            ${field('age', 'Возраст', 'number', 'min="1" step="1" required')}
            ${field('wingspan', 'Размах крыльев', 'number', 'min="0.001" step="any" required')}
            ${field('weight', 'Вес', 'number', 'min="0.001" step="any" required')}
            <label>Характер
                <select name="character"></select>
            </label>
        </div>
        <div class="block">
            <h2>Координаты</h2>
            <div class="grid">
                <label class="span-2">Существующие
                    <select name="coordinatesId"></select>
                </label>
                ${field('coordinatesX', 'X', 'number', 'step="any" required')}
                ${field('coordinatesY', 'Y', 'number', 'step="1" required')}
            </div>
        </div>
        <div class="block">
            <h2>Пещера</h2>
            <div class="grid">
                <label class="span-2">Существующие
                    <select name="caveId"></select>
                </label>
                ${field('caveDepth', 'Глубина', 'number', 'step="any" required')}
                ${field('caveTreasures', 'Сокровища', 'number', 'min="1" step="1" required')}
            </div>
        </div>
        <div class="block">
            <h2>Голова</h2>
            <div class="grid">
                <label class="span-2">Существующие
                    <select name="headId"></select>
                </label>
                ${field('headSize', 'Размер', 'number', 'step="1"')}
            </div>
        </div>
        <div class="block" id="killer-block">
            <h2>Убийца</h2>
            <div class="grid">
                <label class="span-2">Существующие
                    <select name="killerId"></select>
                </label>
            </div>
            <div id="killer-fields" class="grid">
                ${field('killerName', 'Имя', 'text', 'maxlength="255"')}
                <label>Цвет глаз
                    <select name="eyeColor"></select>
                </label>
                <label>Цвет волос
                    <select name="hairColor"></select>
                </label>
                <label>Страна
                    <select name="nationality"></select>
                </label>
                ${field('birthday', 'День рождения', 'datetime-local')}
            </div>
            <div id="location-fields">
                <h2>Место убийцы</h2>
                <div class="grid">
                    <label class="span-2">Существующие
                        <select name="locationId"></select>
                    </label>
                    ${field('locationX', 'X', 'number', 'step="any"')}
                    ${field('locationY', 'Y', 'number', 'step="any"')}
                    ${field('locationZ', 'Z', 'number', 'step="any"')}
                    ${field('locationName', 'Название', 'text', 'maxlength="321"')}
                </div>
            </div>
        </div>
        <div class="actions">
            <button type="submit">${editing ? 'Сохранить' : 'Создать'}</button>
            <a class="button quiet" href="index.html">К списку</a>
        </div>`;
}

function form() {
    return document.getElementById('form');
}

function fillSelects() {
    const node = form();
    node.character.innerHTML = enumOptions(options.characters, 'Не указан');
    node.eyeColor.innerHTML = enumOptions(options.colors, 'Не указан');
    node.hairColor.innerHTML = enumOptions(options.colors, 'Не указан');
    node.nationality.innerHTML = enumOptions(options.countries, 'Не указана');
    node.coordinatesId.innerHTML = optionsHtml(options.coordinates, { value: 'new', text: 'Новые' });
    node.caveId.innerHTML = optionsHtml(options.caves, { value: 'new', text: 'Новая' });
    node.headId.innerHTML = optionsHtml(options.heads, { value: '', text: 'Нет' })
        + '<option value="new">Новая</option>';
    node.killerId.innerHTML = optionsHtml(options.persons, { value: '', text: 'Нет' })
        + '<option value="new">Новый</option>';
    node.locationId.innerHTML = optionsHtml(options.locations, { value: 'new', text: 'Новое' });
    node.coordinatesId.value = 'new';
    node.caveId.value = 'new';
    node.headId.value = '';
    node.killerId.value = '';
    node.locationId.value = 'new';
}

function setValue(name, value) {
    form().elements[name].value = value == null ? '' : value;
}

function fillDragon(dragon) {
    setValue('name', dragon.name);
    setValue('age', dragon.age);
    setValue('wingspan', dragon.wingspan);
    setValue('weight', dragon.weight);
    setValue('character', dragon.character || '');
    setValue('coordinatesId', dragon.coordinatesId);
    setValue('coordinatesX', dragon.coordinatesX);
    setValue('coordinatesY', dragon.coordinatesY);
    setValue('caveId', dragon.caveId);
    setValue('caveDepth', dragon.caveDepth);
    setValue('caveTreasures', dragon.caveTreasures);
    if (dragon.headId == null) {
        setValue('headId', '');
        setValue('headSize', '');
    } else {
        setValue('headId', dragon.headId);
        setValue('headSize', dragon.headSize);
    }
    if (dragon.killerId == null) {
        setValue('killerId', '');
    } else {
        setValue('killerId', dragon.killerId);
        setValue('killerName', dragon.killerName);
        setValue('eyeColor', dragon.eyeColor || '');
        setValue('hairColor', dragon.hairColor || '');
        setValue('nationality', dragon.nationality || '');
        setValue('birthday', dragon.birthday ? dragon.birthday.slice(0, 16) : '');
        setValue('locationId', dragon.locationId || 'new');
        setValue('locationX', dragon.locationX);
        setValue('locationY', dragon.locationY);
        setValue('locationZ', dragon.locationZ);
        setValue('locationName', dragon.locationName || '');
    }
    syncKiller();
    syncHead();
}

function syncKiller() {
    const active = form().killerId.value !== '';
    document.getElementById('killer-fields').hidden = !active;
    document.getElementById('location-fields').hidden = !active;
}

function syncHead() {
    const active = form().headId.value !== '';
    form().headSize.disabled = !active;
}

function bindSelects() {
    const node = form();
    node.coordinatesId.addEventListener('change', () => {
        const item = options.coordinates.find((it) => String(it.id) === node.coordinatesId.value);
        node.coordinatesX.value = item ? item.x : '';
        node.coordinatesY.value = item ? item.y : '';
    });
    node.caveId.addEventListener('change', () => {
        const item = options.caves.find((it) => String(it.id) === node.caveId.value);
        node.caveDepth.value = item ? item.depth : '';
        node.caveTreasures.value = item ? item.treasures : '';
    });
    node.headId.addEventListener('change', () => {
        const item = options.heads.find((it) => String(it.id) === node.headId.value);
        node.headSize.value = item ? item.size : '';
        syncHead();
    });
    node.killerId.addEventListener('change', () => {
        const item = options.persons.find((it) => String(it.id) === node.killerId.value);
        if (item) {
            node.killerName.value = item.name || '';
            node.eyeColor.value = item.eyeColor || '';
            node.hairColor.value = item.hairColor || '';
            node.nationality.value = item.nationality || '';
            node.birthday.value = item.birthday ? item.birthday.slice(0, 16) : '';
            node.locationId.value = item.locationId || 'new';
            node.locationX.value = item.locationX ?? '';
            node.locationY.value = item.locationY ?? '';
            node.locationZ.value = item.locationZ ?? '';
            node.locationName.value = item.locationName || '';
        } else if (node.killerId.value === '') {
            node.killerName.value = '';
            node.eyeColor.value = '';
            node.hairColor.value = '';
            node.nationality.value = '';
            node.birthday.value = '';
        }
        syncKiller();
    });
    node.locationId.addEventListener('change', () => {
        const item = options.locations.find((it) => String(it.id) === node.locationId.value);
        node.locationX.value = item ? item.x : '';
        node.locationY.value = item ? item.y : '';
        node.locationZ.value = item ? item.z : '';
        node.locationName.value = item && item.name ? item.name : '';
    });
}

function text(value) {
    if (value == null) {
        return null;
    }
    const trimmed = String(value).trim();
    return trimmed === '' ? null : trimmed;
}

function idOrNull(value) {
    if (value == null || value === '' || value === 'new') {
        return null;
    }
    return String(value);
}

function payload() {
    const node = form();
    const killerActive = node.killerId.value !== '';
    const headActive = node.headId.value !== '';
    return {
        name: text(node.name.value),
        age: text(node.age.value),
        wingspan: text(node.wingspan.value),
        weight: text(node.weight.value),
        character: text(node.character.value),
        coordinatesId: idOrNull(node.coordinatesId.value),
        coordinatesX: text(node.coordinatesX.value),
        coordinatesY: text(node.coordinatesY.value),
        caveId: idOrNull(node.caveId.value),
        caveDepth: text(node.caveDepth.value),
        caveTreasures: text(node.caveTreasures.value),
        headId: headActive ? idOrNull(node.headId.value) : null,
        headSize: headActive ? text(node.headSize.value) : null,
        killerId: killerActive ? idOrNull(node.killerId.value) : null,
        killerName: killerActive ? text(node.killerName.value) : null,
        eyeColor: killerActive ? text(node.eyeColor.value) : null,
        hairColor: killerActive ? text(node.hairColor.value) : null,
        nationality: killerActive ? text(node.nationality.value) : null,
        birthday: killerActive ? text(node.birthday.value) : null,
        locationId: killerActive ? idOrNull(node.locationId.value) : null,
        locationX: killerActive ? text(node.locationX.value) : null,
        locationY: killerActive ? text(node.locationY.value) : null,
        locationZ: killerActive ? text(node.locationZ.value) : null,
        locationName: killerActive ? text(node.locationName.value) : null
    };
}

function numberValue(value) {
    if (value == null || String(value).trim() === '') {
        return null;
    }
    const number = Number(String(value).replace(',', '.'));
    return Number.isNaN(number) ? NaN : number;
}

function clientErrors() {
    const node = form();
    const errors = [];
    if (!text(node.name.value)) errors.push('Имя не может быть пустым');
    else if (node.name.value.trim().length > 255) errors.push('Имя не длиннее 255 символов');
    const age = numberValue(node.age.value);
    if (age == null || Number.isNaN(age) || age <= 0 || !Number.isInteger(age)) errors.push('Возраст должен быть больше 0');
    const wingspan = numberValue(node.wingspan.value);
    if (wingspan == null || Number.isNaN(wingspan) || wingspan <= 0) errors.push('Размах крыльев должен быть больше 0');
    const weight = numberValue(node.weight.value);
    if (weight == null || Number.isNaN(weight) || weight <= 0) errors.push('Вес должен быть больше 0');
    if (numberValue(node.coordinatesX.value) == null || Number.isNaN(numberValue(node.coordinatesX.value))) errors.push('Укажите координату X');
    const y = numberValue(node.coordinatesY.value);
    if (y == null || Number.isNaN(y) || !Number.isInteger(y)) errors.push('Координата Y не может быть пустой');
    if (numberValue(node.caveDepth.value) == null || Number.isNaN(numberValue(node.caveDepth.value))) errors.push('Укажите глубину пещеры');
    const treasures = numberValue(node.caveTreasures.value);
    if (treasures == null || Number.isNaN(treasures) || treasures <= 0 || !Number.isInteger(treasures)) errors.push('Число сокровищ должно быть больше 0');
    if (node.headId.value !== '') {
        const size = numberValue(node.headSize.value);
        if (size == null || Number.isNaN(size) || !Number.isInteger(size)) errors.push('Укажите размер головы');
    }
    if (node.killerId.value !== '') {
        if (!text(node.killerName.value)) errors.push('Имя убийцы не может быть пустым');
        if (!text(node.eyeColor.value)) errors.push('Цвет глаз убийцы должен быть GREEN, BLACK, BLUE, YELLOW или ORANGE');
        if (numberValue(node.locationX.value) == null || Number.isNaN(numberValue(node.locationX.value))) errors.push('Укажите координату X места');
        if (numberValue(node.locationY.value) == null || Number.isNaN(numberValue(node.locationY.value))) errors.push('Укажите координату Y места');
        if (numberValue(node.locationZ.value) == null || Number.isNaN(numberValue(node.locationZ.value))) errors.push('Укажите координату Z места');
        if ((node.locationName.value || '').trim().length > 321) errors.push('Название места не длиннее 321 символа');
    }
    return errors;
}

async function start() {
    if (editing && !dragonId) {
        showError(new Error('Не указан дракон'));
        return;
    }
    renderForm();
    try {
        options = await api('api/dragons/options');
        fillSelects();
        bindSelects();
        if (editing) {
            const dragon = await api('api/dragons/' + dragonId);
            document.getElementById('title').textContent = 'Изменение: ' + dragon.name;
            fillDragon(dragon);
        } else {
            syncKiller();
            syncHead();
        }
    } catch (error) {
        showError(error);
    }
    form().addEventListener('submit', async (event) => {
        event.preventDefault();
        const errors = clientErrors();
        if (errors.length) {
            showError(new Error(errors.join('. ')));
            return;
        }
        try {
            const body = JSON.stringify(payload());
            const dragon = editing
                ? await api('api/dragons/' + dragonId, { method: 'PUT', body })
                : await api('api/dragons', { method: 'POST', body });
            if (window.opener) {
                if (window.opener.reloadDragons) window.opener.reloadDragons();
                else window.opener.location.reload();
                window.close();
                return;
            }
            location.href = 'view.html?id=' + dragon.id;
        } catch (error) {
            showError(error);
        }
    });
}

start();
