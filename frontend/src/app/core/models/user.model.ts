export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string | null;
  role: UserRole;
  emailVerified: boolean;
}

export type UserRole = "ADMIN" | "MEMBER" | "VIEWER";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  organizationName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface RegisterResponse {
  message: string;
}

export interface RefreshRequest {
  refreshToken: string;
}
