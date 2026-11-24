
import { useEffect } from "react";
import { Outlet } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";

import Sidebar from "./Sidebar";
import Navbar from "./Navbar";
import Footer from "./Footer";

import { initializeSidebar } from "../../scripts/sidebar";

export default function AppLayout() {
    const { session } = useAuth();

    useEffect(() => {
        const cleanup = initializeSidebar();

        return cleanup;
    }, []);

    const user = {
        name: session?.user.name ?? session?.user.email ?? "User",
        email: session?.user.email ?? "",
        avatar: "/assets/images/avatar.png",
    };

    return (
        <div className="app-shell">
            <Sidebar user={user} />

            <div className="main-wrapper">
                <Navbar user={user} />

                <main className="main-content">
                    <Outlet />
                </main>

                <Footer />
            </div>
        </div>
    );
}
