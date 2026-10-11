package uwunotfurry.recursivereflection.field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public final class InstanceFieldPathTest {

	private static class A {
		static B bStatic = new B();
		B b = new B();
	}

	private static class B {
		static C cStatic = new C();
		C c = new C();
	}

	private static class C {
		private C cPrivate;
		C c;
	}

	@Test
	void newWithSingleInstanceField() {
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b")
			)
		);
	}

	@Test
	void newWithMultipleInstanceFields() {
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void newWithRepeatedFields() {
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void newWithPrivateField() {
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("cPrivate")
			)
		);
	}

	@Test
	void newWithStaticFirstField() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("bStatic")
			)
		);
	}

	@Test
	void newWithStaticFurtherFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b"),
				B.class.getDeclaredField("cStatic")
			)
		);
	}

	@Test
	void newWithNonChainingFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b"),
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void resolveNonStaticChainingFields() {
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b")
			).resolve(
				B.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void resolveNonStaticNonChainingFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b")
			).resolve(
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void resolveStaticChainingFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b")
			).resolve(
				B.class.getDeclaredField("cStatic")
			)
		);
	}

	@Test
	void getType() {
		Assertions.assertEquals(
			B.class,
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b")
				)
			).getType()
		);
	}

	@Test
	void getRequiredType() {
		Assertions.assertEquals(
			A.class,
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b")
				)
			).getRequiredType()
		);
	}

	@Test
	void equalsSamePath() {
		Assertions.assertTrue(
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b")
				)
			).equals(
				Assertions.assertDoesNotThrow(
					() -> new InstanceFieldPath(
						A.class.getDeclaredField("b")
					)
				)
			)
		);
	}

	@Test
	void equalsDifferentPath() {
		Assertions.assertFalse(
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b")
				)
			).equals(
				Assertions.assertDoesNotThrow(
					() -> new InstanceFieldPath(
						A.class.getDeclaredField("b"),
						B.class.getDeclaredField("c")
					)
				)
			)
		);
	}

	@Test
	void equalsDifferentType() {
		Assertions.assertFalse(
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b")
				)
			).equals(
				new Object()
			)
		);
	}

	@Test
	void getCanonicalPath() {
		Assertions.assertEquals(
			"expression.b.c.c",
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b"),
					B.class.getDeclaredField("c"),
					C.class.getDeclaredField("c")
				)
			).getCanonicalPath("expression")
		);
	}

	@Test
	void getOnSingleFieldPath() {
		A aObject = new A();
		Assertions.assertSame(
			aObject.b,
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b")
				).get(aObject, Object.class)
			)
		);
	}

	@Test
	void getOnMultiFieldPath() {
		A aObject = new A();
		aObject.b.c.c = new C();
		Assertions.assertSame(
			aObject.b.c.c,
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b"),
					B.class.getDeclaredField("c"),
					C.class.getDeclaredField("c")
				).get(aObject, Object.class)
			)
		);
	}

	@Test
	void getAsType() {
		A aObject = new A();
		Assertions.assertSame(
			aObject.b,
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b")
				).get(aObject, B.class)
			)
		);
	}

	@Test
	void getNullValuedField() {
		A aObject = new A();
		Assertions.assertNull(
			Assertions.assertDoesNotThrow(
				() -> new InstanceFieldPath(
					A.class.getDeclaredField("b"),
					B.class.getDeclaredField("c"),
					C.class.getDeclaredField("c")
				).get(aObject, Object.class)
			)
		);
	}

	@Test
	void getThroughNullValuedField() {
		A aObject = new A();
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			).get(aObject, Object.class)
		);
	}

	@Test
	void getAsWrongType() {
		A aObject = new A();
		Assertions.assertThrows(
			ClassCastException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b")
			).get(aObject, C.class)
		);
	}

	@Test
	void getPrivateField() {
		A aObject = new A();
		Assertions.assertThrows(
			IllegalAccessException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("cPrivate")
			).get(aObject, Object.class)
		);
	}

	@Test
	void getWithNull() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b")
			).get(null, Object.class)
		);
	}

	@Test
	void getWithObjectOfIncorrectType() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				A.class.getDeclaredField("b")
			).get(new Object(), Object.class)
		);
	}

	private static class D {
		C c = new C();
		C cNull;
		private C cPrivate;
	}

	@Test
	void setOnSingleFieldPath() {
		D dObject = new D();
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).set(dObject, cObject)
		);
		Assertions.assertSame(
			cObject,
			dObject.c
		);
	}

	@Test
	void setOnMultiFieldPath() {
		D dObject = new D();
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			).set(dObject, cObject)
		);
		Assertions.assertSame(
			cObject,
			dObject.c.c
		);
	}

	@Test
	void setNull() {
		D dObject = new D();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).set(dObject, null)
		);
		Assertions.assertNull(
			dObject.c
		);
	}

	@Test
	void setThroughNullValuedField() {
		D dObject = new D();
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			).set(dObject, new C())
		);
	}

	@Test
	void setWrongType() {
		D dObject = new D();
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).set(dObject, new Object())
		);
	}

	@Test
	void setPrivateField() {
		D dObject = new D();
		Assertions.assertThrows(
			IllegalAccessException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("cPrivate")
			).set(dObject, new C())
		);
	}

	@Test
	void setWithNull() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).set(null, new C())
		);
	}

	@Test
	void setWithObjectOfIncorrectType() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).set(new Object(), new C())
		);
	}

	@Test
	void mapProvidedSameObject() {
		D dObject = new D();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).map(dObject, cObject -> {
				Assertions.assertSame(
					dObject.c,
					cObject
				);
				return cObject;
			})
		);
	}

	@Test
	void mapOnSingleFieldPath() {
		D dObject = new D();
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).map(dObject, __ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			dObject.c
		);
	}

	@Test
	void mapOnMultiFieldPath() {
		D dObject = new D();
		dObject.c.c = new C();
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			).map(dObject, __ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			dObject.c.c
		);
	}

	@Test
	void mapOnNullValuedField() {
		D dObject = new D();
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			).map(dObject, __ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			dObject.c.c
		);
	}

	@Test
	void mapToNull() {
		D dObject = new D();
		Assertions.assertDoesNotThrow(
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).map(dObject, __ -> null)
		);
		Assertions.assertNull(
			dObject.c
		);
	}

	@Test
	void mapThroughNullValuedField() {
		D dObject = new D();
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			).map(dObject, __ -> new C())
		);
	}

	@Test
	void mapToWrongType() {
		D dObject = new D();
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).map(dObject, __ -> new Object())
		);
	}

	@Test
	void mapPrivateField() {
		D dObject = new D();
		Assertions.assertThrows(
			IllegalAccessException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("cPrivate")
			).map(dObject, __ -> new C())
		);
	}

	@Test
	void mapWithNull() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).map(null, __ -> new C())
		);
	}

	@Test
	void mapWithObjectOfIncorrectType() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new InstanceFieldPath(
				D.class.getDeclaredField("c")
			).map(new Object(), __ -> new C())
		);
	}
}