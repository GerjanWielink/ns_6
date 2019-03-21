package ns.tcphack;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.util.Arrays;

class MyTcpHandler extends TcpHandler {
	private static final String SOURCE =      "20 01 06 7c 25 64 a3 15 a1 b1 6a 1f a4 75 8c ee".replaceAll(" ", "");
	private static final String DESTINATION = "20 01 06 7c 25 64 a1 70 02 04 23 ff fe de 4b 2c".replaceAll(" ", "");
	private static final String ADVERTISED_WINDOW = "FFFF";

	private String destinationPort = "1E20";
	private String sequenceNumber = "00000000";

	boolean done = false;

	public static void main(String[] args) {
		new MyTcpHandler();
	}

	public MyTcpHandler() {
		super();
//		packet.setData("GET /s2249146 HTTP/1.0 \n\n");

			this.sendData(initialPacket().getPacket());    // send the packet


		while (!done) {
			// check for reception of a packet, but wait at most 500 ms:
			Byte[] receivedPacket = this.receiveData(500);

			if (receivedPacket == null || receivedPacket.length == 0) {
				System.out.println("Nothing...");
				continue;
			}

			handlePacketReceived(receivedPacket);
		}   
	}

	public Packet initialPacket(){
		return buildPacket("00000000", true, false, false);
	}

	public Packet buildPacket(String acknowledgement, boolean syn, boolean ack, boolean fin) {
		Packet packet = new Packet(SOURCE, DESTINATION);
		packet.setTCPHeader(destinationPort, sequenceNumber, acknowledgement, Packet.getFlags(syn, ack, fin), ADVERTISED_WINDOW);

		return packet;
	}

	private void handlePacketReceived(Byte[] receivedPacket){
		PacketReader packetReader = new PacketReader(receivedPacket);
		destinationPort = packetReader.getPort();
		sequenceNumber = packetReader.getAcknowledmentNum();

		System.out.println(String.format("Packet received: SYN: %b, FIN: %b, ACK: %b",
			packetReader.getSyn(),
			packetReader.getFin(),
			packetReader.getAck()
		));

//		if(packetReader.getFin() && packetReader.getAck()) {
////			this.done = true;
////		}


		if(packetReader.getFin() && packetReader.getAck()) {
			// Acknowledge received Fin
			Packet packet = buildPacket(
					incrementSequenceNumber(packetReader.getSequenceNum(), 1),
					false, true, false
			);
			this.sendData(packet.getPacket());

			// Final fin
			Packet finPacket = buildPacket(
					"00000000",
					false, false, true
			);
			this.sendData(finPacket.getPacket());

			this.done = true;
			return;
		}

		if (packetReader.getSyn()) {
			Packet packet = buildPacket(
					incrementSequenceNumber(packetReader.getSequenceNum(), 1),
					false, true, false
			);
			this.sendData(packet.getPacket());

			Packet requestPacket = buildPacket(
					incrementSequenceNumber(packetReader.getSequenceNum(), 1),
					false, true, false
			);
			requestPacket.setData("GET /s2249146 HTTP/1.0\r\n" +
					"Host:\r\n" +
					"\r\n");
			this.sendData(requestPacket.getPacket());
			return;
		}

		if(packetReader.getAck() && packetReader.getData().length > 0) {
			Packet packet = buildPacket(
					incrementSequenceNumber(packetReader.getSequenceNum(), 1),
					false, true, false
			);
			this.sendData(packet.getPacket());
		}
	}

	public String incrementSequenceNumber(String sequenceNumber, int increment) {
		long seqNum = Long.valueOf(sequenceNumber, 16) + increment;

		return String.format("%08x", seqNum);
	}
}
