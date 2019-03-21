package ns.tcphack;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.util.Arrays;

public class PacketReader {
    private byte[] bytes;

    public PacketReader(Byte[] bytes) {
        byte[] primitiveBytes = new byte[bytes.length];

        for(int i = 0; i < bytes.length; i++) {
            primitiveBytes[i] = bytes[i];
        }

        this.bytes = primitiveBytes;
    }

    public boolean getSyn() {
        byte flags = bytes[53];

        return ((flags >> 1) & 0x1) == 0x1;
    }

    public boolean getAck() {
        byte flags = bytes[53];

        return ((flags >> 4) & 0x1) == 0x1;
    }

    public boolean getFin() {
        byte flags = bytes[53];

        return (flags & 0x1)  == 0x1;
    }

    public String getPort() {
        return getDataPart(40, 42);
    }

    public String getSequenceNum() {
        return getDataPart(44, 48);
    }

    public byte[] getData() {
        int headerLength = (Integer.valueOf(this.getHeaderLength(), 16) * 4) + 40;

        if (headerLength > (bytes.length - 1)) {
            return new byte[0];
        }

        return Arrays.copyOfRange(bytes, headerLength, bytes.length - 1);
    }

    public String getAcknowledmentNum() {
        return getDataPart(48, 52);
    }

    public String getHeaderLength() {
        byte fullByte = bytes[52];
        byte releventPart = (byte) (fullByte >> 4);

        return String.format("%02x", releventPart);
    }

    private String getDataPart(int from, int to) {
        byte[] dataBytes = Arrays.copyOfRange(bytes, from, to);

        return HexBin.encode(dataBytes);
    }
}
