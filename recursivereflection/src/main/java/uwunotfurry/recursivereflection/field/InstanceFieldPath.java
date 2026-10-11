package uwunotfurry.recursivereflection.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A class representing a series of fields accesses whose first field is not
 * static.
 */
public final class InstanceFieldPath extends FieldPath<InstanceFieldPath> {

	/**
	 * Create a new instance.
	 *
	 * @param firstField the first field of the path, must not be static.
	 *
	 * @param furtherFields the following fields of the path, must not be
	 * static.
	 *
	 * @throws IllegalArgumentException if any of the fields' declared type is
	 * not the class or interface declaring the underlying next field (or a
	 * subclass or implementor thereof), or any of the fields are static.
	 */
	public InstanceFieldPath(Field firstField, Field... furtherFields) {
		super(firstField, furtherFields);

		if (Modifier.isStatic(firstField.getModifiers())) {
			throw new IllegalArgumentException("first field must be non-static");
		}
	}

	private InstanceFieldPath(InstanceFieldPath base, Field... furtherFields) {
		super(base, furtherFields);
	}

	/**
	 * Create a new {@link InstanceFieldPath} whose path is this path plus the
	 * provided fields.
	 *
	 * @param fields the fields to add to the path.
	 *
	 * @return A new instance field path whose  path is this path plus the
	 * provided fields.
	 *
	 * @throws IllegalArgumentException if any of the fields' declared type is
	 * not the class or interface declaring the underlying next field (or a
	 * subclass or implementor thereof), or any of the fields are static.
	 */
	public InstanceFieldPath resolve(Field... fields) {
		return new InstanceFieldPath(this, fields);
	}

	/**
	 * Get a string representing the path.
	 *
	 * @param expression a string representing an object, or expression
	 * provding an object, as if following this path's fields from that object.
	 *
	 * @return The provided expression and the name of each field, dot
	 * separated.
	 */
	public String getCanonicalPath(String expression) {
		return expression + this.getCanonicalPathSuffix();
	}
}