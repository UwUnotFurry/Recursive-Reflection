package uwunotfurry.recursivereflection.field;

import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A class providing utils to recursively search field structure.
 */
public final class FieldCrawler {

	private BiPredicate<Field,Object> searchIf;
	private BiPredicate<Field,Object> acceptIf;
	private boolean stopAtAcceptedFields;
	private boolean stopAtCycles;
	private boolean makeAccessible;

	private FieldCrawler(BiPredicate<Field,Object> searchIf, BiPredicate<Field,Object> acceptIf, boolean stopAtAcceptedFields, boolean stopAtCycles, boolean makeAccessible) {
		this.searchIf = searchIf;
		this.acceptIf = acceptIf;
		this.stopAtAcceptedFields = stopAtAcceptedFields;
		this.stopAtCycles = stopAtCycles;
		this.makeAccessible = makeAccessible;
	}

	public static final class Builder {
		private boolean stopAtAcceptedFields = false;
		private boolean stopAtCycles = true;
		private boolean makeAccessible = false;

		private List<FieldCondition> searchFieldConditions = new ArrayList<>();
		private List<FieldCondition> acceptFieldConditions = new ArrayList<>();

		/**
		 * Create a new {@link Builder} with default values.
		 *
		 * <p>The defaults are</p>
		 * <ul>
		 * 	<li>No search {@link FieldCondition FieldCondition}s,</li>
		 * 	<li>No accept {@link FieldCondition FieldCondition}s,</li>
		 * 	<li>{@link #stopAtAcceptedFields(boolean) stopAtAcceptedFields(false)},</li>
		 * 	<li>{@link #stopAtCycles(boolean) stopAtCycles(true)},</li>
		 * 	<li>{@link #makeFieldsAccessible(boolean) makeFieldsAccessible(false)},</li>
		 * </ul>
		 * <p>
		 * though they could change in the future; you are encouraged to
		 * specify each value regardless.
		 * </p>
		 */
		public Builder() {}

		/**
		 * A class encompassing a condition on fields: for use with
		 * {@link FieldCrawler.Builder}s.
		 * <p>
		 * A {@link FieldCondition FieldCondition} encompasses a field's type,
		 * access modifier and whether the underlying field's value is
		 * {@code null}.
		 * </p><p>
		 * The resulting field condition tests if a field matches the speficied
		 * type <b>and</b> matches <b>any</b> of the speficied access modifiers
		 * <b>and</b> the underlying value is<b>n't</b> {@code null} if
		 * {@link #notNullValued()} is speficied.
		 * </p>
		 */
		public static final class FieldCondition {
			private Class<?> typeToLookFor = null;
			private boolean allowSubTypes = false;

			private boolean allowPublic = false;
			private boolean allowPackage = false;
			private boolean allowPrivate = false;
			private boolean allowProtected = false;

			private boolean notNull = false;

			/**
			 * Create a new {@link FieldCondition FieldCondition} with default
			 * values.
			 *
			 * <p>The defaults are</p>
			 * <ul>
			 * 	<li>No type,</li>
			 * 	<li>No access modifiers,</li>
			 * 	<li>{@link #allowNullValued() allowNullValued()},</li>
			 * </ul>
			 * <p>
			 * though they could change in the future; you are encouraged to
			 * specify each value regardless.
			 * </p>
			 */
			public FieldCondition() {}

			/**
			 * Create a {@link FieldCondition FieldCondition} that accepts any
			 * field.
			 *
			 * @return A {@link FieldCondition FieldCondition} that accepts any
			 * field.
			 */
			public static FieldCondition any() {
				return new FieldCondition()
					.anyAccessModifier()
					.anyType()
					.allowNullValued()
				;
			}

