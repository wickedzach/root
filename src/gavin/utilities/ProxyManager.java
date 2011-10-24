package gavin.utilities;

public abstract class ProxyManager {

	private static ProxyManager NoProxyManager = new ProxyManager() {

	};

	private static class JdbcProxyManager extends ProxyManager {

	}

	private static abstract class FsProxyManager extends ProxyManager {

	}

	public static ProxyManager noProxyManager() {
		return NoProxyManager;
	}

	public enum Strategy {
		Sequential, Random, Statistics
	}

	private Strategy strategy = Strategy.Sequential;

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
}
