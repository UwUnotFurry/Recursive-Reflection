package uwunotfurry.recursivereflection.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.UnaryOperator;

/**
 * A class representing a series of fields accesses whose first field is
 * static.
 */
public final class StaticFieldPath extends FieldPath<StaticFieldPath> {

	/**
	 * Create a new instance.
	 *
	 * @param firstField the first field of the path, must be static.
	 *
	 * @param furtherFields the following fields of the path, must not be
	 * static.
	 *
	 * @throws IllegalArgumentException if any of the fields' declared type is
	 * not the class or interface declaring the underlying next field (or a
	 * subclass or implementor thereof), or any of the further fields are
	 * static, or the first field is not static.
	 */
	public StaticFieldPath(Field firstField, Field... furtherFields) {
		super(firstField, furtherFields);

		if (!Modifier.isStatic(firstField.getModifiers())) {
			throw new IllegalArgumentException("first field must be static");
		}
	}

	private StaticFieldPath(StaticFieldPath base, Field... furtherFields) {
		super(base, furtherFields);
	}

	/**
	 * Create a new {@link StaticFieldPath} whose path is this path plus the
	 * provided fields.
	 *
	 * @param fields the fields to add to the path.
	 *
	 * @return A new static field path whose path is this path plus the
	 * provided fields.
	 *
	 * @throws IllegalArgumentException if any of the fields' declared type is
	 * not the class or interface declaring the underlying next field (or a
	 * subclass or implementor thereof), or any of the fields are static.
	 */
	public StaticFieldPath resolve(Field... fields) {
		return new StaticFieldPath(this, fields);
	}

	/**
	 * Get the object at the end of the field path.
	 * <p>
	 * There is no garantee this method is able to get anything; some field in
	 * the path may be {@code null} valued or may not be accessible.
	 * </p><p>
	 * This is a convenience function, equivalent to
	 * {@link FieldPath#get(Object,Class) get(null,asType)}.
	 * </p>
	 *
	 * @param <T> the type to cast the result to.
	 *
	 * @param asType the type to cast the result to.
	 *
	 * @return The object obtained from evaluating the last field in the path.
	 *
	 * @throws IllegalAccessException if any field in the path is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 *
	 * @throws NullPointerException if the evaultion of a field in the path,
	 * aside from the last one, is {@code null}.
	 *
	 * @throws ClassCastException if the value at the end of the field path is
	 * not assignable to the provided type.
	 */
	public <T> T get(Class<T> asType) throws IllegalAccessException, NullPointerException {
		return this.get(null, asType);
	}

	/**
	 * Set the object at the end of the field path.
	 * <p>
	 * There is no garantee this method is able to set anything; some field in
	 * the path may be {@code null} valued or may not be accessible.
	 * </p><p>
	 * This is a convenience function, equivalent to
	 * {@link FieldPath#set(Object,Object) set(null,value)}.
	 * </p>
	 *
	 * @param value the new value to set, must be assignable to the type
	 * obtained from {@link #getType()}.
	 *
	 * @throws IllegalAccessException if any field in the path is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 *
	 * @throws IllegalArgumentException if the new value is not assignable to
	 * the type obtained from {@link #getType()}.
	 *
	 * @throws NullPointerException if the evaultion of a field in the path,
	 * aside from the last one, is {@code null}.
	 */
	public void set(Object value) throws IllegalAccessException, NullPointerException {
		this.set(null, value);
	}

	/**
	 * Modify the object at the end of the field path.
	 * <p>
	 * There is no garantee this method is able to map anything; some field in
	 * the path may be {@code null} valued or may not be accessible.
	 * </p><p>
	 * This is a convenience function, equivalent to
	 * {@link FieldPath#map(Object,UnaryOperator) map(null,mapper)}.
	 * </p>
	 *
	 * @param <T> the type obtained from {@link #getType()}.
	 *
	 * @param mapper a function mapping the field path's old value to a new
	 * one.
	 *
	 * @throws IllegalAccessException if any field in the path is enforcing
	 * Java language access control and the underlying field is inaccessible.
	 *
	 * @throws IllegalArgumentException if {@code T} is not the type obtained
	 * from {@link #getType()}.
	 *
	 * @throws NullPointerException if the evaultion of a field in the path,
	 * aside from the last one, is {@code null}.
	 */
	public <T> void map(UnaryOperator<T> mapper) throws IllegalAccessException, NullPointerException {
		this.map(null, mapper);
	}

	/**
	 * Get a string representing the path.
	 *
	 * @return The canonical path, constructed from the canonical name of
	 * the type obtained from
	 * {@link FieldPath#getRequiredType() getRequiredType()} and the name of
	 * each field, dot separated.
	 */
	public String getCanonicalPath() {
		return this.getRequiredType().getCanonicalName() + this.getCanonicalPathSuffix();
	}

	/**
	 * @return The result of {@link #getCanonicalPath()}.
	 */
	public String toString() {
		return getCanonicalPath();
	}
}