			/**
			 * Specifies any access modifier is allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition anyAccessModifier() {
				this.allowPublic    = true;
				this.allowPackage   = true;
				this.allowPrivate   = true;
				this.allowProtected = true;
				return this;
			}

			/**
			 * Specifies public fields are allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition allowPublic() {
				this.allowPublic = true;
				return this;
			}

			/**
			 * Specifies protected fields are allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition allowProtected() {
				this.allowProtected = true;
				return this;
			}

			/**
			 * Specifies private fields are allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition allowPrivate() {
				this.allowPrivate = true;
				return this;
			}

			/**
			 * Specifies package fields are allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition allowPackage() {
				this.allowPackage = true;
				return this;
			}

			/**
			 * Specifies public fields are not allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition exceptPublic() {
				this.allowPublic = false;
				return this;
			}

			/**
			 * Specifies protected fields are not allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition exceptProtected() {
				this.allowProtected = false;
				return this;
			}

			/**
			 * Specifies private fields are not allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition exceptPrivate() {
				this.allowPrivate = false;
				return this;
			}

			/**
			 * Specifies package fields are not allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition exceptPackage() {
				this.allowPackage = false;
				return this;
			}

			/**
			 * Specifies fields with any type are allowed.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition anyType() {
				this.typeToLookFor = Object.class;
				this.allowSubTypes = true;
				return this;
			}

			/**
			 * Specifies only fields with a given type are allowed.
			 *
			 * @param type the type to allow.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition ofType(Class<?> type) {
				this.typeToLookFor = type;
				this.allowSubTypes = false;
				return this;
			}

			/**
			 * Specifies only fields with a given type or subtype thereof are
			 * allowed.
			 *
			 * @param type the type to allow.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition ofSubtypeOf(Class<?> type) {
				this.typeToLookFor = type;
				this.allowSubTypes = true;
				return this;
			}

			/**
			 * Specifies only fields whose value is not {@code null} are
			 * allowed.
			 * <p>
			 * Some fields may be treated as being {@code null} valued where
			 * they're not really. This includes
			 * </p><ul>
			 *	<li>fields whose previous field in the path is {@code null} valued, or treated as such;</li>
			 *	<li>fields who cannot be made accessible through {@link Field#setAccessible(boolean) setAccessible(true)}.</li>
			 * </ul>
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition notNullValued() {
				this.notNull = true;
				return this;
			}

			/**
			 * Specifies fields are allowed regardless of whether they may be
			 * {@code null} valued or not.
			 *
			 * @return This {@link FieldCondition FieldCondition} object.
			 */
			public FieldCondition allowNullValued() {
				this.notNull = false;
				return this;
			}

			private BiPredicate<Field,Object> toBiPredicate() {
				return this.getTypePredicate()
					.and(this.getNullPredicate())
					.and(this.getAccessPredicate())
				;
			}

			private BiPredicate<Field,Object> getTypePredicate() {
				if (this.typeToLookFor == null) {
					throw new IllegalStateException("Must specify a type.");
				}
				if (this.allowSubTypes) {
					return (field,value) -> this.typeToLookFor.isAssignableFrom(field.getType());
				}
				return (field,value) -> field.getType() == this.typeToLookFor;
			}

			private BiPredicate<Field,Object> getNullPredicate() {
				if (this.notNull) {
					return (field,value) -> value != null;
				}
				return (field,value) -> true;
			}

			private BiPredicate<Field,Object> getAccessPredicate() {

				if (
					this.allowPublic
				&&	this.allowProtected
				&&	this.allowPrivate
				&&	this.allowPackage
				) {
					return (field,value) -> true;
				}

				BiPredicate<Field,Object> out = null;
				if (this.allowPublic) {
					out = FieldCondition::isPublic;
				}
				if (this.allowProtected) {
					if (out == null) {
						out = FieldCondition::isProtected;
					} else {
						out = out.or(FieldCondition::isProtected);
					}
				}
				if (this.allowPrivate) {
					if (out == null) {
						out = FieldCondition::isPrivate;
					} else {
						out = out.or(FieldCondition::isPrivate);
					}
				}
				if (this.allowPackage) {
					if (out == null) {
						out = FieldCondition::isPackage;
					} else {
						out = out.or(FieldCondition::isPackage);
					}
				}

				if (out == null) {
					throw new IllegalStateException("Must allow at least one access modifier: public, protected, private or package.");
				}
				return out;
			}

