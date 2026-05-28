import { Outlet, NavLink, useNavigate } from "react-router-dom";
import { Camera, History, LogOut, User as UserIcon, Sparkles } from "lucide-react";
import { useAuth } from "@/providers/AuthProvider";
import { SPORT_LABELS } from "@/lib/constants";
import { cn } from "@/lib/utils";

const navItems = [
  { to: "/upload", label: "Analyze", icon: Camera },
  { to: "/history", label: "History", icon: History },
];

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="sticky top-0 z-10 border-b border-gray-200 bg-white">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4">
          <button
            onClick={() => navigate("/upload")}
            className="flex items-center gap-2 text-lg font-bold text-primary-700"
          >
            <Sparkles className="h-5 w-5" />
            Ghost Coach
          </button>

          <nav className="hidden gap-1 md:flex">
            {navItems.map(({ to, label, icon: Icon }) => (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-primary-50 text-primary-700"
                      : "text-gray-600 hover:bg-gray-100 hover:text-gray-900",
                  )
                }
              >
                <Icon className="h-4 w-4" />
                {label}
              </NavLink>
            ))}
          </nav>

          <div className="flex items-center gap-3">
            {user ? (
              <div className="hidden text-right md:block">
                <p className="text-sm font-medium text-gray-900">{user.fullName}</p>
                <p className="text-xs text-gray-500">{SPORT_LABELS[user.sport]}</p>
              </div>
            ) : null}
            <button
              onClick={logout}
              className="flex h-9 w-9 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100 hover:text-gray-900"
              aria-label="Logout"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>

        <nav className="flex border-t border-gray-200 bg-white md:hidden">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                cn(
                  "flex flex-1 items-center justify-center gap-1.5 py-2 text-sm font-medium",
                  isActive ? "border-b-2 border-primary-600 text-primary-700" : "text-gray-500",
                )
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}

// re-export for icon usage in pages
export { UserIcon };
