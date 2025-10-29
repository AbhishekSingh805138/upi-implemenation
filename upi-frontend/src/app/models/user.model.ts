export interface User {
  id?: number;
  username: string;
  email: string;
  phone: string;
  fullName: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserRegistrationRequest {
  username: string;
  email: string;
  phone: string;
  fullName: string;
}

export interface UserLoginRequest {
  identifier: string;
}

export interface UserUpdateRequest {
  email?: string;
  phone?: string;
  fullName?: string;
}