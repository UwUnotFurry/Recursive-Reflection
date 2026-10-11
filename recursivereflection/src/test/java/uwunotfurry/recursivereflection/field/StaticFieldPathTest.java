package uwunotfurry.recursivereflection.field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public final class StaticFieldPathTest {

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

	static {
		A.bStatic.c.c = new C();
	}

	@Test
	void newWithSingleStaticField() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic")
			)
		);
	}

	@Test
	void newWithOneStaticThenNonStaticFields() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void newWithRepeatedFields() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void newWithPrivateField() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("cPrivate")
			)
		);
	}

	@Test
	void newWithNonStaticFirstField() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("b")
			)
		);
	}

	@Test
	void newWithStaticFurtherFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic"),
				B.class.getDeclaredField("cStatic")
			)
		);
	}

	@Test
	void newWithNonChainingFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic"),
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void resolveNonStaticChainingFields() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic")
			).resolve(
				B.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void resolveNonStaticNonChainingFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic")
			).resolve(
				C.class.getDeclaredField("c")
			)
		);
	}

	@Test
	void resolveStaticChainingFields() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic")
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
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				)
			).getType()
		);
	}

	@Test
	void getRequiredType() {
		Assertions.assertEquals(
			A.class,
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				)
			).getRequiredType()
		);
	}

	@Test
	void getCanonicalPath() {
		Assertions.assertEquals(
			A.class.getCanonicalName() + ".bStatic.c.c",
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic"),
					B.class.getDeclaredField("c"),
					C.class.getDeclaredField("c")
				)
			).getCanonicalPath()
		);
	}

	@Test
	void equalsSamePath() {
		Assertions.assertEquals(
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				)
			),
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				)
			)
		);
	}

	@Test
	void equalsDifferentPath() {
		Assertions.assertNotEquals(
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				)
			),
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic"),
					B.class.getDeclaredField("c")
				)
			)
		);
	}

	@Test
	void equalsDifferentType() {
		Assertions.assertNotEquals(
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				)
			),
			new Object()
		);
	}

	@Test
	void getOnSingleFieldPath() {
		Assertions.assertSame(
			A.bStatic,
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				).get(Object.class)
			)
		);
	}

	@Test
	void getOnMultiFieldPath() {
		Assertions.assertSame(
			A.bStatic.c.c,
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic"),
					B.class.getDeclaredField("c"),
					C.class.getDeclaredField("c")
				).get(Object.class)
			)
		);
	}

	@Test
	void getAsType() {
		Assertions.assertSame(
			A.bStatic,
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				).get(B.class)
			)
		);
	}

	@Test
	void getNullValuedField() {
		Assertions.assertNull(
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic"),
					B.class.getDeclaredField("c"),
					C.class.getDeclaredField("c"),
					C.class.getDeclaredField("c")
				).get(Object.class)
			)
		);
	}

	@Test
	void getThroughNullValuedField() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c"),
				C.class.getDeclaredField("c")
			).get(Object.class)
		);
	}

	@Test
	void getAsWrongType() {
		Assertions.assertThrows(
			ClassCastException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic")
			).get(C.class)
		);
	}

	@Test
	void getPrivateField() {
		Assertions.assertThrows(
			IllegalAccessException.class,
			() -> new StaticFieldPath(
				A.class.getDeclaredField("bStatic"),
				B.class.getDeclaredField("c"),
				C.class.getDeclaredField("cPrivate")
			).get(Object.class)
		);
	}

	@Test
	void getWithNull() {
		Assertions.assertSame(
			A.bStatic,
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				).get(null, Object.class)
			)
		);
	}

	@Test
	void getWithObjectOfCorrectType() {
		Assertions.assertSame(
			A.bStatic,
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				).get(new A(), Object.class)
			)
		);
	}

	@Test
	void getWithObjectOfIncorrectType() {
		Assertions.assertSame(
			A.bStatic,
			Assertions.assertDoesNotThrow(
				() -> new StaticFieldPath(
					A.class.getDeclaredField("bStatic")
				).get(new Object(), Object.class)
			)
		);
	}

	private static class D {
		static C cStatic1;
		static C cStatic2;
		static C cStatic3;
		static C cStatic4;
		static C cStatic5;
		static C cStatic6;
		static C cStatic7;

		static C cStatic8 = new C();
		static C cStatic9 = new C();
		static C cStatic10;
		static C cStatic11 = new C();
		static C cStatic12;
		static C cStatic13 = new C();
		static C cStatic14 = new C();
		static C cStatic15 = new C();
		static C cStatic16 = new C();

		static C cStaticNested1 = new C();
		static C cStaticNested2 = new C();
		static {
			cStaticNested1.c = new C();
			cStaticNested2.c = new C();
		}

		private static C cStaticPrivate1;
		private static C cStaticPrivate2;
	}

	@Test
	void setOnSingleFieldPath() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic1")
			).set(cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic1
		);
	}

	@Test
	void setOnMultiFieldPath() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStaticNested1"),
				C.class.getDeclaredField("c")
			).set(cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStaticNested1.c
		);
	}

	@Test
	void setNull() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic2")
			).set(null)
		);
		Assertions.assertNull(D.cStatic2);
	}

	@Test
	void setThroughNullValuedField() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic3"),
				C.class.getDeclaredField("c")
			).set(new C())
		);
	}

	@Test
	void setWrongType() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic4")
			).set(new Object())
		);
	}

	@Test
	void setPrivateField() {
		Assertions.assertThrows(
			IllegalAccessException.class,
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStaticPrivate1")
			).set(new C())
		);
	}

	@Test
	void setWithNull() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic5")
			).set(null, cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic5
		);
	}

	@Test
	void setWithObjectOfCorrectType() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic6")
			).set(new D(), cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic6
		);
	}

	@Test
	void setWithObjectOfIncorrectType() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic7")
			).set(new Object(), cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic7
		);
	}

	@Test
	void mapProvidedSameObject() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic8")
			).map(cObject -> {
				Assertions.assertSame(
					D.cStatic8,
					cObject
				);
				return cObject;
			})
		);
	}

	@Test
	void mapOnSingleFieldPath() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic9")
			).map(__ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic9
		);
	}

	@Test
	void mapOnMultiFieldPath() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStaticNested2"),
				C.class.getDeclaredField("c")
			).map(__ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStaticNested2.c
		);
	}

	@Test
	void mapOnNullValuedField() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic10")
			).map(__ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic10
		);
	}

	@Test
	void mapToNull() {
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic11")
			).map(__ -> null)
		);
		Assertions.assertNull(
			D.cStatic11
		);
	}

	@Test
	void mapThroughNullValuedField() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic12"),
				C.class.getDeclaredField("c")
			).map(__ -> new C())
		);
	}

	@Test
	void mapToWrongType() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic13")
			).map(__ -> new Object())
		);
	}

	@Test
	void mapPrivateField() {
		Assertions.assertThrows(
			IllegalAccessException.class,
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStaticPrivate2")
			).map(__ -> new C())
		);
	}

	@Test
	void mapWithNull() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic14")
			).map(null, __ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic14
		);
	}

	@Test
	void mapWithObjectOfCorrectType() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic15")
			).map(new D(), __ -> cObject)
		);
		Assertions.assertSame(
			cObject,
			D.cStatic15
		);
	}

	@Test
	void mapWithObjectOfIncorrectType() {
		C cObject = new C();
		Assertions.assertDoesNotThrow(
			() -> new StaticFieldPath(
				D.class.getDeclaredField("cStatic16")
			).map(new Object(), __ -> cObject)
		);
	}
}