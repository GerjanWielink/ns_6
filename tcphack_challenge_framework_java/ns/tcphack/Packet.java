package ns.tcphack;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

public class Packet {
    private String size;
    // IPv6
    private final String version = "6";
    private final String trafficClass = "00";
    private final String flowLabel = "00000";
    private String payloadLength;
    private final String nextHdr = "FD";
    private final String hopLimit = "FF";
    private String source;
    private String destination;

    // TCP
    private final String srcPort = "04D2";
    private String destPort;
    private String seqNum;
    private String acknowledgement;
    private final String hdrLen = "5";
    private String flags;
    private String advWin;
    private final String urgPtr = "0000";

    // Data
    private String data = "";

    private boolean tcpHeaderSet = false;
    private boolean dataSet = false;

    public Packet(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public static void main(String[] args) {
        Packet packet = new Packet("00", "00");
        packet.setTCPHeader("1E1E", "00000000", "00000000", Packet.getFlags(true, false, false), "0000");
        System.out.println(packet.getPayloadLength());
    }

    public void setTCPHeader(String destPrt, String seqNum, String ack, String flags, String advWin) {
        tcpHeaderSet = true;

        this.destPort = destPrt;
        this.seqNum = seqNum;
        this.acknowledgement = ack;
        this.flags = flags;
        this.advWin = advWin;
    }

    public void setData (String data) {
        this.data = data;
    }

    public static String getFlags(boolean syn, boolean ack, boolean fin) {
        byte ackMask = (byte) (ack ? 0x10 : 0x0);
        byte synMask = (byte) (syn ? 0x2 : 0x0);
        byte finMask = (byte) (fin ? 0x1 : 0x0);

        byte flags = (byte) (ackMask | synMask | finMask);

        return "0" + HexBin.encode(new byte[]{flags});
    }

    private String getChecksum () {
       String pseudoHeader = source + destination + "00" + "06" + getPayloadLength() +
               srcPort + destPort + seqNum + acknowledgement + hdrLen + flags + advWin + "0000" + urgPtr
               +  HexBin.encode(data.getBytes());

       while (pseudoHeader.length() % 4 != 0) {
           pseudoHeader += "0";
       }

       int result = 0;

       for (int i = 0; i + 4 < pseudoHeader.length(); i += 4) {
           String word = pseudoHeader.substring(i, i + 4);
           int wordVal = Integer.valueOf(word, 16);

           result += wordVal;

           if ((result & 0xFFFF0000) != 0) {
               result &= 0xFFFF;
               result++;
           }
       }

        result = ~(result & 0xFFFF);

//       return String.format("%04x", (short) result);
        return "0000";
    }


    public byte[] getPacket () {
        return HexBin.decode(this.toString());
    }

    private String getPayloadLength() {
        byte[] dataBytes = this.data.getBytes();
        int len = dataBytes.length + 20;

        return String.format("%04x", len);
    }

    public String toString() {
        String result  =  version + trafficClass + flowLabel + getPayloadLength() + nextHdr + hopLimit + source + destination;
        if (tcpHeaderSet) {
            result += srcPort + destPort + seqNum + acknowledgement + hdrLen + flags + advWin + getChecksum() + urgPtr;
        }

        return result + HexBin.encode(data.getBytes());
    }
}
