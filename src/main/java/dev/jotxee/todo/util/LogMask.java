package dev.jotxee.todo.util;

/**
 * Utility class for masking sensitive data in log output (GDPR compliance).
 */
public final class LogMask {

    private static final int VISIBLE_CHARS = 2;

    private LogMask() {}

    /**
     * Returns the first {@value #VISIBLE_CHARS} characters of the input followed by "***".
     * If the input is shorter than {@value #VISIBLE_CHARS} characters, only "***" is returned.
     *
     * @param value the sensitive string to mask
     * @return masked string, or "unknown" if null
     */
    public static String partial(String value) {
        if (value == null) {
            return "unknown";
        }
        return value.substring(0, Math.min(VISIBLE_CHARS, value.length()));
    }
}
