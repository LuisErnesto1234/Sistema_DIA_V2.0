document.addEventListener('DOMContentLoaded', function() {
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    const sidebarOverlay = document.querySelector('.sidebar-overlay');
    const sidebarMenu = document.querySelector('.sidebar-menu');
    const main = document.querySelector('.main');

    // Verificar el estado guardado al cargar la página
    const isSidebarShown = localStorage.getItem('sidebarShown') === 'true';

    // Aplicar el estado guardado (ahora oculto por defecto)
    if (isSidebarShown) {
        main.classList.remove('active', 'main-expanded');
        sidebarOverlay.classList.remove('hidden');
        sidebarMenu.classList.remove('-translate-x-full');
    } else {
        main.classList.add('active', 'main-expanded');
        sidebarOverlay.classList.add('hidden');
        sidebarMenu.classList.add('-translate-x-full');
    }

    sidebarToggle.addEventListener('click', function(e) {
        e.preventDefault();
        main.classList.toggle('active');
        main.classList.toggle('main-expanded');
        sidebarOverlay.classList.toggle('hidden');
        sidebarMenu.classList.toggle('-translate-x-full');

        // Guardar el estado en localStorage (ahora guardamos cuando está visible)
        const isNowShown = !sidebarMenu.classList.contains('-translate-x-full');
        localStorage.setItem('sidebarShown', isNowShown);
    });

    sidebarOverlay.addEventListener('click', function(e) {
        e.preventDefault();
        main.classList.add('active');
        main.classList.toggle('main-expanded');
        sidebarOverlay.classList.add('hidden');
        sidebarMenu.classList.add('-translate-x-full');

        // Guardar el estado en localStorage
        localStorage.setItem('sidebarShown', false);
    });

    document.querySelectorAll('.sidebar-dropdown-toggle').forEach(function(item) {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const parent = item.closest('.group');
            if (parent.classList.contains('selected')) {
                parent.classList.remove('selected');
            } else {
                document.querySelectorAll('.sidebar-dropdown-toggle').forEach(function(i) {
                    i.closest('.group').classList.remove('selected');
                });
                parent.classList.add('selected');
            }
        });
    });
});