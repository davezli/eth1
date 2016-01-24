import java.lang.*;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.util.*;

class Order {
	public String symbol;
	public String dir;
	int price;
	int size;
	int num_filled = 0;
	long count;

	public Order(String s, String d, int p, int si, long c) {
		symbol = s;
		dir = d;
		price = p;
		size = si;
		count = c;
	}
};

public class Bot {
	public static BufferedReader from_exchange;
	public static PrintWriter to_exchange;
	public static Map<String, LinkedList<Integer>> history = new HashMap<String, LinkedList<Integer>>();
	public static Map<String, Order> orders = new HashMap<String, Order>();
	public static int money = 0;
	public static long count = 0;
	public static int id = 1;
	public static double spread = 0.015;
	public static int moneycap = -5000000;
	public static int buycap = 3;

	public static String hello() throws Exception {
		to_exchange.println("HELLO WOLVESOFWALLST");
		String reply = from_exchange.readLine().trim();
		String hellosplited[] = reply.split(" ");
		money = Integer.parseInt(hellosplited[1]);
		System.err.printf("The exchange replied: %s\n", reply);
		return reply;
	}

	public static void add(String symbol, String dir, int price, int size)
			throws Exception {
		if (money - price < moneycap) {
			return;
		}
		String command = "ADD " + id + " " + symbol + " " + dir + " " + price
				+ " " + size;
		to_exchange.println(command);
		System.err.printf("Told exchange to %s\n", command);
		Order buffer = new Order(symbol, dir, price, size, count);
		orders.put(String.valueOf(id), buffer);
		System.err.printf("Added index " + id + " to the orders map\n");
		id++;
	}

	public static void convert(String symbol, String dir, int size)
			throws Exception {
		String command = "CONVERT " + id + " " + symbol + " " + dir + " "
				+ size;
		to_exchange.println(command);
		System.err.printf("Told exchange to %s\n", command);
		id++;
	}

	public static void cancel(int id) throws Exception {
		String command = "CANCEL " + id;
		to_exchange.println(command);
		System.err.printf("Told exchange to %s\n", command);
	}

	public static void printTrend() {
		for (Map.Entry<String, LinkedList<Integer>> entry : history.entrySet()) {
			System.err.printf(entry.getKey() + ":");
			for (Integer i : entry.getValue()) {
				System.err.printf(i + ",");
			}
			System.err.printf(" ");
		}
		System.err.printf("\n");
	}

	public static boolean strictlyOrdered(LinkedList<Integer> ll, String s) {
		if (!s.equals("LESS") && !s.equals("GREATER"))
			return false;
		if (s.equals("LESS")) {
			for (int i = 0; i < ll.size() - 1; i++) {
				if (ll.get(i) >= ll.get(i + 1)) {
					return false;
				}
			}
		}
		if (s.equals("GREATER")) {
			for (int i = 0; i < ll.size() - 1; i++) {
				if (ll.get(i) <= ll.get(i + 1)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
		try {
			Socket skt = new Socket("production", 20000);
			from_exchange = new BufferedReader(new InputStreamReader(
					skt.getInputStream()));
			to_exchange = new PrintWriter(skt.getOutputStream(), true);

			hello();
			Random rand = new Random();
			String line;
			for (int i = 0; i < orders.size(); i++) {
				if ((orders.get(i).count > 500 + count)
						&& (orders.get(i).dir.equals("BUY"))) {
					cancel(i);
				}
			}
			while ((line = from_exchange.readLine()) != null) {
				count++;
				line = line.trim();
				if (line.startsWith("FILL")) {
					System.err.printf("The exchange replied: %s\n", line);
					String[] splited = line.split(" ");
					System.err.printf("Checking orders for index " + splited[1]
							+ "\n");
					if (!orders.containsKey(splited[1])) {
						System.err.printf("??????????????????");
					}
					Order o = orders.get(splited[1]);
					if (splited[3].equals("BUY")) {
						orders.get(splited[1]).num_filled += Integer
								.parseInt(splited[5]);
						money -= o.price;
						add(o.symbol, "SELL", (int) (o.price * (1 + spread)),
								o.num_filled);
					}
					if (splited[3].equals("SELL")) {
						money += (int) (o.price * 1.015);
					}
				}
				if (line.startsWith("ACK") || line.startsWith("REJECT")
						|| line.startsWith("ERROR")) {
					System.err.printf("The exchange replied: %s\n", line);
				}
				if (line.startsWith("TRADE")) {
					String[] splitedTrade = line.split(" ");
					String symbol = splitedTrade[1];
					if (symbol.equals("BOND"))
						continue;
					int price = Integer.parseInt(splitedTrade[2]);
					if (history.containsKey(symbol)) {
						int history_size = 2;
						if (history.get(symbol).size() == history_size) {
							history.get(symbol).removeFirst();
						}
						history.get(symbol).addLast(price);

						if (history.get(symbol).size() == history_size) {
							if (strictlyOrdered(history.get(symbol), "GREATER")) {
								printTrend();
								int buyamount = rand.nextInt(buycap) + 1;
								add(symbol, "BUY", (int) (price * (1 - 0.005)),
										buyamount);
							}
						}
					} else {
						LinkedList<Integer> ll = new LinkedList<Integer>();
						ll.addLast(price);
						history.put(symbol, ll);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
