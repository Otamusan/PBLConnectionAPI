package otamusan.pbl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import otamusan.pbl.DataTypeManagers.ContainerKey;
import otamusan.pbl.Data.IDataSerializer;

public class Connections {
	private DatagramChannel channel;
	private InetSocketAddress addressSend;
	private InetSocketAddress addressReceive;
	private List<Player> players;
	private DataTypeManagers typeManager;
	private Thread thread;

	public Connections(InetSocketAddress send, InetSocketAddress receive) {
		this.addressSend = send;
		this.addressReceive = receive;
		this.typeManager = new DataTypeManagers();
		this.players = new ArrayList<Player>();
	}

	public boolean isExist(Player player) {
		for (Player p : this.players) {
			if (player.equals(p))
				return true;
		}
		return false;
	}

	public Connections(InetSocketAddress address) {
		this(address, address);
	}

	public <T> ContainerKey<T> register(IDataSerializer<T> dataType) {
		return this.typeManager.register(dataType);
	}

	public <T> Boolean isChange(ContainerKey<T> key, Player player) {
		return this.typeManager.isChange(key, player);
	}

	public <T> Optional<T> getData(ContainerKey<T> key, Player player) {
		return this.typeManager.getData(key, player);
	}

	public void addPlayer(Player player) {
		this.players.add(player);
		this.typeManager.addConnection(player);
	}

	public void open() throws IOException {
		this.typeManager.lock();
		this.channel = DatagramChannel.open();
		this.channel.socket().bind(this.addressReceive);
		this.thread = new Thread(new Read(this.channel, this));
		this.thread.start();
	}

	public static class Read implements Runnable {
		private DatagramChannel channel;
		private Connections data;

		public Read(DatagramChannel channel, Connections data) {
			this.channel = channel;
			this.data = data;
		}

		@Override
		public void run() {
			while (true) {
				ByteBuffer bb = ByteBuffer.allocate(this.data.typeManager.CAP);
				SocketAddress address = null;
				try {
					address = this.channel.receive(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
				bb.flip();
				if (address instanceof InetSocketAddress) {
					//this.data.receive(bb,new Player((InetSocketAddress) address));
					this.data.receive(bb, new Player((InetSocketAddress) address));
				}
			}
		}
	}

	public void receive(ByteBuffer raw, Player player) {

	}

	public void onUpdate() {
		this.typeManager.update();
	}

	public <T> void send(T t, ContainerKey<T> key) throws IOException {
		ByteBuffer buffer = this.typeManager.getBuffer(t, key);
		this.channel.send(buffer, this.addressSend);
	}

	public void close() throws IOException {
		this.channel.close();
	}
}