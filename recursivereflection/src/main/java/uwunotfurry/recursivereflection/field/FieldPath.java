package uwunotfurry.recursivereflection.field;

import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A class representing a series of fields accesses.
 *
 * <p>
 * e.g. {@code SomeClass.someField.someOtherField...}
 * </p>
 *
 * @see FieldCrawler
 */
public abstract class FieldPath<S extends FieldPath<S>> {
	final List<Field> fieldPath;

	FieldPath(Field firstField, Field... furtherFields) {
		checkFieldsChain(firstField.getType(), furtherFields);
		checkFieldsAreNotStatic(furtherFields);

		this.fieldPath = ImmutableList.<Field>builder()
			.add(firstField)
			.add(furtherFields)
			.build()
		;
	}

	FieldPath(S base, Field... fields) {
		checkFieldsChain(base.getType(), fields);
		checkFieldsAreNotStatic(fields);

		this.fieldPath = ImmutableList.<Field>builder()
			.addAll(base.fieldPath)
			.add(fields)
			.build()
		;
	}

	private static void checkFieldsChain(Class<?> firstType, Field... fields) {
		Class<?> parentFieldType = firstType;
		for (Field field : fields) {
			if (!field.getDeclaringClass().isAssignableFrom(parentFieldType)) {
				throw new IllegalArgumentException(field.getName() + " is not present in class " + parentFieldType.getName());
			}
			parentFieldType = field.getType();
		}
	}

	private static void checkFieldsAreNotStatic(Field... fields) {
		for (Field field : fields) {
			int fieldModifiers = field.getModifiers();
			if (Modifier.isStatic(fieldModifiers)) {
				throw new IllegalArgumentException(field.toString() + " is static");
			}
		}
	}

	/**
	 * Create a new {@link FieldPath} whose path is this path plus the provided
	 * fields.
	 *
	 * @param fields the fields to add to the path.
	 *
	 * @return A new field path whose path is this path plus the provided
	 * fields.
	 *
	 * @throws IllegalArgumentException if any of the fields' declared type is
	 * not the class or interface declaring the underlying next field (or a
	 * subclass or implementor thereof), or any of the fields are static.
	 */
	public abstract S resolve(Field... fields);

	/**
	 * Get the object at the end of the field path.
	 * <p>
	 * There is no garantee this method is able to get anything; some field in
	 * the path may be {@code null} valued or may not be accessible.
	 * </p>
	 *
	 * @param <T> the type to cast the result to.
	 *
	 * @param object the object to follow this field path from. Ignored if this
	 * is a {@link StaticFieldPath}, otherwise must be assignable to the type
	 * obtained from {@link #getRequiredType()}.
	 *
	 * @param asType the type to cast the result to.
	 *
	 * @return The object obtained from evaluating the last field in the path.
	 *
	 * @throws IllegalAccessException if any field in the path is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 *
	 * @throws IllegalArgumentException if this is an {@link InstanceFieldPath}
	 * and the specified object is not assignable to the type obtained from
	 * {@link #getRequiredType()}.
	 *
	 * @throws NullPointerException if the evaultion of a field in the path,
	 * aside from the last one, is {@code null}; or the provided object is
	 * {@code null} and this is an {@link InstanceFieldPath}.
	 *
	 * @throws ClassCastException if the value at the end of the field path is
	 * not assignable to the provided type.
	 */
	public final <T> T get(Object object, Class<T> asType) throws IllegalAccessException, NullPointerException {
		Object currentObject = object;
		for (Field field : this.fieldPath) {
			currentObject = field.get(currentObject);
		}

		return asType.cast(currentObject);
	}

