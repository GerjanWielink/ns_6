package ns.tcphack;

public class Checksum {

    /**
     * Calculate the Internet Checksum of a buffer (RFC 1071 - http://www.faqs.org/rfcs/rfc1071.html)
     * Algorithm is
     * 1) apply a 16-bit 1's complement sum over all octets (adjacent 8-bit pairs [A,B], final odd length is [A,0])
     * 2) apply 1's complement to this final sum
     * <p>
     * Notes:
     * 1's complement is bitwise NOT of positive value.
     * Ensure that any carry bits are added back to avoid off-by-one errors
     *
     * @param buf The message
     * @return The checksum
     */
    public static long calculateChecksum(byte[] buf) {
        int length = buf.length;
        int i = 0;

        long sum = 0;
        long data;

        // Handle all pairs
        while (length > 1) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
            sum += data;
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i += 2;
            length -= 2;
        }

        // Handle remaining byte in odd length buffers
        if (length > 0) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            sum += (buf[i] << 8 & 0xFF00);
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
        sum = ~sum;
        sum = sum & 0xFFFF;
        return sum;

    }

    static long integralFromBytes(byte[] buffer, int offset, int length) {

        long answer = 0;

        while (--length >= 0) {
            answer = answer << 8;
            answer |= buffer[offset] >= 0 ? buffer[offset] : 0xffffff00 ^ buffer[offset];
            ++offset;
        }

        return answer;
    }

    public static short checksum(byte[] message, int length, int offset) {

// Sum consecutive 16-bit words.

        int sum = 0;

        while (offset < length - 1) {

            sum += (int) integralFromBytes(message, offset, 2);

            offset += 2;
        }

        if (offset == length - 1) {

            sum += (message[offset] >= 0 ? message[offset] : message[offset] ^ 0xffffff00) << 8;
        }

// Add upper 16 bits to lower 16 bits.

        sum = (sum >>> 16) + (sum & 0xffff);

// Add carry

        sum += sum >>> 16;

// Ones complement and truncate.

        return (short) ~sum;
    }

}