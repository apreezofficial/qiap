"use client";

import { useId, useState } from "react";

/**
 * Early-access form. NOT connected to any backend yet.
 * To go live: POST `email` to your endpoint inside onSubmit (e.g. a Next route handler,
 * Formspree, Resend audience, or a PHP/MySQL endpoint) and only then show the success state.
 */
export function Waitlist({ dark = true }: { dark?: boolean }) {
  const id = useId(); // unique per instance: the form appears twice on the page
  const [email, setEmail] = useState("");
  const [state, setState] = useState<"idle" | "invalid" | "soon">("idle");

  function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!/^\S+@\S+\.\S+$/.test(email)) {
      setState("invalid");
      return;
    }
    // TODO: send `email` to your backend here.
    setState("soon");
  }

  return (
    <form onSubmit={onSubmit} noValidate className="w-full max-w-[420px]">
      <div className="flex items-center rounded-full border border-line bg-white p-1 pl-4 shadow-soft">
        <label htmlFor={id} className="sr-only">
          Email address
        </label>
        <input
          id={id}
          type="email"
          inputMode="email"
          autoComplete="email"
          placeholder="Email address"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            if (state !== "idle") setState("idle");
          }}
          className="min-w-0 flex-1 bg-transparent text-[14px] outline-none placeholder:text-ink-3"
        />
        <button type="submit" className={`btn btn-sm ${dark ? "btn-primary" : "btn-glass"}`}>
          Notify me
        </button>
      </div>
      <p className="mt-2 h-4 pl-4 text-[12px] text-ink-2" role="status" aria-live="polite">
        {state === "invalid" && "Enter a valid email address."}
        {state === "soon" && "Early access isn't open yet. Check back soon!"}
      </p>
    </form>
  );
}
