package ns.tcphack;

abstract class TcpHandler {
	private TcpHackClient client;

	public TcpHandler() {
		client = new TcpHackClient();
	}

	protected void sendData(byte[] data) {
		client.send(data);
	}

	protected Byte[] receiveData(long timeout) {
		return client.dequeuePacket(timeout);
	}
}
