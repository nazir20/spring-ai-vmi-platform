export function initializeSidebar() {
  const sidebarElement = document.querySelector(".sidebar-wrapper");
  const toggleBtn = document.querySelector(".sidebar-toggle-btn");
  const desktopToggleBtn = document.querySelector("#desktop-sidebar-toggle");

  if (!sidebarElement) return;
  const sidebar = sidebarElement;

  // Mobile sidebar
  const overlay = document.createElement("div");
  overlay.className = "sidebar-overlay";
  document.body.appendChild(overlay);

  function toggleMobileSidebar(e: Event) {
    e.stopPropagation();

    sidebar.classList.toggle("show");
    overlay.classList.toggle("show", sidebar.classList.contains("show"));
  }

  function closeMobileSidebar() {
    sidebar.classList.remove("show");
    overlay.classList.remove("show");
  }

  toggleBtn?.addEventListener("click", toggleMobileSidebar);
  overlay.addEventListener("click", closeMobileSidebar);

  // Desktop sidebar
  function toggleDesktopSidebar() {
    document.body.classList.toggle("sidebar-minimized");

    const icon = desktopToggleBtn?.querySelector("i");

    if (icon) {
      icon.className = document.body.classList.contains("sidebar-minimized")
        ? "bi bi-chevron-bar-right"
        : "bi bi-chevron-bar-left";
    }

    setTimeout(() => {
      window.dispatchEvent(new Event("resize"));
    }, 300);
  }

  desktopToggleBtn?.addEventListener("click", toggleDesktopSidebar);

  // Cleanup
  return () => {
    toggleBtn?.removeEventListener("click", toggleMobileSidebar);
    overlay.removeEventListener("click", closeMobileSidebar);
    desktopToggleBtn?.removeEventListener("click", toggleDesktopSidebar);

    overlay.remove();
  };
}
