function startRename(btn) {
    const td = btn.closest('td');
    td.querySelector('.rename-display').classList.add('d-none');
    const form = td.querySelector('.rename-form');
    form.classList.remove('d-none');
    form.classList.add('d-flex');
    form.querySelector('input').focus();
}

function cancelRename(btn) {
    const td = btn.closest('td');
    const form = td.querySelector('.rename-form');
    form.classList.add('d-none');
    form.classList.remove('d-flex');
    td.querySelector('.rename-display').classList.remove('d-none');
}