			private static boolean isPublic(Field field, Object value) {
				return Modifier.isPublic(field.getModifiers());
			}

			private static boolean isProtected(Field field, Object value) {
				return Modifier.isProtected(field.getModifiers());
			}

			private static boolean isPrivate(Field field, Object value) {
				return Modifier.isPrivate(field.getModifiers());
			}

			private static boolean isPackage(Field field, Object value) {
				int modifiers = field.getModifiers();
				return !(
					Modifier.isPublic(modifiers)
				||	Modifier.isProtected(modifiers)
				||	Modifier.isPrivate(modifiers)
				);
			}
		}

		/**
		 * Creates a {@link FieldCrawler}.
		 *
		 * @return A {@link FieldCrawler} with the specified properties from
		 * {@code this} {@link FieldCrawler.Builder}.
		 *
		 * @throws IllegalStateException if no search
		 * {@link FieldCondition FieldCondition}s are specified, or no accept
		 * {@link FieldCondition FieldCondition}s are specified, or any of the
		 * {@link FieldCondition FieldCondition}s have no specified type or
		 * no specified access modifiers.
		 */
		public FieldCrawler build() {
			return new FieldCrawler(
				this.searchFieldPredicate(),
				this.acceptFieldPredicate(),
				this.stopAtAcceptedFields,
				this.stopAtCycles,
				this.makeAccessible
			);
		}

		/**
		 * Specify to search further in field structure if a field matches the
		 * provided condition.
		 *
		 * @param condition the condition to check fields against.
		 *
		 * @return This {@link Builder} object.
		 */
		public Builder searchFieldsMatching(FieldCondition condition) {
			searchFieldConditions.add(condition);
			return this;
		}

		/**
		 * Specify to search further in field structure if a field matches any
		 * of the provided conditions.
		 *
		 * @param conditions the conditions to check fields against.
		 *
		 * @return This {@link Builder} object.
		 */
		public Builder searchFieldsMatchingAny(FieldCondition... conditions) {
			for (FieldCondition condition : conditions) {
				searchFieldConditions.add(condition);
			}
			return this;
		}

		/**
		 * Specify to accept a field if it matches the provided condition.
		 * <p>
		 * Accepted fields are returned by the {@link FieldCrawler}'s methods
		 * </p>
		 *
		 * @param condition the condition to check fields against.
		 *
		 * @return This {@link Builder} object.
		 */
		public Builder acceptFieldsMatching(FieldCondition condition) {
			acceptFieldConditions.add(condition);
			return this;
		}

		/**
		 * Specify to accept a field if it matches any of the provided
		 * conditions.
		 * <p>
		 * Accepted fields are returned by the {@link FieldCrawler}'s methods
		 * </p>
		 *
		 * @param conditions the conditions to check fields against.
		 *
		 * @return This {@link Builder} object.
		 */
		public Builder acceptFieldsMatchingAny(FieldCondition... conditions) {
			for (FieldCondition condition : conditions) {
				acceptFieldConditions.add(condition);
			}
			return this;
		}

