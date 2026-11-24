import { NavLink, Link } from "react-router-dom";
import type { SidebarProps } from "../../types/layout";

export default function Sidebar({ user }: SidebarProps) {
  const menuSections = [
    {
      title: "Menu",
      items: [
        {
          label: "Dashboard",
          icon: "bi-grid-fill",
          path: "/dashboard",
          id: "menu-overview",
        },
        {
          label: "AI Assistant",
          icon: "bi-robot",
          path: "/chat",
          id: "menu-assistant",
        },
      ],
    },
    {
      title: "Inventory",
      items: [
        {
          label: "Silos & Tanks",
          icon: "bi-database",
          path: "/inventory/silos",
          id: "menu-silos",
        },
        {
          label: "Silo Map",
          icon: "bi-map",
          path: "/inventory/map",
          id: "menu-silo-map",
        },
        {
          label: "Stock Levels",
          icon: "bi-box-seam",
          path: "/inventory/stock",
          id: "menu-stock",
        },
        {
          label: "Consumption",
          icon: "bi-graph-down",
          path: "/inventory/consumption",
          id: "menu-consumption",
        },
        {
          label: "Forecasts",
          icon: "bi-graph-up-arrow",
          path: "/inventory/forecasts",
          id: "menu-forecasts",
        },
      ],
    },
    {
      title: "Logistics",
      items: [
        {
          label: "Shipments",
          icon: "bi-truck",
          path: "/shipments",
          id: "menu-shipments",
        },
        {
          label: "Purchase Orders",
          icon: "bi-cart-check",
          path: "/purchase-orders",
          id: "menu-purchase-orders",
        },
      ],
    },
    {
      title: "Monitoring",
      items: [
        {
          label: "Alerts",
          icon: "bi-bell",
          path: "/alerts",
          id: "menu-alerts",
        },
      ],
    },
    {
      title: "Administration",
      items: [
        {
          label: "User Management",
          icon: "bi-people",
          path: "/users",
          id: "menu-users",
        },
        {
          label: "Settings",
          icon: "bi-gear",
          path: "/settings",
          id: "menu-settings",
        },
      ],
    },
  ];

  return (
    <div className="sidebar-wrapper" id="sidebar">
      {/* Brand Logo / Identity */}
      <Link to="/dashboard" className="sidebar-brand">
        <i className="bi bi-asterisk"></i>
        <span>Orbit Logistics</span>
      </Link>

      {/* Navigation Menu */}
      <div className="flex-grow-1 overflow-y-auto">
        {menuSections.map((section) => (
          <div className="sidebar-menu-section" key={section.title}>
            <div className="sidebar-menu-title">
              {section.title}
            </div>

            <ul className="sidebar-menu-list">
              {section.items.map((item) => (
                <li className="sidebar-menu-item" key={item.id}>
                  <NavLink
                    to={item.path}
                    id={item.id}
                    title={item.label}
                    className={({ isActive }) =>
                      `sidebar-menu-link${isActive ? " active" : ""}`
                    }
                  >
                    <i className={`bi ${item.icon}`}></i>
                    <span>{item.label}</span>
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      {/* Sidebar Profile Card */}
      <div className="sidebar-profile">
        <img
          src={user.avatar || "/assets/images/avatar.png"}
          alt={user.name}
          className="sidebar-profile-img"
        />

        <div className="sidebar-profile-info">
          <div className="sidebar-profile-name">
            {user.name}
          </div>

          {user.email !== user.name && (
              <div className="sidebar-profile-email">{user.email}</div>
          )}
        </div>
      </div>
    </div>
  );
}
