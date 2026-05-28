import { useEffect, useRef } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Sparkles } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { FieldError } from "@/components/ui/field-error";
import { LoadingSpinner } from "@/components/common/LoadingSpinner";
import {
  EXPERIENCE_LEVELS,
  LEVEL_LABELS,
  SPORTS,
  SPORT_LABELS,
} from "@/lib/constants";
import { registerSchema, type RegisterValues } from "../schemas";
import { useRegister } from "../hooks/useRegister";
import { useSystemVars } from "@/features/systemVars/hooks/useSystemVars";
import { useAuth } from "@/providers/AuthProvider";
import { extractError } from "@/lib/api";

export function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const mutation = useRegister();

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: "",
      password: "",
      fullName: "",
      sport: "CRICKET",
      position: "",
      experienceLevel: "BEGINNER",
    },
  });

  const selectedSport = watch("sport");
  const { data: positions = [], isLoading: positionsLoading } = useSystemVars(
    "POSITION",
    selectedSport,
  );

  const initializedRef = useRef(false);
  const lastSportRef = useRef(selectedSport);

  useEffect(() => {
    if (lastSportRef.current !== selectedSport) {
      lastSportRef.current = selectedSport;
      setValue("position", "");
      return;
    }
    const first = positions[0];
    if (!initializedRef.current && first) {
      initializedRef.current = true;
      setValue("position", first.key);
    }
  }, [selectedSport, positions, setValue]);

  const onSubmit = async (values: RegisterValues) => {
    try {
      const result = await mutation.mutateAsync(values);
      login(result.token, result.user);
      toast.success(`Welcome, ${result.user.fullName}!`);
      navigate("/upload", { replace: true });
    } catch (error) {
      toast.error(extractError(error).message);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary-50 to-white px-4 py-8">
      <Card className="w-full max-w-md">
        <CardHeader>
          <div className="mb-2 flex items-center gap-2 text-primary-700">
            <Sparkles className="h-6 w-6" />
            <span className="text-lg font-bold">Ghost Coach</span>
          </div>
          <CardTitle>Create your account</CardTitle>
          <CardDescription>
            Tell us about yourself for personalized coaching.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="fullName">Full name</Label>
              <Input id="fullName" {...register("fullName")} />
              <FieldError>{errors.fullName?.message}</FieldError>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                {...register("email")}
              />
              <FieldError>{errors.email?.message}</FieldError>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                autoComplete="new-password"
                {...register("password")}
              />
              <FieldError>{errors.password?.message}</FieldError>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label htmlFor="sport">Sport</Label>
                <Select id="sport" {...register("sport")}>
                  {SPORTS.map((s) => (
                    <option key={s} value={s}>
                      {SPORT_LABELS[s]}
                    </option>
                  ))}
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="position">Position</Label>
                <Select
                  id="position"
                  disabled={positionsLoading || positions.length === 0}
                  {...register("position")}
                >
                  <option value="">Select position</option>
                  {positions.map((p) => (
                    <option key={p.key} value={p.key}>
                      {p.label}
                    </option>
                  ))}
                </Select>
                <FieldError>{errors.position?.message}</FieldError>
              </div>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="experienceLevel">Experience level</Label>
              <Select id="experienceLevel" {...register("experienceLevel")}>
                {EXPERIENCE_LEVELS.map((l) => (
                  <option key={l} value={l}>
                    {LEVEL_LABELS[l]}
                  </option>
                ))}
              </Select>
            </div>

            <Button type="submit" className="w-full" disabled={isSubmitting}>
              {isSubmitting ? (
                <LoadingSpinner className="text-white" />
              ) : (
                "Create account"
              )}
            </Button>

            <p className="text-center text-sm text-gray-500">
              Already registered?{" "}
              <Link
                to="/login"
                className="font-medium text-primary-600 hover:underline"
              >
                Log in
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
