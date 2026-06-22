package osadsakana.utilitiesforprogrammers.client;

/**
 * A millisecond clock that pauses while the display is frozen.
 *
 * <p>Highlight ages and fade-out are measured against this clock, so freezing the
 * display holds every highlight at its current color/opacity and prevents
 * expiry. When unfrozen, time resumes seamlessly (no sudden age jump).
 */
public final class FreezeClock {

    private static long pausedAccumulatedMs = 0L;
    private static long pauseStartMs = 0L; // 0 means "running"

    /** Monotonic milliseconds that do not advance while frozen. */
    public static long now() {
        final long real = System.currentTimeMillis();
        final long pausedSoFar = pausedAccumulatedMs + (pauseStartMs != 0L ? real - pauseStartMs : 0L);
        return real - pausedSoFar;
    }

    public static void setFrozen(boolean frozen) {
        final long real = System.currentTimeMillis();
        if (frozen) {
            if (pauseStartMs == 0L) {
                pauseStartMs = real;
            }
        } else if (pauseStartMs != 0L) {
            pausedAccumulatedMs += real - pauseStartMs;
            pauseStartMs = 0L;
        }
    }

    private FreezeClock() {
    }
}
