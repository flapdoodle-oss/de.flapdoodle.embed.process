package de.flapdoodle.embed.process.builder;

import java.util.HashMap;
import java.util.Map;


public class AbstractEmbeddedBuilder<B> {

	Map<Class<?>, Object> propertyMap = new HashMap<Class<?>, Object>();
	boolean _override = false;

	protected void setOverride(boolean override) {
		_override=override;
	}

	protected <T> T set(Class<T> type, T value) {
		return set(null,type,value);
	}

	protected <T> T set(String label, Class<T> type, T value) {
		T old = (T) propertyMap.put(type, value);
		if ((!_override) && (old!=null)) {
			throw new RuntimeException("" + labelOrTypeAsString(label, type) + " allready set to " + old);
		}
		return old;
	}

	private <T> String labelOrTypeAsString(String label, Class<T> type) {
		return label!=null ? label : ""+type;
	}

	protected <T> T get(Class<T> type) {
		return get((String) null,type);
	}

	protected <T> T get(String label, Class<T> type) {
		T ret = (T) propertyMap.get(type);
		if (ret == null)
			throw new RuntimeException("" + labelOrTypeAsString(label, type) + " not set");
		return ret;
	}

	protected <T> T get(Class<T> type, T defaultValue) {
		T ret = (T) propertyMap.get(type);
		return ret != null
				? ret
				: defaultValue;
	}
}
