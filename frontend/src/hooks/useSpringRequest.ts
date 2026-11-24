import { useEffect, useState } from "react";
import { requestSpring } from "../springApi";

export function useSpringRequest<T>(path: string, revision = 0) {
  const [result, setResult] = useState<{ path: string; revision: number; data?: T; error?: string }>();
  useEffect(() => {
    const controller = new AbortController();
    requestSpring<T>(path, { signal: controller.signal })
      .then((data) => {
        if (!controller.signal.aborted) setResult({ path, revision, data });
      })
      .catch((error: Error) => {
        if (!controller.signal.aborted) setResult({ path, revision, error: error.message });
      });
    return () => controller.abort();
  }, [path, revision]);
  return result?.path === path && result.revision === revision ? result : undefined;
}
