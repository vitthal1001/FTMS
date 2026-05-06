import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

export type AuthStatus = "anonymous" | "authenticating" | "authenticated" | "locked";

interface AuthState {
  status: AuthStatus;
  correlationId: string | null;
  roles: string[];
}

const initialState: AuthState = {
  status: "anonymous",
  correlationId: null,
  roles: []
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    authenticationStarted(state, action: PayloadAction<{ correlationId: string }>) {
      state.status = "authenticating";
      state.correlationId = action.payload.correlationId;
    },
    authenticationSucceeded(state, action: PayloadAction<{ roles: string[] }>) {
      state.status = "authenticated";
      state.roles = action.payload.roles;
    },
    sessionLocked(state) {
      state.status = "locked";
      state.roles = [];
    }
  }
});

export const { authenticationStarted, authenticationSucceeded, sessionLocked } = authSlice.actions;
export default authSlice.reducer;

