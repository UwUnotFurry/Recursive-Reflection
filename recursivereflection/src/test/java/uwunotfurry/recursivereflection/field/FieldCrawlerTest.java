package uwunotfurry.recursivereflection.field;

import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public final class FieldCrawlerTest {

	// Behold! The most insane testing you've likely ever seen.
	// Complicated but hopefully thorough!

	private static abstract class SuperClass {}
	private static interface Interface {}

	private static class A {
		public static B bPublicStaticNull;
		protected static B bProtectedStaticNull;
		private static B bPrivateStaticNull;
		static B bPackageStaticNull;

		public static B bPublicStatic = new B();
		protected static B bProtectedStatic = new B();
		private static B bPrivateStatic = new B();
		static B bPackageStatic = new B();

		public B bPublicNull;
		protected B bProtectedNull;
		private B bPrivateNull;
		B bPackageNull;

		public B bPublic = new B();
		protected B bProtected = new B();
		private B bPrivate = new B();
		B bPackage = new B();
	}

	private static class B implements Interface {
		public static C cPublicStaticNull;
		protected static C cProtectedStaticNull;
		private static C cPrivateStaticNull;
		static C cPackageStaticNull;

		public static C cPublicStatic = new C();
		protected static C cProtectedStatic = new C();
		private static C cPrivateStatic = new C();
		static C cPackageStatic = new C();

		public C cPublicNull;
		protected C cProtectedNull;
		private C cPrivateNull;
		C cPackageNull;

		public C cPublic = new C();
		protected C cProtected = new C();
		private C cPrivate = new C();
		C cPackage = new C();
	}

	private static class C extends SuperClass {
		public static Object objectPublicStaticNull;
		protected static Object objectProtectedStaticNull;
		private static Object objectPrivateStaticNull;
		static Object objectPackageStaticNull;

		public static Object objectPublicStatic = new Object();
		protected static Object objectProtectedStatic = new Object();
		private static Object objectPrivateStatic = new Object();
		static Object objectPackageStatic = new Object();

		public Object objectPublicNull;
		protected Object objectProtectedNull;
		private Object objectPrivateNull;
		Object objectPackageNull;

		public Object objectPublic = new Object();
		protected Object objectProtected = new Object();
		private Object objectPrivate = new Object();
		Object objectPackage = new Object();
	}

	private static Set<FieldPath> AllPaths;

	private static Set<FieldPath> AStaticPaths;
	private static Set<FieldPath> ANonStaticPaths;

	private static Set<FieldPath> AOnlyPaths;
	private static Set<FieldPath> ABOnlyPaths;

	private static Set<FieldPath> ANullPaths;
	private static Set<FieldPath> ANonNullPaths;

	private static Set<FieldPath> BNullPaths;
	private static Set<FieldPath> BNonNullPaths;

	private static Set<FieldPath> CNullPaths;
	private static Set<FieldPath> CNonNullPaths;

	private static Set<FieldPath> APublicPaths;
	private static Set<FieldPath> AProtectedPaths;
	private static Set<FieldPath> APrivatePaths;
	private static Set<FieldPath> APackagePaths;

	private static Set<FieldPath> BPublicPaths;
	private static Set<FieldPath> BProtectedPaths;
	private static Set<FieldPath> BPrivatePaths;
	private static Set<FieldPath> BPackagePaths;

	private static Set<FieldPath> CPublicPaths;
	private static Set<FieldPath> CProtectedPaths;
	private static Set<FieldPath> CPrivatePaths;
	private static Set<FieldPath> CPackagePaths;

	private static Set<FieldPath> inAny(Set<FieldPath>... pathSets) {
		ImmutableSet.Builder<FieldPath> out = ImmutableSet.builder();
		for (Set<FieldPath> pathSet : pathSets) {
			out.addAll(pathSet);
		}
		return out.build();
	}

	private static Set<FieldPath> inAll(Set<FieldPath>... pathSets) {
		Set<FieldPath> out = new HashSet<>(AllPaths);
		for (Set<FieldPath> pathSet : pathSets) {
			out.retainAll(pathSet);
		}
		return ImmutableSet.copyOf(out);
	}

	static {
		ImmutableSet.Builder<FieldPath> AllPathsBuilder        = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> AStaticPathsBuilder    = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> ANonStaticPathsBuilder = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> AOnlyPathsBuilder      = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> ABOnlyPathsBuilder     = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> ANullPathsBuilder      = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> ANonNullPathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> BNullPathsBuilder      = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> BNonNullPathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> CNullPathsBuilder      = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> CNonNullPathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> APublicPathsBuilder    = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> AProtectedPathsBuilder = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> APrivatePathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> APackagePathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> BPublicPathsBuilder    = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> BProtectedPathsBuilder = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> BPrivatePathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> BPackagePathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> CPublicPathsBuilder    = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> CProtectedPathsBuilder = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> CPrivatePathsBuilder   = ImmutableSet.builder();
		ImmutableSet.Builder<FieldPath> CPackagePathsBuilder   = ImmutableSet.builder();

		for (Field aField : A.class.getDeclaredFields()) {
			Set<ImmutableSet.Builder<FieldPath>> aIncludeIn = new HashSet<>();

			int aModifiers = aField.getModifiers();
			FieldPath aFieldPath;
			if (Modifier.isStatic(aModifiers)) {
				aFieldPath = new StaticFieldPath(aField);
				aIncludeIn.add(AStaticPathsBuilder);
			} else {
				aFieldPath = new InstanceFieldPath(aField);
				aIncludeIn.add(ANonStaticPathsBuilder);
			}

			if (Modifier.isPublic(aModifiers)) {
				aIncludeIn.add(APublicPathsBuilder);
			} else if (Modifier.isProtected(aModifiers)) {
				aIncludeIn.add(AProtectedPathsBuilder);
			} else if (Modifier.isPrivate(aModifiers)) {
				aIncludeIn.add(APrivatePathsBuilder);
			} else {
				aIncludeIn.add(APackagePathsBuilder);
			}

			if (aField.getName().contains("Null")) {
				aIncludeIn.add(ANullPathsBuilder);
			} else {
				aIncludeIn.add(ANonNullPathsBuilder);
			}

			AOnlyPathsBuilder.add(aFieldPath);
			AllPathsBuilder.add(aFieldPath);
			for (ImmutableSet.Builder<FieldPath> builder : aIncludeIn) {
				builder.add(aFieldPath);
			}

			for (Field bField : B.class.getDeclaredFields()) {
				Set<ImmutableSet.Builder<FieldPath>> bIncludeIn = new HashSet<>(aIncludeIn);

				int bModifiers = bField.getModifiers();
				if (Modifier.isStatic(bModifiers)) {
					continue;
				}

				FieldPath bFieldPath = aFieldPath.resolve(bField);

				if (Modifier.isPublic(bModifiers)) {
					bIncludeIn.add(BPublicPathsBuilder);
				} else if (Modifier.isProtected(bModifiers)) {
					bIncludeIn.add(BProtectedPathsBuilder);
				} else if (Modifier.isPrivate(bModifiers)) {
					bIncludeIn.add(BPrivatePathsBuilder);
				} else {
					bIncludeIn.add(BPackagePathsBuilder);
				}

				if (bField.getName().contains("Null")) {
					bIncludeIn.add(BNullPathsBuilder);
				} else {
					bIncludeIn.add(BNonNullPathsBuilder);
				}

				ABOnlyPathsBuilder.add(bFieldPath);
				AllPathsBuilder.add(bFieldPath);
				for (ImmutableSet.Builder<FieldPath> builder : bIncludeIn) {
					builder.add(bFieldPath);
				}

				for (Field cField : C.class.getDeclaredFields()) {
					Set<ImmutableSet.Builder<FieldPath>> cIncludeIn = new HashSet<>(bIncludeIn);

					int cModifiers = cField.getModifiers();
					if (Modifier.isStatic(cModifiers)) {
						continue;
					}

					FieldPath cFieldPath = bFieldPath.resolve(cField);

					if (Modifier.isPublic(cModifiers)) {
						cIncludeIn.add(CPublicPathsBuilder);
					} else if (Modifier.isProtected(cModifiers)) {
						cIncludeIn.add(CProtectedPathsBuilder);
					} else if (Modifier.isPrivate(cModifiers)) {
						cIncludeIn.add(CPrivatePathsBuilder);
					} else {
						cIncludeIn.add(CPackagePathsBuilder);
					}

					if (cField.getName().contains("Null")) {
						cIncludeIn.add(CNullPathsBuilder);
					} else {
						cIncludeIn.add(CNonNullPathsBuilder);
					}

					AllPathsBuilder.add(cFieldPath);
					for (ImmutableSet.Builder<FieldPath> builder : cIncludeIn) {
						builder.add(cFieldPath);
					}
				}
			}
		}

		AllPaths        = AllPathsBuilder       .build();
		AStaticPaths    = AStaticPathsBuilder   .build();
		ANonStaticPaths = ANonStaticPathsBuilder.build();
		AOnlyPaths      = AOnlyPathsBuilder     .build();
		ABOnlyPaths     = ABOnlyPathsBuilder    .build();
		ANullPaths      = ANullPathsBuilder     .build();
		ANonNullPaths   = ANonNullPathsBuilder  .build();
		BNullPaths      = BNullPathsBuilder     .build();
		BNonNullPaths   = BNonNullPathsBuilder  .build();
		CNullPaths      = CNullPathsBuilder     .build();
		CNonNullPaths   = CNonNullPathsBuilder  .build();
		APublicPaths    = APublicPathsBuilder   .build();
		AProtectedPaths = AProtectedPathsBuilder.build();
		APrivatePaths   = APrivatePathsBuilder  .build();
		APackagePaths   = APackagePathsBuilder  .build();
		BPublicPaths    = BPublicPathsBuilder   .build();
		BProtectedPaths = BProtectedPathsBuilder.build();
		BPrivatePaths   = BPrivatePathsBuilder  .build();
		BPackagePaths   = BPackagePathsBuilder  .build();
		CPublicPaths    = CPublicPathsBuilder   .build();
		CProtectedPaths = CProtectedPathsBuilder.build();
		CPrivatePaths   = CPrivatePathsBuilder  .build();
		CPackagePaths   = CPackagePathsBuilder  .build();
	}

	@Test
	void searchAndAcceptAny() {
		Assertions.assertEquals(
			AllPaths,
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(FieldCrawler.Builder.FieldCondition.any())
					.acceptFieldsMatching(FieldCrawler.Builder.FieldCondition.any())
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptPublic() {
		Assertions.assertEquals(
			inAny(
				inAll(
					APublicPaths,
					BPublicPaths,
					CPublicPaths
				),
				inAll(
					APublicPaths,
					BPublicPaths,
					ABOnlyPaths
				),
				inAll(
					APublicPaths,
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.anyType()
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptProtected() {
		Assertions.assertEquals(
			inAny(
				inAll(
					AProtectedPaths,
					BProtectedPaths,
					CProtectedPaths
				),
				inAll(
					AProtectedPaths,
					BProtectedPaths,
					ABOnlyPaths
				),
				inAll(
					AProtectedPaths,
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowProtected()
							.anyType()
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowProtected()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptPrivate() {
		Assertions.assertEquals(
			inAny(
				inAll(
					APrivatePaths,
					BPrivatePaths,
					CPrivatePaths
				),
				inAll(
					APrivatePaths,
					BPrivatePaths,
					ABOnlyPaths
				),
				inAll(
					APrivatePaths,
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPrivate()
							.anyType()
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPrivate()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptPackage() {
		Assertions.assertEquals(
			inAny(
				inAll(
					APackagePaths,
					BPackagePaths,
					CPackagePaths
				),
				inAll(
					APackagePaths,
					BPackagePaths,
					ABOnlyPaths
				),
				inAll(
					APackagePaths,
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPackage()
							.anyType()
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPackage()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptPublicAndProtected() {
		Assertions.assertEquals(
			inAny(
				inAll(
					inAny(APublicPaths,AProtectedPaths),
					inAny(BPublicPaths,BProtectedPaths),
					inAny(CPublicPaths,CProtectedPaths)
				),
				inAll(
					inAny(APublicPaths,AProtectedPaths),
					inAny(BPublicPaths,BProtectedPaths),
					ABOnlyPaths
				),
				inAll(
					inAny(APublicPaths,AProtectedPaths),
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.allowProtected()
							.anyType()
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.allowProtected()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptNotPackage() {
		Assertions.assertEquals(
			inAny(
				inAll(
					inAny(APublicPaths,AProtectedPaths,APrivatePaths),
					inAny(BPublicPaths,BProtectedPaths,BPrivatePaths),
					inAny(CPublicPaths,CProtectedPaths,CPrivatePaths)
				),
				inAll(
					inAny(APublicPaths,AProtectedPaths,APrivatePaths),
					inAny(BPublicPaths,BProtectedPaths,BPrivatePaths),
					ABOnlyPaths
				),
				inAll(
					inAny(APublicPaths,AProtectedPaths,APrivatePaths),
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.exceptPackage()
							.anyType()
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.exceptPackage()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptNonNull() {
		Assertions.assertEquals(
			inAny(
				inAll(
					ANonNullPaths,
					BNonNullPaths,
					CNonNullPaths
				),
				inAll(
					ANonNullPaths,
					BNonNullPaths,
					ABOnlyPaths
				),
				inAll(
					ANonNullPaths,
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition
							.any()
							.notNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition
							.any()
							.notNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAndAcceptNonNullFromNullObject() {
		Assertions.assertEquals(
			inAll(
				AStaticPaths,
				inAny(
					inAll(
						ANonNullPaths,
						BNonNullPaths,
						CNonNullPaths
					),
					inAll(
						ANonNullPaths,
						BNonNullPaths,
						ABOnlyPaths
					),
					inAll(
						ANonNullPaths,
						AOnlyPaths
					)
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition
							.any()
							.notNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition
							.any()
							.notNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, null)
			)
		);
	}

	@Test
	void searchPublicAcceptAny() {
		Assertions.assertEquals(
			inAny(
				inAll(
					APublicPaths,
					BPublicPaths
				),
				inAll(
					APublicPaths,
					ABOnlyPaths
				),
				inAll(
					AOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.anyType()
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAnyAcceptTypeDeclaredFinal() {
		Assertions.assertEquals(
			inAll(
				ABOnlyPaths,
				inAny(
					BPublicPaths,
					BProtectedPaths,
					BPrivatePaths,
					BPackagePaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.ofType(C.class)
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAnyAcceptObjectType() {
		Assertions.assertEquals(
			inAny(
				CPublicPaths,
				CProtectedPaths,
				CPrivatePaths,
				CPackagePaths
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.ofType(Object.class)
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAnyAcceptSubtypesOfClass() {
		Assertions.assertEquals(
			inAll(
				ABOnlyPaths,
				inAny(
					BPublicPaths,
					BProtectedPaths,
					BPrivatePaths,
					BPackagePaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.ofSubtypeOf(SuperClass.class)
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAnyAcceptSubtypesOfInterface() {
		Assertions.assertEquals(
			inAll(
				AOnlyPaths,
				inAny(
					APublicPaths,
					AProtectedPaths,
					APrivatePaths,
					APackagePaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.ofSubtypeOf(Interface.class)
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void stopAtAcceptedFields() {
		Assertions.assertEquals(
			inAny(
				inAll(
					APublicPaths,
					AOnlyPaths
				),
				inAll(
					inAny(
						AProtectedPaths,
						APrivatePaths,
						APackagePaths
					),
					BPublicPaths,
					ABOnlyPaths
				),
				inAll(
					inAny(
						AProtectedPaths,
						APrivatePaths,
						APackagePaths
					),
					inAny(
						BProtectedPaths,
						BPrivatePaths,
						BPackagePaths
					),
					CPublicPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(true)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchAnyAcceptPublicTypeOrProtectedOtherType() {
		Assertions.assertEquals(
			inAny(
				inAll(
					APublicPaths,
					AOnlyPaths
				),
				inAll(
					BProtectedPaths,
					ABOnlyPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatchingAny(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.ofType(B.class)
							.allowNullValued()
						,
						FieldCrawler.fieldCondition()
							.allowProtected()
							.ofType(C.class)
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void searchPublicTypeOrProtectedOtherTypeAcceptAny() {
		Assertions.assertEquals(
			inAny(
				AOnlyPaths,
				inAll(
					APublicPaths,
					ABOnlyPaths
				),
				inAll(
					APublicPaths,
					BProtectedPaths
				)
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatchingAny(
						FieldCrawler.fieldCondition()
							.allowPublic()
							.ofType(B.class)
							.allowNullValued()
						,
						FieldCrawler.fieldCondition()
							.allowProtected()
							.ofType(C.class)
							.allowNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsRootedAt(A.class, new A())
			)
		);
	}

	@Test
	void tryNoAccessModifiers() {
		Assertions.assertThrows(
			IllegalStateException.class,
			() -> FieldCrawler.builder()
				.searchFieldsMatching(
					FieldCrawler.fieldCondition()
						.anyType()
						.allowNullValued()
				)
				.acceptFieldsMatching(
					FieldCrawler.Builder.FieldCondition.any()
				)
				.stopAtAcceptedFields(false)
				.build()
		);
	}

	@Test
	void tryNoType() {
		Assertions.assertThrows(
			IllegalStateException.class,
			() -> FieldCrawler.builder()
				.searchFieldsMatching(
					FieldCrawler.fieldCondition()
						.anyAccessModifier()
						.allowNullValued()
				)
				.acceptFieldsMatching(
					FieldCrawler.Builder.FieldCondition.any()
				)
				.stopAtAcceptedFields(false)
				.build()
		);
	}

	@Test
	void tryNoAcceptFieldCondition() {
		Assertions.assertThrows(
			IllegalStateException.class,
			() -> FieldCrawler.builder()
				.searchFieldsMatching(
					FieldCrawler.Builder.FieldCondition.any()
				)
				.stopAtAcceptedFields(false)
				.build()
		);
	}

	@Test
	void tryNoSearchFieldCondition() {
		Assertions.assertThrows(
			IllegalStateException.class,
			() -> FieldCrawler.builder()
				.acceptFieldsMatching(
					FieldCrawler.Builder.FieldCondition.any()
				)
				.stopAtAcceptedFields(false)
				.build()
		);
	}

	@Test
	void makeFieldsAccessible() {
		A aObject = new A();
		Assertions.assertDoesNotThrow(
			() -> {
				for (FieldPath path : FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.anyType()
							.notNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.stopAtAcceptedFields(false)
					.makeFieldsAccessible(true)
					.build()
					.findFieldsRootedAt(A.class, aObject)
				) {
					path.get(aObject, Object.class);
				}
			}
		);
	}

	// Does not test with the protected fields:
	// JUnit makes protected members in the same package accessible
	// - including those in the inner classes of the test class;
	// meaning protected field access cannot be meaningfully tested.
	@Test
	void dontMakeFieldsAccessible() {
		A aObject = new A();
		Set<FieldPath> canBeAccessed = inAny(
			inAll(
				inAny(APublicPaths, APackagePaths),
				AOnlyPaths
			),
			inAll(
				inAny(APublicPaths, APackagePaths),
				inAny(BPublicPaths, BPackagePaths),
				ABOnlyPaths
			),
			inAll(
				inAny(APublicPaths, APackagePaths),
				inAny(BPublicPaths, BPackagePaths),
				inAny(CPublicPaths, CPackagePaths)
			)
		);

		for (FieldPath path :
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.exceptProtected()
							.anyType()
							.notNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.exceptProtected()
							.anyType()
							.allowNullValued()
					)
					.stopAtAcceptedFields(false)
					.makeFieldsAccessible(false)
					.build()
					.findFieldsRootedAt(A.class, aObject)
			)
		) {
			if (canBeAccessed.contains(path)) {
				Assertions.assertDoesNotThrow(
					() -> path.get(aObject, Object.class)
				);
			} else {
				Assertions.assertThrows(
					IllegalAccessException.class,
					() -> path.get(aObject, Object.class),
					path.toString()
				);
			}
		}
	}

	@Test
	void findStatic() {
		Assertions.assertEquals(
			AStaticPaths,
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsStaticlyRootedAt(A.class)
			)
		);
	}

	@Test
	void findNonStatic() {
		Assertions.assertEquals(
			ANonStaticPaths,
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.stopAtAcceptedFields(false)
					.build()
					.findFieldsNonStaticlyRootedAt(A.class, new A())
			)
		);
	}

	private static class D {
		static D dStatic = new D();
		D d;
	}

	private static D dObject = new D();

	static {
		D.dStatic.d = new D();
		D.dStatic.d.d = new D();
		D.dStatic.d.d.d = new D();

		dObject.d = new D();
		dObject.d.d = new D();
	}

	@Test
	void stopAtCycles() {
		Assertions.assertEquals(
			Assertions.assertDoesNotThrow(
				() -> ImmutableSet.builder()
					.add(
						new StaticFieldPath(
							D.class.getDeclaredField("dStatic")
						),
						new InstanceFieldPath(
							D.class.getDeclaredField("d")
						)
					)
					.build()
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.stopAtAcceptedFields(false)
					.stopAtCycles(true)
					.build()
					.findFieldsRootedAt(D.class, dObject)
			)
		);
	}

	@Test
	void dontStopAtCyclesButCheckNull() {
		Assertions.assertEquals(
			Assertions.assertDoesNotThrow(
				() -> ImmutableSet.builder()
					.add(
						new StaticFieldPath(
							D.class.getDeclaredField("dStatic")
						),
						new StaticFieldPath(
							D.class.getDeclaredField("dStatic"),
							D.class.getDeclaredField("d")
						),
						new StaticFieldPath(
							D.class.getDeclaredField("dStatic"),
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d")
						),
						new StaticFieldPath(
							D.class.getDeclaredField("dStatic"),
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d")
						),
						new StaticFieldPath(
							D.class.getDeclaredField("dStatic"),
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d")
						),
						new InstanceFieldPath(
							D.class.getDeclaredField("d")
						),
						new InstanceFieldPath(
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d")
						),
						new InstanceFieldPath(
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d"),
							D.class.getDeclaredField("d")
						)
					)
					.build()
			),
			Assertions.assertDoesNotThrow(
				() -> FieldCrawler.builder()
					.searchFieldsMatching(
						FieldCrawler.fieldCondition()
							.anyAccessModifier()
							.anyType()
							.notNullValued()
					)
					.acceptFieldsMatching(
						FieldCrawler.Builder.FieldCondition.any()
					)
					.stopAtAcceptedFields(false)
					.stopAtCycles(false)
					.build()
					.findFieldsRootedAt(D.class, dObject)
			)
		);
	}

	@Test
	void dontStopAtCyclesButAllowNull() {
		Assertions.assertThrows(
			// OutOfMemoryError or StackOverflowError
			VirtualMachineError.class,
			() -> FieldCrawler.builder()
				.searchFieldsMatching(
					FieldCrawler.Builder.FieldCondition.any()
				)
				.acceptFieldsMatching(
					FieldCrawler.Builder.FieldCondition.any()
				)
				.stopAtAcceptedFields(false)
				.stopAtCycles(false)
				.build()
				.findFieldsRootedAt(D.class, dObject)
		);
	}
}