	/**
	 * Set the object at the end of the field path.
	 * <p>
	 * There is no garantee this method is able to set anything; some field in
	 * the path may be {@code null} valued or may not be accessible.
	 * </p>
	 *
	 * @param object the object to follow this field path from. Ignored if this
	 * is a {@link StaticFieldPath}, otherwise must be assignable to the type
	 * obtained from {@link #getRequiredType()}.
	 *
	 * @param value the new value to set, must be assignable to the type
	 * obtained from {@link #getType()}.
	 *
	 * @throws IllegalAccessException if any field in the path is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 *
	 * @throws IllegalArgumentException if this is an {@link InstanceFieldPath}
	 * and the specified object is not assignable to the type obtained from
	 * {@link #getRequiredType()}, or the new value is not assignable to the
	 * type obtained from {@link #getType()}.
	 *
	 * @throws NullPointerException if the evaultion of a field in the path,
	 * aside from the last one, is {@code null}; or the provided object is
	 * {@code null} and this is an {@link InstanceFieldPath}.
	 */
	public final void set(Object object, Object value) throws IllegalAccessException, NullPointerException {
		Object currentObject = object;
		Iterator<Field> iterator = this.fieldPath.iterator();
		while (iterator.hasNext()) {
			Field field = iterator.next();
			if (iterator.hasNext()) {
				currentObject = field.get(currentObject);
			} else {
				field.set(currentObject, value);
			}
		}
	}

	/**
	 * Modify the object at the end of the field path.
	 * <p>
	 * There is no garantee this method is able to map anything; some field in
	 * the path may be {@code null} valued or may not be accessible.
	 * </p>
	 *
	 * @param <T> the type obtained from {@link #getType()}.
	 *
	 * @param object the object to follow this field path from. Ignored if this
	 * is a {@link StaticFieldPath}, otherwise must be assignable to the type
	 * obtained from {@link #getRequiredType()}.
	 *
	 * @param mapper a function mapping the field path's old value to a new
	 * one.
	 *
	 * @throws IllegalAccessException if any field in the path is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 *
	 * @throws IllegalArgumentException if this is an {@link InstanceFieldPath}
	 * and the specified object is not assignable to the type obtained from
	 * {@link #getRequiredType()}, or {@code T} is not the type obtained from
	 * {@link #getType()}.
	 *
	 * @throws NullPointerException if the evaultion of a field in the path,
	 * aside from the last one, is {@code null}; or the provided object is
	 * {@code null} and this is an {@link InstanceFieldPath}.
	 */
	public final <T> void map(Object object, UnaryOperator<T> mapper) throws IllegalAccessException, NullPointerException {
		Object currentObject = object;
		Iterator<Field> iterator = this.fieldPath.iterator();
		while (iterator.hasNext()) {
			Field field = iterator.next();
			if (iterator.hasNext()) {
				currentObject = field.get(currentObject);
			} else {
				T oldValue;
				try {
					oldValue = (T)field.get(currentObject);
				} catch (ClassCastException exception) {
					throw new IllegalArgumentException("Mapper function does not accept the correct type: " + field.getType().getName());
				}
				Object newValue;
				try {
					newValue = field.getType().cast(mapper.apply(oldValue));
				} catch (ClassCastException exception) {
					throw new IllegalArgumentException("Mapper function does not provide the correct type: " + field.getType().getName());
				}
				field.set(currentObject, newValue);
			}
		}
	}

	final String getCanonicalPathSuffix() {
		String path = "";
		for (Field field : this.fieldPath) {
			path += "." + field.getName();
		}

		return path;
	}

	/**
	 * Get the type of the field at the end of the path.
	 *
	 * @return The declared type of the underlying last field of the path.
	 */
	public final Class<?> getType() {
		return this.fieldPath.get(this.fieldPath.size() - 1).getType();
	}

	/**
	 * Get the type needed to access this field path.
	 * <p>
	 * This is the type required by the {@link #get(Object,Class)},
	 * {@link #set(Object,Object)} and
	 * {@link #map(Object,UnaryOperator)} methods.
	 * </p>
	 *
	 * @return The declaring class of the underlying first field of the path.
	 */
	public final Class<?> getRequiredType() {
		return this.fieldPath.get(0).getDeclaringClass();
	}

	/**
	 * @return {@code "<?>"} and the name of each field, dot separated.
	 */
	public String toString() {
		return "<?>" + this.getCanonicalPathSuffix();
	}

	/**
	 * @return True if the given object represents a {@link FieldPath} that
	 * represents the same path.
	 */
	public boolean equals(Object other) {
		if (other instanceof FieldPath) {
			return this.fieldPath.equals(((FieldPath)other).fieldPath);
		}

		return false;
	}

	public int hashCode() {
		return this.fieldPath.hashCode() * 31 + this.fieldPath.get(0).hashCode();
	}
}