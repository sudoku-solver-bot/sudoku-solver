// Vitest config for solver parity tests (ADR-0010).
// Uses single fork + high timeout to handle sequential 50+ puzzle solving.
import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    include: ['tests/SolverParity.test.ts'],
    environment: 'node',
    globals: true,
    testTimeout: 120_000,
    hookTimeout: 120_000,
    pool: 'forks',
    poolOptions: {
      forks: {
        singleFork: true,
      },
    },
  }
})
