import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "@/components/layout/AppShell";
import { ProtectedRoute } from "@/components/layout/ProtectedRoute";
import { PublicRoute } from "@/components/layout/PublicRoute";
import { LoginPage } from "@/features/auth/pages/LoginPage";
import { RegisterPage } from "@/features/auth/pages/RegisterPage";
import { UploadPage } from "@/features/upload/pages/UploadPage";
import { HistoryPage } from "@/features/history/pages/HistoryPage";
import { SessionDetailPage } from "@/features/history/pages/SessionDetailPage";

function NotFound() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <h1 className="text-2xl font-bold text-gray-900">404</h1>
      <p className="mt-1 text-sm text-gray-500">Page not found</p>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route element={<PublicRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route index element={<Navigate to="/upload" replace />} />
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/history" element={<HistoryPage />} />
          <Route path="/history/:id" element={<SessionDetailPage />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
