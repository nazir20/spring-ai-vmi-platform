
import { Link } from "react-router-dom";

export default function Footer() {
  return (
    <footer className="footer-custom">
      <div className="footer-left">
        <span className="footer-logo">
          <i className="bi bi-asterisk" />
          Orbit Logistics
        </span>

        <span className="footer-separator">|</span>

        <span className="footer-copy">
          &copy; {new Date().getFullYear()} Orbit Logistics.
          All rights reserved.
        </span>
      </div>

      <div className="footer-right">
        <ul className="footer-links">
          <li>
            <Link to="/dashboard" className="footer-link">
              Overview
            </Link>
          </li>

          <li>
            <Link to="/chat" className="footer-link">
              AI Assistant
            </Link>
          </li>

          <li>
            <Link to="/help" className="footer-link">
              Help & Documentation
            </Link>
          </li>

          <li>
            <Link to="/status" className="footer-link">
              System Status <span className="status-dot" />
            </Link>
          </li>
        </ul>
      </div>
    </footer>
  );
}
