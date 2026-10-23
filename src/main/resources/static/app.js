'use strict';
const $ = id => document.getElementById(id);
const state = { view: 'rutinas', ejercicios: [], rutinas: [], loaded: false, pendingDelete: null, busy: false };
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
const levelName = value => ({PRINCIPIANTE:'Principiante',INTERMEDIO:'Intermedio',AVANZADO:'Avanzado'}[value] || value);
async function api(path, options = {}) {
  const response = await fetch('/api/' + path, { ...options, headers: {'Content-Type':'application/json', ...options.headers} });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error([error.mensaje || `Error HTTP ${response.status}`, ...Object.entries(error.errores || {}).map(([key,value]) => `${key}: ${value}`)].join('\n'));
  }
  return response.status === 204 ? null : response.json();
}
let toastTimeout;
function toast(message) { $('toast').textContent = message; $('toast').hidden = false; clearTimeout(toastTimeout); toastTimeout = setTimeout(() => $('toast').hidden = true, 4000); }
async function load() {
  $('connection').textContent = 'Conectando…';
  try {
    const [ejercicios, rutinas] = await Promise.all([api('ejercicios'), api('rutinas')]);
    Object.assign(state, {ejercicios, rutinas, loaded:true});
    $('stat-routines').textContent = rutinas.length;
    $('stat-exercises').textContent = ejercicios.length;
    $('stat-minutes').textContent = rutinas.reduce((sum,r) => sum + r.duracionMinutos, 0);
    $('connection').textContent = '● API conectada'; $('connection').classList.remove('offline');
    render();
  } catch(error) {
    state.loaded = false;
    $('connection').textContent = '● Sin conexión'; $('connection').classList.add('offline');
    $('cards').innerHTML = '<div class="empty"><h3>No pudimos cargar tus datos</h3><p>Comprueba que la aplicación y la base de datos estén disponibles.</p><button class="button secondary" data-action="retry">Reintentar</button></div>';
    toast(error.message);
  }
}
function setView(view) {
  state.view = view; $('search').value = '';
  document.querySelectorAll('.nav-btn').forEach(button => button.classList.toggle('active', button.dataset.view === view));
  $('section-title').textContent = view === 'rutinas' ? 'Mis rutinas' : 'Biblioteca de ejercicios';
  $('create').textContent = view === 'rutinas' ? '+ Nueva rutina' : '+ Nuevo ejercicio';
  if (state.loaded) render();
}
function render() {
  const routines = state.view === 'rutinas';
  const items = state[state.view].filter(item => item.nombre.toLocaleLowerCase('es').includes($('search').value.toLocaleLowerCase('es')));
  $('result-count').textContent = `${items.length} ${routines ? 'rutinas' : 'ejercicios'}`;
  if (!items.length) {
    $('cards').innerHTML = `<div class="empty"><h3>${$('search').value ? 'No hay coincidencias' : routines ? 'Tu próxima rutina está por empezar' : 'Construye tu biblioteca'}</h3><p>${$('search').value ? 'Prueba con otro nombre.' : routines ? 'Crea un plan y añade los ejercicios que más te inspiran.' : 'Añade tu primer ejercicio para usarlo en todas tus rutinas.'}</p></div>`; return;
  }
  $('cards').innerHTML = items.map(item => `<article class="card"><div class="card-top"><span class="card-symbol" aria-hidden="true">${routines ? '▦' : '↗'}</span><span class="badge">${escapeHtml(routines ? levelName(item.nivel) : item.grupoMuscular)}</span></div><h3>${escapeHtml(item.nombre)}</h3><p>${escapeHtml(item.descripcion)}</p><div class="card-meta">${routines ? `<span>◷ ${item.duracionMinutos} min</span><span>↗ ${item.ejercicios.length} ejercicios</span>` : `<span>${escapeHtml(item.equipo)}</span><span>${item.rutinaIds.length} rutinas</span>`}</div>${routines && item.ejercicios.length ? `<details><summary>Ver ejercicios</summary><ul>${item.ejercicios.map(e => `<li>${escapeHtml(e.nombre)}</li>`).join('')}</ul></details>` : ''}<div class="card-actions"><button data-action="edit" data-id="${item.id}">Editar</button><button data-action="delete" data-id="${item.id}">Eliminar</button></div></article>`).join('');
}
function openEditor(id) {
  if (!state.loaded) { toast('Espera a que se carguen los datos o reintenta la conexión.'); return; }
  const routines = state.view === 'rutinas';
  const item = id ? state[state.view].find(record => record.id === id) : null;
  $('editor-form').reset(); $('form-error').textContent = '';
  $('record-id').value = item?.id || '';
  $('editor-title').textContent = `${item ? 'Editar' : routines ? 'Nueva' : 'Nuevo'} ${routines ? 'rutina' : 'ejercicio'}`;
  $('routine-fields').hidden = !routines; $('exercise-fields').hidden = routines;
  $('duracion').required = routines; $('duracion').disabled = !routines;
  $('grupo').required = !routines; $('equipo').required = !routines;
  $('grupo').disabled = routines; $('equipo').disabled = routines;
  $('nombre').value = item?.nombre || ''; $('descripcion').value = item?.descripcion || '';
  if (routines) {
    $('nivel').value = item?.nivel || 'PRINCIPIANTE'; $('duracion').value = item?.duracionMinutos || 45;
    const selected = new Set((item?.ejercicios || []).map(e => e.id));
    $('exercise-options').innerHTML = state.ejercicios.length ? state.ejercicios.map(e => `<label><input type="checkbox" name="ejercicio" value="${e.id}" ${selected.has(e.id) ? 'checked' : ''}> ${escapeHtml(e.nombre)}</label>`).join('') : '<p class="field-help">Todavía no hay ejercicios. Puedes crear la rutina vacía y añadirlos después desde Editar.</p>';
  } else { $('grupo').value = item?.grupoMuscular || ''; $('equipo').value = item?.equipo || ''; }
  $('editor').showModal();
}
document.querySelectorAll('.nav-btn').forEach(button => button.addEventListener('click', () => setView(button.dataset.view)));
$('search').addEventListener('input', () => { if (state.loaded) render(); });
$('create').addEventListener('click', () => openEditor());
$('hero-create').addEventListener('click', () => { setView('rutinas'); openEditor(); });
for (const id of ['close-editor','cancel-editor']) $(id).addEventListener('click', () => { if (!state.busy) $('editor').close(); });
$('editor').addEventListener('cancel', event => { if (state.busy) event.preventDefault(); });
$('editor-form').addEventListener('submit', async event => {
  event.preventDefault(); if (state.busy) return;
  const resource = state.view;
  const payload = {nombre:$('nombre').value.trim(), descripcion:$('descripcion').value.trim()};
  if (resource === 'rutinas') Object.assign(payload, {nivel:$('nivel').value, duracionMinutos:Number($('duracion').value), ejercicioIds:[...document.querySelectorAll('input[name=ejercicio]:checked')].map(el => Number(el.value))});
  else Object.assign(payload, {grupoMuscular:$('grupo').value.trim(), equipo:$('equipo').value.trim()});
  const id = $('record-id').value; state.busy = true; $('save').disabled = true; $('form-error').textContent = '';
  try {
    await api(resource + (id ? '/' + id : ''), {method:id ? 'PUT' : 'POST', body:JSON.stringify(payload)});
    $('editor').close(); toast('Guardado correctamente'); await load();
  } catch(error) { $('form-error').textContent = error.message; }
  finally { state.busy = false; $('save').disabled = false; }
});
$('cards').addEventListener('click', event => {
  const button = event.target.closest('button[data-action]'); if (!button) return;
  if (button.dataset.action === 'retry') { load(); return; }
  const id = Number(button.dataset.id);
  if (button.dataset.action === 'edit') { openEditor(id); return; }
  const item = state[state.view].find(record => record.id === id);
  state.pendingDelete = {resource:state.view, id};
  $('delete-description').textContent = `Se eliminará “${item.nombre}”. ${state.view === 'ejercicios' ? 'También se quitará de sus rutinas; las rutinas se conservan.' : 'Sus ejercicios se conservarán en la biblioteca.'}`;
  $('delete-error').textContent = ''; $('delete-dialog').showModal();
});
$('confirm-delete').addEventListener('click', async () => {
  if (state.busy || !state.pendingDelete) return;
  state.busy = true; $('confirm-delete').disabled = true;
  try { const {resource,id} = state.pendingDelete; await api(`${resource}/${id}`, {method:'DELETE'}); $('delete-dialog').close(); toast('Registro eliminado'); await load(); }
  catch(error) { $('delete-error').textContent = error.message; }
  finally { state.busy = false; $('confirm-delete').disabled = false; }
});
load();