		/**
		 * Specifies whether to search further when a cycle in field structure
		 * as been detected.
		 *
		 * <p>
		 * A cycle in field structure occurs where following field structure
		 * eventually leads to a field whose declared class is the declaring
		 * class of another field earilier in field structure.<br>
		 * For instance,
		 * </p><blockquote><pre>{@code
		 *class A {
		 *	B b;
		 *}
		 *
		 *class B {
		 *	A a;
		 *}
		 * }</pre></blockquote><p>
		 * <b>Warning:</b> if this is set to {@code false} and any of the
		 * search {@link FieldCondition FieldCondition}s does not check for
		 * {@code null}ness then the resulting {@link FieldCrawler} may run
		 * through a cycle in field structure forever (or until an
		 * {@link OutOfMemoryError} or {@link StackOverflowError} is thrown).
		 * </p>
		 *
		 * @param stop {@code true} if the {@link FieldCrawler} created by this
		 * builder should stop when a cycle in field structure is found,
		 * {@code false} otherwise.
		 *
		 * @return This {@link Builder} object.
		 */
		public Builder stopAtCycles(boolean stop) {
			this.stopAtCycles = stop;
			return this;
		}

		/**
		 * Specifies whether to search further in field structure if a field
		 * has been accpeted.
		 *
		 * @param stop {@code false} if the {@link FieldCrawler} created by
		 * this builder should further search the field structure when a field
		 * has been accepted, {@code true} otherwise.
		 *
		 * @return This {@link Builder} object.
		 */
		public Builder stopAtAcceptedFields(boolean stop) {
			this.stopAtAcceptedFields = stop;
			return this;
		}

		/**
		 * Specifies whether to make fields accessible.
		 *
		 * @param accessible {@code true} if the {@link FieldCrawler} created
		 * by this builder should make fields accessible, {@code false}
		 * otherwise.
		 *
		 * @return This {@link Builder} object.
		 */
		public Builder makeFieldsAccessible(boolean accessible) {
			this.makeAccessible = accessible;
			return this;
		}

		private BiPredicate<Field,Object> searchFieldPredicate() {
			if (this.searchFieldConditions.isEmpty()) {
				throw new IllegalStateException();
			}

			Iterator<FieldCondition> iterator = this.searchFieldConditions.iterator();
			BiPredicate out = iterator.next().toBiPredicate();
			while (iterator.hasNext()) {
				out = out.or(iterator.next().toBiPredicate());
			}

			return out;
		}

		private BiPredicate<Field,Object> acceptFieldPredicate() {
			if (this.acceptFieldConditions.isEmpty()) {
				throw new IllegalStateException();
			}

			Iterator<FieldCondition> iterator = this.acceptFieldConditions.iterator();
			BiPredicate out = iterator.next().toBiPredicate();
			while (iterator.hasNext()) {
				out = out.or(iterator.next().toBiPredicate());
			}

			return out;
		}
	}

	/**
	 * Create a new {@link Builder}.
	 *
	 * @return A new {@link Builder} with default values.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Create a new {@link Builder.FieldCondition FieldCondition}.
	 *
	 * @return A new {@link Builder.FieldCondition FieldCondition} with default
	 * values.
	 */
	public static Builder.FieldCondition fieldCondition() {
		return new Builder.FieldCondition();
	}

	/**
	 * Create a new {@link Builder.FieldCondition FieldCondition} accepting all
	 * fields.
	 *
	 * @return A new {@link Builder.FieldCondition FieldCondition} accepting all
	 * fields.
	 */
	public static Builder.FieldCondition anyField() {
		return Builder.FieldCondition.any();
	}

	/**
	 * Find static and instance field paths.
	 *
	 * @param <T> the class to look through.
	 * @param rootClass the class to look through.
	 * @param object the object to determine the value of
	 * {@link InstanceFieldPath}s, for {@code null} checks. Maybe be
	 * {@code null} if all {@link Builder.FieldCondition FieldConditions}
	 * {@link Builder.FieldCondition#allowNullValued() allowNullValued}.
	 *
	 * @return The {@link FieldPath}s found.
	 */
	public <T> Set<FieldPath> findFieldsRootedAt(Class<T> rootClass, T object) {
		return ImmutableSet.<FieldPath>builder()
			.addAll(findFieldsStaticlyRootedAt(rootClass))
			.addAll(findFieldsNonStaticlyRootedAt(rootClass, object))
			.build()
		;
	}

