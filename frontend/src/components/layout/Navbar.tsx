import { Link } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import ThemeToggle from "./ThemeToggle";
import type { NavbarProps } from "../../types/layout";

export default function Navbar({ user }: NavbarProps) {
  const { logout } = useAuth();

  return (
    <header className="navbar-custom">
      <div className="navbar-left">

        {/* Desktop sidebar toggle */}
        <button
          className="btn-desktop-toggle d-none d-xl-flex align-items-center justify-content-center me-3"
          id="desktop-sidebar-toggle"
          aria-label="Minimize Sidebar"
          type="button"
        >
          <i className="bi bi-chevron-bar-left"></i>
        </button>

        {/* Mobile sidebar toggle */}
        <button
          className="sidebar-toggle-btn me-2"
          id="sidebar-toggle"
          aria-label="Toggle Navigation"
          type="button"
        >
          <i className="bi bi-list"></i>
        </button>

        {/* Quick Actions Dropdown */}
        <div className="dropdown ms-2">
          <button
            className="btn-quick-action dropdown-toggle"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            id="quick-actions-dropdown"
          >
            <i className="bi bi-plus-lg"></i>
            <span>Create</span>
          </button>

          <ul
            className="dropdown-menu dropdown-menu-quick-action"
            aria-labelledby="quick-actions-dropdown"
          >
            <li className="dropdown-header">
              Quick Action Shortcuts
            </li>

            <li>
              <Link className="dropdown-item" to="/purchase-orders">
                <i className="bi bi-file-earmark-plus"></i>
                New Purchase Order
              </Link>
            </li>

            <li>
              <Link className="dropdown-item" to="/users">
                <i className="bi bi-person-plus"></i>
                New User
              </Link>
            </li>

            <li>
              <Link className="dropdown-item" to="/inventory/silos">
                <i className="bi bi-box-seam"></i>
                Add Silo
              </Link>
            </li>

            <li>
              <hr className="dropdown-divider" />
            </li>

            <li>
              <Link className="dropdown-item" to="/settings">
                <i className="bi bi-gear"></i>
                System Settings
              </Link>
            </li>
          </ul>
        </div>
      </div>

      {/* Search */}
      <div className="navbar-search-wrapper">
        <input
          type="text"
          className="navbar-search-input"
          placeholder="Search Orbit Logistics..."
          id="main-search"
        />

        <button className="navbar-search-btn" aria-label="Search" type="button">
          <i className="bi bi-search"></i>
        </button>
      </div>

      {/* Right Actions */}
      <div className="navbar-actions">
        <ThemeToggle />

        {/* Fullscreen Toggle */}
        <button
          className="navbar-action-btn me-1"
          aria-label="Toggle Fullscreen"
          id="btn-fullscreen"
          type="button"
        >
          <i className="bi bi-arrows-fullscreen"></i>
        </button>

        {/* Notifications */}
        <div className="dropdown">
          <button
            className="navbar-action-btn dropdown-toggle"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            id="btn-notifications"
            data-bs-auto-close="outside"
          >
            <i className="bi bi-bell"></i>
            <span className="navbar-action-badge"></span>
          </button>

          <div
            className="dropdown-menu dropdown-menu-end dropdown-menu-notification p-0"
            aria-labelledby="btn-notifications"
          >
            <div className="notification-header">
              <h6 className="notification-title">Notifications</h6>
              <button className="btn-clear-all" type="button">
                Mark all read
              </button>
            </div>

            <div className="notification-list">
              <Link to="/alerts" className="notification-item">
                <div className="notification-icon bg-warning text-dark">
                  <i className="bi bi-box-seam-fill"></i>
                </div>

                <div className="notification-content">
                  <p className="notification-text">
                    Low stock warning
                  </p>
                  <span className="notification-time">
                    Inventory monitoring
                  </span>
                </div>

                <span className="notification-unread-dot"></span>
              </Link>

              <Link to="/alerts" className="notification-item">
                <div className="notification-icon bg-primary text-white">
                  <i className="bi bi-truck"></i>
                </div>

                <div className="notification-content">
                  <p className="notification-text">
                    Shipment status update
                  </p>
                  <span className="notification-time">
                    Logistics monitoring
                  </span>
                </div>

                <span className="notification-unread-dot"></span>
              </Link>
            </div>

            <Link to="/alerts" className="notification-footer">
              View All Notifications
            </Link>
          </div>
        </div>

        {/* Profile Dropdown */}
        <div className="dropdown ms-2">
          <button
            className="navbar-profile-btn dropdown-toggle"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            id="profile-dropdown"
          >
            <img
              src={user.avatar || "/assets/images/avatar.png"}
              alt="Profile Image"
              className="navbar-profile-img"
            />

            <span className="navbar-profile-name d-none d-md-inline" title={user.name}>
              {user.name}
            </span>

            <i className="bi bi-chevron-down navbar-profile-caret"></i>
          </button>

          <ul
            className="dropdown-menu dropdown-menu-end dropdown-menu-profile"
            aria-labelledby="profile-dropdown"
          >
            <li className="dropdown-header">
              Welcome, {user.name}!
            </li>

            <li>
              <Link className="dropdown-item" to="/settings">
                <i className="bi bi-person"></i>
                My Account
              </Link>
            </li>

            <li>
              <Link className="dropdown-item" to="/settings">
                <i className="bi bi-gear"></i>
                Settings
              </Link>
            </li>

            <li>
              <hr className="dropdown-divider" />
            </li>

            <li>
              <Link className="dropdown-item text-danger" to="/login" replace onClick={logout}>
                <i className="bi bi-box-arrow-right"></i>
                Logout
              </Link>
            </li>
          </ul>
        </div>
      </div>
    </header>
  );
}
