package de.flapdoodle.embed.process.prototype;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SystemBuilder {
	
	private final String name;
	private final Map<NamedType<?>, SupplierFactory<?>> transitionMap = new LinkedHashMap<>();

	private SystemBuilder(String name) {
		this.name = name;
	}
	
	public <D> TransitionBuilder<D> transitionInto(Class<D> stateType) {
		return new TransitionBuilder<D>(this, NamedType.of(stateType));
	}
	
	public <D> TransitionBuilder<D> transitionInto(String name, Class<D> stateType) {
		return new TransitionBuilder<D>(this, NamedType.of(name, stateType));
	}

	private <D> SystemBuilder put(NamedType<D> stateType,SupplierFactory<D> context) {
		SupplierFactory<?> old = transitionMap.put(stateType, context);
		if (old!=null) {
			throw new IllegalArgumentException("transition for "+stateType+" already set");
		}
		return this;
	}
	
	public static SystemBuilder builderOf(String name) {
		return new SystemBuilder(name);
	}
	
	public System build() {
		return new MapBasedSystem(transitionMap);
	}
	
	public static class TransitionBuilder<D> {

		private final SystemBuilder systemBuilder;
		private final NamedType<D> stateType;

		public TransitionBuilder(SystemBuilder systemBuilder, NamedType<D> stateType) {
			this.systemBuilder = systemBuilder;
			this.stateType = stateType;
		}
		
		public SystemBuilder with(Transitions.StartingTransition<D> transition) {
			return systemBuilder.put(stateType, system -> () -> transition.transitInto());
		}
		
		public <S> SystemBuilder with(Class<S> type, Transitions.DependingTransition<S,D> transition) {
			return with(NamedType.of(type), transition);
		}
		
		public <S> SystemBuilder with(String name, Class<S> type, Transitions.DependingTransition<S,D> transition) {
			return with(NamedType.of(name, type), transition);
		}
		
		private <S> SystemBuilder with(NamedType<S> namedType, Transitions.DependingTransition<S,D> transition) {
			return systemBuilder.put(stateType, system -> {
				return () -> transition.transitFrom(system.transitionInto(namedType).get());
			});
		}
	}

	@FunctionalInterface
	interface SupplierFactory<D> {
		Supplier<State<D>> supplierOf(System system);
	}

	private static class MapBasedSystem implements System {

		private final LinkedHashMap<NamedType<?>, SupplierFactory<?>> transitionMap;

		public MapBasedSystem(Map<NamedType<?>, SupplierFactory<?>> transitionMap) {
			this.transitionMap = new LinkedHashMap<>(transitionMap);
		}
		
		@Override
		public <T> Supplier<State<T>> transitionInto(NamedType<T> type) {
			SupplierFactory<T> supplierFactory = (SupplierFactory<T>) transitionMap.get(type);
			if (supplierFactory==null) {
				throw new IllegalArgumentException("could not resolve transition for: "+type);
			}
			
			return supplierFactory.supplierOf(this);
		}
		
	}
	
}
