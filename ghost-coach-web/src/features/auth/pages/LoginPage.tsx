import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Sparkles } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { FieldError } from "@/components/ui/field-error";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import { loginSchema, type LoginValues } from "../schemas";
import { useLogin } from "../hooks/useLogin";
import { useAuth } from "@/providers/AuthProvider";
import { extractError } from "@/lib/api";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const mutation = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const from = (location.state as { from?: Location } | null)?.from?.pathname ?? "/upload";

  const onSubmit = async (values: LoginValues) => {
    try {
      const result = await mutation.mutateAsync(values);
      login(result.token, result.user);
      toast.success(`Welcome back, ${result.user.fullName}`);
      navigate(from, { replace: true });
    } catch (error) {
      toast.error(extractError(error).message);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary-50 to-white px-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <div className="mb-2 flex items-center gap-2 text-primary-700">
            <Sparkles className="h-6 w-6" />
            <span className="text-lg font-bold">Ghost Coach</span>
          </div>
          <CardTitle>Welcome back</CardTitle>
          <CardDescription>Log in to analyze your stance.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" autoComplete="email" {...register("email")} />
              <FieldError>{errors.email?.message}</FieldError>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                {...register("password")}
              />
              <FieldError>{errors.password?.message}</FieldError>
            </div>
            <Button type="submit" className="w-full" disabled={isSubmitting}>
              {isSubmitting ? <LoadingSpinner className="text-white" /> : "Log in"}
            </Button>
            <p className="text-center text-sm text-gray-500">
              No account?{" "}
              <Link to="/register" className="font-medium text-primary-600 hover:underline">
                Sign up
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
