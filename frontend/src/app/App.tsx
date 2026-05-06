import { Activity, AlertTriangle, ShieldCheck, WalletCards } from "lucide-react";
import type { ReactNode } from "react";
import { useSelector } from "react-redux";
import type { RootState } from "./store";
import { runtimeConfig } from "../lib/runtimeConfig";

const serviceReadiness = [
  { name: "Gateway", state: "Planned", signal: "JWT edge policy" },
  { name: "Identity", state: "Planned", signal: "Refresh rotation" },
  { name: "Ledger", state: "Planned", signal: "Outbox guarded" },
  { name: "Fraud", state: "Planned", signal: "Risk stream" }
];

export function App() {
  const authStatus = useSelector((state: RootState) => state.auth.status);

  return (
    <main className="min-h-screen bg-slate-50 text-ink">
      <section className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
          <div>
            <p className="text-sm font-medium uppercase tracking-wide text-bank-blue">NeoBankX</p>
            <h1 className="mt-1 text-2xl font-semibold">Banking operations console</h1>
          </div>
          <div className="rounded-md border border-slate-200 px-3 py-2 text-sm">
            {authStatus}
          </div>
        </div>
      </section>

      <section className="mx-auto grid max-w-7xl gap-6 px-6 py-8 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="grid gap-4 sm:grid-cols-2">
          <StatusTile icon={<ShieldCheck />} label="Security baseline" value="RBAC + JWT" />
          <StatusTile icon={<Activity />} label="Telemetry" value="Metrics + traces" />
          <StatusTile icon={<WalletCards />} label="Money movement" value="Saga planned" />
          <StatusTile icon={<AlertTriangle />} label="Risk posture" value="Audit required" />
        </div>

        <div className="rounded-md border border-slate-200 bg-white p-5 shadow-sm">
          <h2 className="text-lg font-semibold">Runtime</h2>
          <dl className="mt-4 space-y-3 text-sm">
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">Environment</dt>
              <dd className="font-medium">{runtimeConfig.environment}</dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">API base</dt>
              <dd className="max-w-64 truncate font-medium">{runtimeConfig.apiBaseUrl}</dd>
            </div>
          </dl>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-6 pb-10">
        <div className="overflow-hidden rounded-md border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-100 text-slate-600">
              <tr>
                <th className="px-4 py-3 font-medium">Service</th>
                <th className="px-4 py-3 font-medium">State</th>
                <th className="px-4 py-3 font-medium">Control Plane Signal</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {serviceReadiness.map((service) => (
                <tr key={service.name}>
                  <td className="px-4 py-3 font-medium">{service.name}</td>
                  <td className="px-4 py-3">{service.state}</td>
                  <td className="px-4 py-3 text-slate-600">{service.signal}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}

function StatusTile({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="rounded-md border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center gap-3">
        <div className="flex size-10 items-center justify-center rounded-md bg-blue-50 text-bank-blue">
          {icon}
        </div>
        <div>
          <p className="text-sm text-slate-500">{label}</p>
          <p className="text-lg font-semibold">{value}</p>
        </div>
      </div>
    </div>
  );
}