	/**
	 * Find static field paths.
	 *
	 * @param rootClass the class to look through.
	 *
	 * @return The {@link StaticFieldPath}s found.
	 */
	public Set<StaticFieldPath> findFieldsStaticlyRootedAt(Class<?> rootClass) {
		ImmutableSet.Builder<StaticFieldPath> fields = ImmutableSet.builder();

		for (Field field : findFieldsInClass(rootClass)) {
			int fieldModifiers = field.getModifiers();
			if (Modifier.isStatic(fieldModifiers)) {
				Set<Class<?>> searchedClasses = new HashSet<>();
				searchedClasses.add(rootClass);
				this.dealWithField(field, new StaticFieldPath(field), fields::add, searchedClasses, null);
			}
		}

		return fields.build();
	}

	/**
	 * Find instance field paths.
	 *
	 * @param <T> the class to look through.
	 * @param rootClass the class to look through.
	 * @param object the object to determine the value of
	 * {@link InstanceFieldPath}s, for {@code null} checks. Maybe be
	 * {@code null} if all {@link Builder.FieldCondition FieldConditions}
	 * {@link Builder.FieldCondition#allowNullValued() allowNullValued}.
	 *
	 * @return The {@link InstanceFieldPath}s found.
	 */
	public <T> Set<InstanceFieldPath> findFieldsNonStaticlyRootedAt(Class<T> rootClass, T object) {
		ImmutableSet.Builder<InstanceFieldPath> fields = ImmutableSet.builder();

		for (Field field : findFieldsInClass(rootClass)) {
			int fieldModifiers = field.getModifiers();
			if (!Modifier.isStatic(fieldModifiers)) {
				Set<Class<?>> searchedClasses = new HashSet<>();
				searchedClasses.add(rootClass);
				this.dealWithField(field, new InstanceFieldPath(field), fields::add, searchedClasses, object);
			}
		}

		return fields.build();
	}

	private <T extends FieldPath<T>> void findNested(T path, Consumer<T> acceptField, Set<Class<?>> searchedClasses, Object parentObject) {
		for (Field field : findFieldsInClass(path.getType())) {
			int fieldModifiers = field.getModifiers();
			if (!Modifier.isStatic(fieldModifiers)) {
				this.dealWithField(field, path.resolve(field), acceptField, searchedClasses, parentObject);
			}
		}
	}

	private static Collection<Field> findFieldsInClass(Class<?> _class) {
		ImmutableSet.Builder<Field> fields = ImmutableSet.builder();
		Class<?> currentClass = _class;
		do {
			fields.add(currentClass.getDeclaredFields());
		} while ((currentClass = currentClass.getSuperclass()) != null);
		return fields.build();
	}

	private static Object fieldValue(Field field, Object object) {
		try {
			field.setAccessible(true);
		} catch (Throwable thrown) {
			return null;
		}
		try {
			return field.get(object);
		} catch (IllegalAccessException exception) {
			throw new RuntimeException(exception);
		} catch (NullPointerException exception) {
			return null;
		}
	}

	private <T extends FieldPath<T>> void dealWithField(Field field, T path, Consumer<T> acceptField, Set<Class<?>> searchedClasses, Object parentValue) {
		Object fieldValue = fieldValue(field, parentValue);
		boolean fieldAcceptable = this.acceptIf.test(field, fieldValue);
		boolean searchField =
			!(this.stopAtAcceptedFields && fieldAcceptable)
		&&	this.searchIf.test(field, fieldValue)
		&&	!(this.stopAtCycles && searchedClasses.contains(field.getType()))
		;

		field.setAccessible(this.makeAccessible);

		if (fieldAcceptable) {
			acceptField.accept(path);
		}

		if (searchField) {
			Set<Class<?>> newSearchedClasses = new HashSet<>();
			newSearchedClasses.addAll(searchedClasses);
			newSearchedClasses.add(field.getType());

			this.findNested(path, acceptField, newSearchedClasses, fieldValue);
		}
	}
}