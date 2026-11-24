
import { useState, type FormEvent } from "react";
import { Navigate, useLocation } from "react-router-dom";
import ThemeToggle from "../components/layout/ThemeToggle";
import { usesSpringLogin } from "../api/config";
import { useAuth } from "../auth/useAuth";

function getReturnPath(state: unknown) {
    if (!state || typeof state !== "object" || !("from" in state) || typeof state.from !== "string"
        || !state.from.startsWith("/") || state.from.startsWith("//") || state.from.includes("\\")) {
        return "/dashboard";
    }

    const destination = new URL(state.from, window.location.origin);
    if (destination.origin !== window.location.origin || destination.pathname === "/login") {
        return "/dashboard";
    }
    return destination.pathname + destination.search + destination.hash;
}

export default function Login() {
    const { session, login, expired } = useAuth();
    const location = useLocation();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [rememberMe, setRememberMe] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const notice = error || (expired ? "Your session expired. Sign in again." : "");

    if (session) return <Navigate to={getReturnPath(location.state)} replace />;

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (loading) return;
        setError("");
        setLoading(true);

        try {
            await login({ email, password, rememberMe });
        } catch (reason) {
            setError(reason instanceof Error ? reason.message : "Unable to sign in.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-wrapper">
            <ThemeToggle onLoginPage />
            {/* Background shapes */}
            <div className="login-bg-shape login-bg-shape-1"></div>
            <div className="login-bg-shape login-bg-shape-2"></div>

            {/* Login card */}
            <div className="login-card">

                {/* Brand */}
                <div className="login-brand">
                    <i className="bi bi-asterisk"></i>
                    <span>Orbit Logistics</span>
                </div>

                <p className="login-subtitle">
                    {usesSpringLogin ? "Sign in to the silo workspace" : "Sign in to access your VMI dashboard"}
                </p>

                <form
                    id="loginForm"
                    className="needs-validation"
                    onSubmit={handleSubmit}
                >
                    {/* Email */}
                    <div className="login-form-group">
                        <label htmlFor="email" className="login-form-label">
                            Email Address
                        </label>

                        <div className="login-input-group">
                            <i className="bi bi-envelope input-icon"></i>

                            <input
                                type="email"
                                id="email"
                                className="login-input"
                                placeholder="name@company.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                autoComplete="username"
                                required
                            />
                        </div>
                    </div>

                    {/* Password */}
                    <div className="login-form-group">
                        <label htmlFor="password" className="login-form-label">
                            Password
                        </label>

                        <div className="login-input-group">
                            <i className="bi bi-shield-lock input-icon"></i>

                            <input
                                type={showPassword ? "text" : "password"}
                                id="password"
                                className="login-input login-input-password"
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                autoComplete="current-password"
                                required
                            />

                            <button
                                type="button"
                                className="password-toggle-btn"
                                aria-label={
                                    showPassword ? "Hide password" : "Show password"
                                }
                                onClick={() => setShowPassword((prev) => !prev)}
                            >
                                <i
                                    className={
                                        showPassword
                                            ? "bi bi-eye-slash"
                                            : "bi bi-eye"
                                    }
                                ></i>
                            </button>
                        </div>
                    </div>

                    {/* Remember me */}
                    <div className="login-options">
                        <label className="custom-control-label">
                            <input
                                type="checkbox"
                                className="custom-checkbox-input"
                                checked={rememberMe}
                                onChange={(e) => setRememberMe(e.target.checked)}
                            />
                            <span>Remember Me</span>
                        </label>
                    </div>

                    {/* Error */}
                    {notice && (
                        <div className="alert alert-danger mt-3" role="alert">
                            {notice}
                        </div>
                    )}

                    {/* Submit */}
                    <button
                        type="submit"
                        className="btn-login"
                        id="btn-submit"
                        disabled={loading}
                    >
                        <span>
                            {loading ? "Signing in..." : "Sign In to Dashboard"}
                        </span>

                        <i className="bi bi-arrow-right"></i>
                    </button>
                </form>

                {/* Footer */}
                <p className="login-footer-text">
                    Need access? Contact your system administrator.
                </p>
            </div>
        </div>
    );
}
