package gavin.utilities;

import java.util.Enumeration;
import java.util.regex.Matcher;

public class MatcherEnumeration implements Enumeration<String> {
	private Matcher matcher;
	private int group;

	public MatcherEnumeration(Matcher matcher, int group) {
		this.matcher = matcher;
		this.group = group;
	}

	@Override
	public boolean hasMoreElements() {
		return matcher.find();
	}

	@Override
	public String nextElement() {
		return matcher.group(group);
	}
}