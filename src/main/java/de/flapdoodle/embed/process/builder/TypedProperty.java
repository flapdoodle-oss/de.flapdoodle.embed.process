package de.flapdoodle.embed.process.builder;

public final class TypedProperty<T> {

	private final String _name;
	private final Class<T> _type;

	public TypedProperty(String name, Class<T> type) {
		if (name==null) throw new IllegalArgumentException("name is null");
		if (type==null) throw new IllegalArgumentException("type is null");
		_name = name;
		_type = type;
	}

	public String name() {
		return _name;
	}

	public Class<T> type() {
		return _type;
	}
	
	@Override
	public String toString() {
		return _name+"("+_type+")";
	}

	public static <T> TypedProperty<T> with(String name, Class<T> type) {
		return new TypedProperty<T>(name, type);
	}

	public static <T> TypedProperty<T> with(Class<T> type) {
		return new TypedProperty<T>(type.getSimpleName(), type);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_name == null)
				? 0
				: _name.hashCode());
		result = prime * result + ((_type == null)
				? 0
				: _type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedProperty other = (TypedProperty) obj;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		if (_type == null) {
			if (other._type != null)
				return false;
		} else if (!_type.equals(other._type))
			return false;
		return true;
	}

}
